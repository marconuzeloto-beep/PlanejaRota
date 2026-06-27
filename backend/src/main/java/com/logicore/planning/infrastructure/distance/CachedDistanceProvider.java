package com.logicore.planning.infrastructure.distance;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.logicore.planning.domain.distance.DistanceMatrix;
import com.logicore.planning.domain.distance.DistanceProvider;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Caffeine-backed decorator over any DistanceProvider.
 * Caches pairwise distances with a 24h TTL and max 10,000 entries.
 * Matrix cache keyed on depot + sorted coordinate list.
 */
public class CachedDistanceProvider implements DistanceProvider {

    private static final Logger log = LoggerFactory.getLogger(CachedDistanceProvider.class);

    private final DistanceProvider delegate;

    private final Cache<String, Double> distanceCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(24))
            .recordStats()
            .build();

    private final Cache<String, DistanceMatrix> matrixCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofHours(1))
            .recordStats()
            .build();

    public CachedDistanceProvider(DistanceProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getProviderName() {
        return "CACHED(" + delegate.getProviderName() + ")";
    }

    @Override
    public double distanceKm(GeoCoordinate from, GeoCoordinate to) {
        String key = cacheKey(from, to);
        return distanceCache.get(key, k -> {
            double d = delegate.distanceKm(from, to);
            log.trace("Cache miss for distance {} → {}: {}km", from, to, d);
            return d;
        });
    }

    @Override
    public double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh) {
        return delegate.durationMinutes(from, to, avgSpeedKmh);
    }

    @Override
    public DistanceMatrix buildMatrix(GeoCoordinate depot, List<GeoCoordinate> locations, double avgSpeedKmh) {
        String key = matrixCacheKey(depot, locations, avgSpeedKmh);
        return matrixCache.get(key, k -> delegate.buildMatrix(depot, locations, avgSpeedKmh));
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats distanceCacheStats() {
        return distanceCache.stats();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats matrixCacheStats() {
        return matrixCache.stats();
    }

    private static String cacheKey(GeoCoordinate from, GeoCoordinate to) {
        return "%.6f,%.6f|%.6f,%.6f".formatted(
                from.latitude(), from.longitude(), to.latitude(), to.longitude());
    }

    private static String matrixCacheKey(GeoCoordinate depot, List<GeoCoordinate> locations, double speed) {
        var sb = new StringBuilder();
        sb.append("%.6f,%.6f".formatted(depot.latitude(), depot.longitude()));
        sb.append("|");
        for (GeoCoordinate c : locations) {
            sb.append("%.6f,%.6f;".formatted(c.latitude(), c.longitude()));
        }
        sb.append("|").append(speed);
        return sb.toString();
    }
}
