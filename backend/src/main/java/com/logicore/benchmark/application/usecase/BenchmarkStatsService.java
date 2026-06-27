package com.logicore.benchmark.application.usecase;

import com.logicore.benchmark.domain.model.BenchmarkEntry;
import com.logicore.benchmark.domain.repository.BenchmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BenchmarkStatsService {

    private final BenchmarkRepository repository;

    public BenchmarkStatsService(BenchmarkRepository repository) {
        this.repository = repository;
    }

    public record PercentileStats(
            String strategyIdentifier,
            long count,
            double minMs, double maxMs,
            double avgMs, double medianMs,
            double p95Ms, double p99Ms,
            double stddevMs,
            double avgDistanceKm,
            double minScoreGrade, double maxScoreGrade,
            double twoOptAvgImprovementKm
    ) {}

    public List<PercentileStats> computeStats(UUID organizationId, int limit) {
        List<BenchmarkEntry> all = repository.findByOrganization(organizationId, limit);

        Map<String, List<BenchmarkEntry>> byStrategy = all.stream()
                .collect(Collectors.groupingBy(BenchmarkEntry::strategyIdentifier));

        return byStrategy.entrySet().stream().map(e -> {
            String strategyId = e.getKey();
            List<BenchmarkEntry> entries = e.getValue();
            long[] times = entries.stream().mapToLong(BenchmarkEntry::executionTimeMs).sorted().toArray();
            double avg = Arrays.stream(times).average().orElse(0);
            double stddev = Math.sqrt(Arrays.stream(times)
                    .mapToDouble(t -> Math.pow(t - avg, 2)).average().orElse(0));
            double avgDist = entries.stream().mapToDouble(BenchmarkEntry::totalDistanceKm).average().orElse(0);
            double avgTwoOpt = entries.stream()
                    .mapToDouble(BenchmarkEntry::twoOptImprovementKm).average().orElse(0);
            double minScore = entries.stream()
                    .mapToDouble(be -> be.routeScore() != null ? be.routeScore() : 0).min().orElse(0);
            double maxScore = entries.stream()
                    .mapToDouble(be -> be.routeScore() != null ? be.routeScore() : 0).max().orElse(0);

            return new PercentileStats(
                    strategyId, times.length,
                    times.length > 0 ? times[0] : 0,
                    times.length > 0 ? times[times.length - 1] : 0,
                    avg, percentile(times, 50),
                    percentile(times, 95), percentile(times, 99),
                    stddev, avgDist, minScore, maxScore, avgTwoOpt
            );
        }).sorted(Comparator.comparingDouble(PercentileStats::avgMs)).toList();
    }

    private static double percentile(long[] sorted, int pct) {
        if (sorted.length == 0) return 0;
        int idx = (int) Math.ceil(pct / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }
}
