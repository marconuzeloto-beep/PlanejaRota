package com.planejarota.infrastructure.config;

import com.planejarota.domain.service.RouteOptimizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os Domain Services como beans Spring.
 * Os Domain Services são pure Java — sem dependências de framework.
 * Este arquivo é o único ponto de acoplamento entre domínio e Spring.
 */
@Configuration
public class DomainConfig {

    @Bean
    public RouteOptimizationService routeOptimizationService() {
        return new RouteOptimizationService();
    }
}
