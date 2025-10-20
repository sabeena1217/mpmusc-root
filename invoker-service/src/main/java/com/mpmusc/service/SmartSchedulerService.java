package com.mpmusc.service;

import com.mpmusc.dto.ScheduleResult;
import com.mpmusc.invoker.FunctionInvoker;
import com.mpmusc.invoker.ProviderResponse;
import com.mpmusc.model.ProviderMetric;
import com.mpmusc.model.ProviderPerformance;
import com.mpmusc.repository.ProviderMetricRepository;
import com.mpmusc.repository.ProviderPerformanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Core scheduler service that finds the allocation of tasks to providers (respecting provider concurrency limits)
 * which minimizes the makespan (total completion time).
 */
@Service
@Slf4j
public class SmartSchedulerService {


    private final ProviderPerformanceRepository perfRepo;
    private final ProviderMetricRepository providerMetricRepository;

    // in-memory cache: provider -> (concurrency -> medianMs)
    private final Map<String, NavigableMap<Integer, Double>> performanceCache = new HashMap<>();

    public SmartSchedulerService(ProviderPerformanceRepository perfRepo,
                                 ProviderMetricRepository providerMetricRepository) {
        this.perfRepo = perfRepo;
        this.providerMetricRepository = providerMetricRepository;
        loadPerformanceCache();
    }

    private void loadPerformanceCache() {
        performanceCache.clear();
        List<ProviderPerformance> all = perfRepo.findAll();
        Map<String, List<ProviderPerformance>> byProvider = all.stream()
                .collect(Collectors.groupingBy(ProviderPerformance::getProvider));
        for (Map.Entry<String, List<ProviderPerformance>> e : byProvider.entrySet()) {
            NavigableMap<Integer, Double> m = new TreeMap<>();
            e.getValue().stream()
                    .sorted(Comparator.comparingInt(ProviderPerformance::getConcurrency))
                    .forEach(pp -> m.put(pp.getConcurrency(), pp.getMedianTimeMs()));
            performanceCache.put(e.getKey(), m);
        }
    }

    private Optional<Double> getMedianMs(String provider, int tasks) {
        if (tasks <= 0) return Optional.of(0.0);
        NavigableMap<Integer, Double> map = performanceCache.get(provider);
        if (map == null || map.isEmpty()) return Optional.empty();
        if (map.containsKey(tasks)) return Optional.of(map.get(tasks));
        Integer floor = map.floorKey(tasks);
        if (floor != null) return Optional.of(map.get(floor));
        return Optional.empty();
    }

    /**
     * Persist a provider metric entry (same fields as your earlier method).
     * This uses providerMetricRepository to store ProviderMetric.
     */
    private void saveProviderMetric(String providerName, long rtt, ProviderResponse resp,
                                    FunctionInvoker inv, int currentConcurrency) {
        try {
            ProviderMetric m = new ProviderMetric();
            m.setProvider(providerName);
            m.setTotalTimeMs(rtt >= 0 ? rtt : -1L);
            m.setExecutionTimeMs(resp != null ? resp.getExecutionTimeMs() : -1L);
            m.setError(resp == null || !resp.isSuccess());
            m.setErrorMsg(resp == null ? "no-response" : resp.getErrorMsg());
            m.setRegion(inv.getRegion());
            m.setCost(inv.getEstimatedCost() != null ? inv.getEstimatedCost() : BigDecimal.ZERO);
            m.setConcurrency(currentConcurrency);
            m.setRecordedAt(LocalDateTime.now());
            providerMetricRepository.save(m);
        } catch (Exception ex) {
            log.error("Failed to save provider metric for {}: {}", providerName, ex.getMessage());
        }
    }


