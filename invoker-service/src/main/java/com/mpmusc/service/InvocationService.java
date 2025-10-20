package com.mpmusc.service;

import com.mpmusc.dto.StartingRttRequest;
import com.mpmusc.invoker.FunctionInvoker;
import com.mpmusc.invoker.ProviderResponse;
import com.mpmusc.model.ProviderMetric;
import com.mpmusc.repository.ProviderAvgRttRepository;
import com.mpmusc.repository.ProviderMetricRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvocationService {

    private final Map<String, FunctionInvoker> invokers;
    private final ProviderMetricRepository providerMetricRepository;
    private final ProviderAvgRttRepository providerAvgRttRepository;

    @Autowired
    public InvocationService(Map<String, FunctionInvoker> invokers,
                             ProviderMetricRepository providerMetricRepository,
                             ProviderAvgRttRepository providerAvgRttRepository) {
        this.invokers = invokers;
        this.providerMetricRepository = providerMetricRepository;
        this.providerAvgRttRepository = providerAvgRttRepository;
    }

    public List<String> analyze(String provider, int count) {
        List<String> log = new ArrayList<>();
        List<String> providers = (provider == null)
                ? List.copyOf(invokers.keySet())
                : List.of(provider.toLowerCase());

        for (String p : providers) {
            FunctionInvoker inv = invokers.get(p);
            if (inv == null) {
                log.add("Unknown provider: " + p);
                continue;
            }

            for (int i = 0; i < count; i++) {
                long start = System.currentTimeMillis();
                ProviderResponse resp;
                try {
                    resp = inv.invoke("fake_employees_100k.csv");
                } catch (Exception e) {
                    resp = new ProviderResponse(e.getMessage(), false, e.getMessage(), 0L);
                }
                long rtt = System.currentTimeMillis() - start;

                saveProviderMetric(p, rtt, resp, inv, inv.getConcurrency());

                log.add(String.format("%s [%d ms, %s]",
                        p, rtt, resp.isSuccess() ? "OK" : "ERR"));
            }
        }
        return log;
    }

    private void saveProviderMetric(String providerName, long rtt, ProviderResponse resp, FunctionInvoker inv, int currentConcurrency) {
        ProviderMetric m = new ProviderMetric();
        m.setProvider(providerName);
        m.setTotalTimeMs(rtt);
        m.setExecutionTimeMs(resp.getExecutionTimeMs());
        m.setError(!resp.isSuccess());
        m.setErrorMsg(resp.getErrorMsg());

        // cost—we’ll assume AWS cost model gives fixed price per 100ms per memory;
        // here just pull from config or return zero:
        m.setRegion(inv.getRegion());
        m.setCost(inv.getEstimatedCost());
        m.setConcurrency(currentConcurrency);
        providerMetricRepository.save(m);
    }

    public Map<String, Object> schedule(String scheduleType) {
        List<ProviderMetric> recent = providerMetricRepository.findSince(LocalDateTime.now().minusHours(1));

        // Group by provider, compute averages
        Map<String, Candidate> candidates = recent.stream()
                .collect(Collectors.groupingBy(ProviderMetric::getProvider))
                .entrySet().stream()
                .map(e -> {
                    String p = e.getKey();
                    List<ProviderMetric> metrics = e.getValue();
                    long avgRtt = (long) metrics.stream().mapToLong(ProviderMetric::getTotalTimeMs).average().orElse(Double.MAX_VALUE);
                    double errRate = metrics.stream().filter(ProviderMetric::isError).count() / (double) metrics.size();
                    BigDecimal avgCost = metrics.stream().map(ProviderMetric::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(metrics.size()), RoundingMode.HALF_UP);
                    return new Candidate(p, avgRtt, errRate, avgCost);
                })
                .collect(Collectors.toMap(Candidate::getProvider, c -> c));

        Candidate best = candidates.values().stream()
                .sorted((a, b) -> a.compareBy(scheduleType, b))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No candidates"));

        ProviderResponse resp;
        try {
            resp = invokers.get(best.getProvider())
                    .invoke("fake_employees_100k.csv");
        } catch (Exception e) {
            resp = new ProviderResponse(e.getMessage(), false, e.getMessage(), 0L);
        }

        return Map.of("chosenProvider", best.getProvider(), "payload", resp.getPayload());
    }



    /**
     * Helper holder for scheduling logic
     */
    private static class Candidate {
        private final String provider;
        private final long avgRtt;
        private final double errRate;
        private final BigDecimal avgCost;

        Candidate(String p, long r, double e, BigDecimal c) {
            provider = p;
            avgRtt = r;
            errRate = e;
            avgCost = c;
        }

        public String getProvider() {
            return provider;
        }

        /**
         * Compare two candidates by the given scheduleType
         */
        public int compareBy(String type, Candidate other) {
            switch (type) {
                case "lowLatency":
                    return Double.compare(this.avgRtt, other.avgRtt);
                case "lowError":
                    return Double.compare(this.errRate, other.errRate);
                case "lowCost":
                    return this.avgCost.compareTo(other.avgCost);
                default:
                    return Double.compare(this.avgRtt, other.avgRtt);
            }
        }
    }

    public Map<String, List<Long>> benchmarkProviders_singleFunctionRequest() {
        long durationMs = 10 * 60 * 1000 + 15000; // 6 minutes
        long intervalMs = 15_000;        // 15 seconds

        Map<String, List<Long>> results = new ConcurrentHashMap<>();
        invokers.keySet().forEach(p -> results.put(p, Collections.synchronizedList(new ArrayList<>())));

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(invokers.size());

        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs;

        for (String provider : invokers.keySet()) {
            scheduler.scheduleAtFixedRate(() -> {
                long now = System.currentTimeMillis();
                if (now > endTime) {
                    return; // stop once duration exceeded
                }
                try {
                    long start = System.currentTimeMillis();
                    FunctionInvoker functionInvoker = invokers.get(provider);
                    ProviderResponse resp = functionInvoker.invoke("fake_employees_100k.csv");
                    long rtt = System.currentTimeMillis() - start;
                    saveProviderMetric(provider, rtt, resp, functionInvoker, functionInvoker.getConcurrency());

                    if (resp.isSuccess()) {
                        results.get(provider).add(rtt);
                    } else {
                        results.get(provider).add(-1L); // mark failure
                    }
                } catch (Exception e) {
                    results.get(provider).add(-1L);
                }
            }, 0, intervalMs, TimeUnit.MILLISECONDS);
        }

        // Stop scheduler after duration
        scheduler.schedule(() -> scheduler.shutdown(), durationMs, TimeUnit.MILLISECONDS);

        try {
            scheduler.awaitTermination(durationMs + 5_000, TimeUnit.MILLISECONDS); // wait with buffer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return results;
    }

    public Map<String, List<Long>> benchmarkProviders_parallelFunctionRequests(String provider, int count) {
        System.out.println("=== Starting benchmark for " + provider + " ===");
        Map<String, List<Long>> results = new HashMap<>();
        results.put(provider, new ArrayList<>());

        long durationMs = 10 * 60 * 1000 + 15000; // 5 minutes
        long intervalMs = 15 * 1000;     // 15 seconds
        long endTime = System.currentTimeMillis() + durationMs;

        FunctionInvoker functionInvoker = invokers.get(provider);
        int originalConcurrency = functionInvoker.getConcurrency();
        ExecutorService executor = Executors.newFixedThreadPool(count);

        while (System.currentTimeMillis() < endTime) {
            long iterationStart = System.currentTimeMillis();
            AtomicInteger remainingConcurrency = new AtomicInteger(originalConcurrency);

            // run count parallel requests
            List<CompletableFuture<Long>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    int currentConcurrency = remainingConcurrency.getAndDecrement();
                    long start = System.currentTimeMillis();
                    try {
                        ProviderResponse resp = functionInvoker.invoke("fake_employees_100k.csv");
                        long rtt = System.currentTimeMillis() - start;
                        saveProviderMetric(provider, resp.isSuccess() ? rtt : -1L, resp, functionInvoker, currentConcurrency);
                        return resp.isSuccess() ? rtt : -1L;
                    } catch (Exception e) {
                        ProviderResponse resp = new ProviderResponse(
                                "Error when invoking: " + e.getMessage(),
                                false,
                                e.getMessage(),
                                -1L
                        );
                        saveProviderMetric(provider, -1L, resp, functionInvoker, currentConcurrency);
                        log.error("{} | Error occurred at benchmarkProviders_parallelFunctionRequests: {}", provider, e.getMessage());
                        return -1L;
                    }
                }, executor);
                futures.add(future);
            }

            // collect results
            List<Long> rtts = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            if (rtts.contains(-1L)) {
                // At least one failed in this batch
                results.get(provider).add(-1L);
            } else {
                long avg = Math.round(rtts.stream().mapToLong(Long::longValue).average().orElse(0));
                results.get(provider).add(avg);
            }


            // adjust sleep so the total cycle time = 15s
            long elapsed = System.currentTimeMillis() - iterationStart;
            long remaining = intervalMs - elapsed;
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        executor.shutdown();
        System.out.println("=== Finished benchmark for " + provider + " ===");
        return results;
    }

