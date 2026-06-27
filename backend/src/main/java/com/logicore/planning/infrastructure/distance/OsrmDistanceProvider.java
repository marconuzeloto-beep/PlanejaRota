package com.logicore.planning.infrastructure.distance;

import com.logicore.planning.domain.distance.DistanceMatrix;
import com.logicore.planning.domain.distance.DistanceProvider;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * OSRM-backed distance provider for real road distances.
 * Requires a running OSRM instance (self-hosted or public demo).
 * Falls back to Haversine when OSRM is unreachable.
 */
@Component("osrmDistanceProvider")
public class OsrmDistanceProvider implements DistanceProvider {

    private static final Logger log = LoggerFactory.getLogger(OsrmDistanceProvider.class);

    private final String osrmBaseUrl;
    private final HaversineDistanceProvider fallback;
    private final RestTemplate restTemplate;

    public OsrmDistanceProvider(
            @Value("${app.distance.osrm.base-url:http://router.project-osrm.org}") String osrmBaseUrl,
            HaversineDistanceProvider fallback) {
        this.osrmBaseUrl = osrmBaseUrl;
        this.fallback = fallback;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getProviderName() {
        return "OSRM";
    }

    @Override
    public double distanceKm(GeoCoordinate from, GeoCoordinate to) {
        try {
            String url = "%s/route/v1/driving/%f,%f;%f,%f?overview=false"
                    .formatted(osrmBaseUrl, from.longitude(), from.latitude(),
                               to.longitude(), to.latitude());
            @SuppressWarnings("unchecked")
            var response = restTemplate.getForObject(url, java.util.Map.class);
            if (response != null && "Ok".equals(response.get("code"))) {
                @SuppressWarnings("unchecked")
                var routes = (List<java.util.Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    Number distance = (Number) routes.get(0).get("distance");
                    return distance.doubleValue() / 1000.0;
                }
            }
        } catch (Exception ex) {
            log.warn("OSRM unavailable, falling back to Haversine: {}", ex.getMessage());
        }
        return fallback.distanceKm(from, to);
    }

    @Override
    public double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh) {
        try {
            String url = "%s/route/v1/driving/%f,%f;%f,%f?overview=false"
                    .formatted(osrmBaseUrl, from.longitude(), from.latitude(),
                               to.longitude(), to.latitude());
            @SuppressWarnings("unchecked")
            var response = restTemplate.getForObject(url, java.util.Map.class);
            if (response != null && "Ok".equals(response.get("code"))) {
                @SuppressWarnings("unchecked")
                var routes = (List<java.util.Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    Number duration = (Number) routes.get(0).get("duration");
                    return duration.doubleValue() / 60.0;
                }
            }
        } catch (Exception ex) {
            log.warn("OSRM unavailable for duration, falling back: {}", ex.getMessage());
        }
        return fallback.durationMinutes(from, to, avgSpeedKmh);
    }

    @Override
    public DistanceMatrix buildMatrix(GeoCoordinate depot, List<GeoCoordinate> locations, double avgSpeedKmh) {
        // For matrix queries, OSRM /table endpoint is ideal but requires batch support.
        // Delegate to default implementation which calls distanceKm pairwise.
        return DistanceProvider.super.buildMatrix(depot, locations, avgSpeedKmh);
    }
}