    public ScheduleResult computeAndExecute(int totalConcurrency,
                                            Map<String, FunctionInvoker> invokers,
                                            String invokePayloadFilename) {

        if (totalConcurrency <= 0) {
            throw new IllegalArgumentException("concurrency must be >= 1");
        }

        // --- 1) compute optimal allocation (enumeration) ---
        List<String> providers = new ArrayList<>(invokers.keySet());
        int n = providers.size();
        int[] limits = new int[n];
        for (int i = 0; i < n; i++) limits[i] = invokers.get(providers.get(i)).getConcurrency();

        int totalCapacity = Arrays.stream(limits).sum();
        if (totalConcurrency > totalCapacity) {
            throw new IllegalArgumentException("Requested concurrency " + totalConcurrency +
                    " exceeds total provider capacity " + totalCapacity);
        }

        double[] bestMakespan = {Double.MAX_VALUE};
        int[] bestAlloc = new int[n];
        int[] currentAlloc = new int[n];
        enumerateAllocations(providers, limits, 0, totalConcurrency, currentAlloc, bestMakespan, bestAlloc);

        // Build estimated result maps from bestAlloc
        Map<String, Integer> allocation = new LinkedHashMap<>();
        Map<String, Double> providerEstimatedTimes = new LinkedHashMap<>();
        double estimatedCompletion = 0.0;
        int totalAssigned = 0;
        for (int i = 0; i < n; i++) {
            String p = providers.get(i);
            int tasks = bestAlloc[i];
            allocation.put(p, tasks);
            Double t = getMedianMs(p, tasks).orElse(0.0);
            providerEstimatedTimes.put(p, t);
            estimatedCompletion = Math.max(estimatedCompletion, t);
            totalAssigned += tasks;
        }

        // If nothing assigned, return immediately
        if (totalAssigned == 0) {
            ScheduleResult empty = new ScheduleResult();
            empty.setAllocation(allocation);
            empty.setProviderEstimatedTimesMs(providerEstimatedTimes);
            empty.setEstimatedCompletionTimeMs(estimatedCompletion);
            empty.setProviderActualAvgRttMs(Collections.emptyMap());
            empty.setProviderSuccessCount(Collections.emptyMap());
            empty.setProviderFailureCount(Collections.emptyMap());
            empty.setActualCompletionTimeMs(null);
            return empty;
        }

        // --- 2) Execute the allocation in parallel ---
        // Reasonable pool size: allow full parallelism but cap to avoid excessive threads.
        int cap = 200; // tuneable cap
        int poolSize = Math.min(Math.max(1, totalAssigned), cap);
        ExecutorService exec = Executors.newFixedThreadPool(poolSize);

        // Per-provider aggregates (no storing all RTTs)
        Map<String, LongAdder> sumRtt = new ConcurrentHashMap<>();
        Map<String, AtomicInteger> successCount = new ConcurrentHashMap<>();
        Map<String, AtomicInteger> failureCount = new ConcurrentHashMap<>();
        Map<String, AtomicLong> maxRtt = new ConcurrentHashMap<>();
        Map<String, AtomicInteger> remainingConcurrencyMap = new HashMap<>();

        // initialize per-provider structures
        allocation.keySet().forEach(p -> {
            sumRtt.put(p, new LongAdder());
            successCount.put(p, new AtomicInteger(0));
            failureCount.put(p, new AtomicInteger(0));
            maxRtt.put(p, new AtomicLong(Long.MIN_VALUE));
            remainingConcurrencyMap.put(p, new AtomicInteger(invokers.get(p).getConcurrency()));
        });

        List<CompletableFuture<Void>> allFutures = new ArrayList<>(totalAssigned);
        AtomicLong globalMaxRtt = new AtomicLong(Long.MIN_VALUE);

        for (Map.Entry<String, Integer> e : allocation.entrySet()) {
            final String provider = e.getKey();
            final int count = e.getValue();
            if (count <= 0) continue;
            final FunctionInvoker invoker = invokers.get(provider);
            final AtomicInteger remainingConcurrency = remainingConcurrencyMap.get(provider);

            for (int i = 0; i < count; i++) {
                CompletableFuture<Void> fut = CompletableFuture.runAsync(() -> {
                    // Capture remainingConcurrency just like your benchmark
                    int currentConcurrency = remainingConcurrency.getAndDecrement();
                    long startMs = System.currentTimeMillis();
                    ProviderResponse resp = null;
                    long rtt = -1L;
                    try {
                        resp = invoker.invoke(invokePayloadFilename);
                        rtt = System.currentTimeMillis() - startMs;

                        // save metric with the captured remainingConcurrency
                        saveProviderMetric(provider, resp.isSuccess() ? rtt : -1L, resp, invoker, currentConcurrency);

                        if (resp.isSuccess()) {
                            sumRtt.get(provider).add(rtt);
                            successCount.get(provider).incrementAndGet();
                            // update per-provider max and global max
                            maxRtt.get(provider).getAndAccumulate(rtt, Math::max);
                            globalMaxRtt.getAndAccumulate(rtt, Math::max);
                        } else {
                            failureCount.get(provider).incrementAndGet();
                            // still update max? We only consider successful RTTs for actualCompletionTimeMs per your requirement.
                        }
                    } catch (Exception ex) {
                        // Build error ProviderResponse to save similar to your other code
                        ProviderResponse errResp = new ProviderResponse(
                                "Error when invoking: " + ex.getMessage(),
                                false,
                                ex.getMessage(),
                                -1L
                        );
                        saveProviderMetric(provider, -1L, errResp, invoker, currentConcurrency);
                        failureCount.get(provider).incrementAndGet();
                        // log and continue
                        log.error("Error invoking provider {} : {}", provider, ex.getMessage());
                    }
                }, exec);
                allFutures.add(fut);
            }
        }

        // Wait for all tasks to complete
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

        // Compute per-provider averages, per-provider max, and global max
        Map<String, Double> providerActualAvg = new LinkedHashMap<>();
        Map<String, Long> providerMax = new LinkedHashMap<>();
        Map<String, Integer> providerSuccessCount = new LinkedHashMap<>();
        Map<String, Integer> providerFailureCount = new LinkedHashMap<>();

        boolean anySuccess = false;
        for (String p : allocation.keySet()) {
            int succ = successCount.get(p).get();
            int fail = failureCount.get(p).get();
            long max = maxRtt.get(p).get();
            long sum = sumRtt.get(p).sum();

            double avg = succ == 0 ? 0.0 : ((double) sum) / succ;
            providerActualAvg.put(p, avg);
            providerMax.put(p, (max == Long.MIN_VALUE) ? 0L : max);
            providerSuccessCount.put(p, succ);
            providerFailureCount.put(p, fail);

            if (succ > 0) anySuccess = true;
        }

        Double actualInvocationMaxMs = anySuccess ? (double) globalMaxRtt.get() : null;

        // Build final ScheduleResult
        ScheduleResult sr = new ScheduleResult();
        sr.setAllocation(allocation);
        sr.setProviderEstimatedTimesMs(providerEstimatedTimes);
        sr.setEstimatedCompletionTimeMs(estimatedCompletion);

        sr.setProviderActualAvgRttMs(providerActualAvg);
        sr.setProviderSuccessCount(providerSuccessCount);
        sr.setProviderFailureCount(providerFailureCount);
        // actualCompletionTimeMs is the slowest single successful invocation RTT across all providers
        sr.setActualCompletionTimeMs(actualInvocationMaxMs);

        // cleanup
        exec.shutdownNow();

        return sr;
    }