//    public Object smartSchedule(int concurrency) {
//        long durationMs = 10 * 60 * 1000 + 15000;
//        long intervalMs = 15 * 1000;
//        long endTime = System.currentTimeMillis() + durationMs;
//
//        Map<String, Long> currentRtts = getInitialRtts(req.getStartingRtts(), concurrency);
//        boolean firstIteration = true;
//
//        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
//
//        while (System.currentTimeMillis() < endTime) {
//            long iterationStart = System.currentTimeMillis();
//
//            if (!firstIteration) {
//                currentRtts = getRttsFromDb(concurrency);
//            }
//
//            // distribute requests
//            Map<String, Integer> allocation = distributeInvocations(currentRtts, concurrency);
//
//            List<CompletableFuture<Void>> futures = new ArrayList<>();
//            allocation.forEach((provider, count) -> {
//                FunctionInvoker invoker = invokers.get(provider);
//                for (int i = 0; i < count; i++) {
//                    futures.add(CompletableFuture.runAsync(() -> {
//                        long start = System.currentTimeMillis();
//                        try {
//                            ProviderResponse resp = invoker.invoke("fake_employees_100k.csv");
//                            long rtt = System.currentTimeMillis() - start;
//                            saveProviderAvg(provider, concurrency, rtt, 1); // save batch RTT
//                        } catch (Exception e) {
//                            saveProviderAvg(provider, concurrency, -1L, 1);
//                        }
//                    }, executor));
//                }
//            });
//
//            futures.forEach(CompletableFuture::join);
//
//            firstIteration = false;
//
//            // sleep until interval
//            long elapsed = System.currentTimeMillis() - iterationStart;
//            long remaining = intervalMs - elapsed;
//            if (remaining > 0) {
//                try {
//                    Thread.sleep(remaining);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }
//
//        executor.shutdown();
//        return "Benchmark finished";
//    }
//
//    private Map<String, Integer> distributeInvocations(Map<String, Long> currentRtts, int concurrency) {
//        List<String> sortedProviders = currentRtts.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .toList();
//
//        Map<String, Integer> allocation = new HashMap<>();
//        for (int i = 0; i < concurrency; i++) {
//            String p = sortedProviders.get(i % sortedProviders.size());
//            allocation.merge(p, 1, Integer::sum);
//        }
//        return allocation;
//    }
//
//    private Map<String, Long> getInitialRtts(Map<String, Map<Integer, Long>> startingRtts, int concurrency) {
//        return startingRtts.entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        e -> e.getValue().getOrDefault(concurrency, Long.MAX_VALUE)));
//    }
//
//    private Map<String, Long> getRttsFromDb(int concurrency) {
//        List<Object[]> rows = providerAvgRttRepository.findLatestAveragesByConcurrency(concurrency);
//        return rows.stream()
//                .collect(Collectors.toMap(
//                        r -> (String) r[0],   // provider
//                        r -> (Long) r[1]      // averageRtt
//                ));
//    }

}
