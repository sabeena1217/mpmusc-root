package com.mpmusc.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Core service to compute gender distribution from a CSV file present in resources.
 */
public class GenderDistributionService {

    public GenderDistributionResult analyze(GenderDistributionRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String filename = (request.getFilename() != null && !request.getFilename().isEmpty())
                    ? request.getFilename()
                    : "fake_employees_100k.csv"; // default file assumed in resources

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                return new GenderDistributionResult("File not found in resources: " + filename);
            }

            Map<String, Map<String, Double>> distribution = new HashMap<>();
            Map<String, Long> departmentTotals = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.readLine(); // Skip header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",", -1);
                    if (tokens.length < 5) continue;

                    String gender = tokens[2].trim();
                    String department = tokens[4].trim();

//                    simulateProcessingDelay(); // optional

                    distribution
                            .computeIfAbsent(department, k -> new HashMap<>())
                            .merge(gender, 1.0, Double::sum);

                    departmentTotals.merge(department, 1L, Long::sum);
                }
            }

            for (Map.Entry<String, Map<String, Double>> entry : distribution.entrySet()) {
                String department = entry.getKey();
                Map<String, Double> genderCounts = entry.getValue();
                long total = departmentTotals.get(department);

                for (Map.Entry<String, Double> genderEntry : genderCounts.entrySet()) {
                    double percentage = (genderEntry.getValue() / total) * 100;
                    genderCounts.put(genderEntry.getKey(),
                            BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
            }

            long endTime = System.currentTimeMillis();
            return new GenderDistributionResult(distribution, endTime - startTime);

        } catch (Exception e) {
            return new GenderDistributionResult("Exception: " + e.getMessage());
        }
    }

    private void simulateProcessingDelay() {
//        for (int i = 0; i < 50; i++) {
        for (int i = 0; i < 100; i++) {
            double temp = Math.sqrt(i) * Math.pow(i, 0.5);
        }
    }
}