    /**
     * enumerateAllocations: same recursion method used previously to find best allocation.
     * Provided here for completeness — keep the version you already use (prunes invalid allocations by missing medians).
     */
    private void enumerateAllocations(List<String> providers, int[] limits,
                                      int index, int remainingTasks, int[] currentAlloc,
                                      double[] bestMakespan, int[] bestAlloc) {
        int n = providers.size();
        if (index == n) {
            if (remainingTasks == 0) {
                double makespan = 0.0;
                for (int i = 0; i < n; i++) {
                    String p = providers.get(i);
                    int tasks = currentAlloc[i];
                    Optional<Double> tOpt = getMedianMs(p, tasks);
                    if (!tOpt.isPresent()) {
                        makespan = Double.MAX_VALUE;
                        break;
                    }
                    makespan = Math.max(makespan, tOpt.get());
                    if (makespan >= bestMakespan[0]) break;
                }
                if (makespan < bestMakespan[0]) {
                    bestMakespan[0] = makespan;
                    System.arraycopy(currentAlloc, 0, bestAlloc, 0, n);
                }
            }
            return;
        }

        int maxForProvider = Math.min(remainingTasks, limits[index]);
        for (int t = 0; t <= maxForProvider; t++) {
            currentAlloc[index] = t;
            enumerateAllocations(providers, limits, index + 1, remainingTasks - t, currentAlloc, bestMakespan, bestAlloc);
        }
        currentAlloc[index] = 0;
    }
}