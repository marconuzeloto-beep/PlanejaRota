package com.logicore.planning.infrastructure.distance;

import com.logicore.planning.domain.distance.DistanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DistanceProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(DistanceProviderFactory.class);

    @Value("${app.distance.provider:haversine}")
    private String providerName;

    @Value("${app.distance.cache.enabled:true}")
    private boolean cacheEnabled;

    @Primary
    @Bean("primaryDistanceProvider")
    public DistanceProvider primaryDistanceProvider(
            HaversineDistanceProvider haversine,
            EuclideanDistanceProvider euclidean,
            OsrmDistanceProvider osrm) {

        DistanceProvider base = switch (providerName.toLowerCase()) {
            case "osrm"      -> osrm;
            case "euclidean" -> euclidean;
            default          -> haversine;
        };

        log.info("Distance provider selected: {} (cache: {})", base.getProviderName(), cacheEnabled);

        return cacheEnabled ? new CachedDistanceProvider(base) : base;
    }
}
