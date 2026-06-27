package com.logicore.planning.api;

import com.logicore.planning.domain.service.AdvisorRequest;
import com.logicore.planning.domain.service.StrategyAdvisor;
import com.logicore.planning.domain.service.StrategyRecommendation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/advisor")
public class AdvisorController {

    private final StrategyAdvisor advisor;

    public AdvisorController(StrategyAdvisor advisor) {
        this.advisor = advisor;
    }

    public record RecommendRequest(
            @Min(1) int orderCount,
            boolean hasTimeWindows,
            boolean hasHazardousCargo,
            boolean hasMultipleVehicles,
            @PositiveOrZero long slaMsLimit
    ) {}

    @PostMapping("/recommend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StrategyRecommendation> recommend(@Valid @RequestBody RecommendRequest req) {
        var request = new AdvisorRequest(
                req.orderCount(), req.hasTimeWindows(),
                req.hasHazardousCargo(), req.hasMultipleVehicles(), req.slaMsLimit());
        return ResponseEntity.ok(advisor.recommend(request));
    }
}
