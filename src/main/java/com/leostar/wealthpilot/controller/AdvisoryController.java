package com.leostar.wealthpilot.controller;

import com.leostar.wealthpilot.model.*;
import com.leostar.wealthpilot.service.GoalProjectionService;
import com.leostar.wealthpilot.service.PortfolioAdvisorService;
import com.leostar.wealthpilot.service.RiskProfilerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdvisoryController {

    private final RiskProfilerService riskProfilerService;
    private final GoalProjectionService goalProjectionService;
    private final PortfolioAdvisorService portfolioAdvisorService;

    @Autowired
    public AdvisoryController(RiskProfilerService riskProfilerService, GoalProjectionService goalProjectionService, PortfolioAdvisorService portfolioAdvisorService) {
        this.riskProfilerService = riskProfilerService;
        this.goalProjectionService = goalProjectionService;
        this.portfolioAdvisorService = portfolioAdvisorService;
    }

    @PostMapping("/risk-profile")
    public ResponseEntity<Map<String, Object>> getRiskProfile(@Valid @RequestBody RiskProfileRequest request) {
        RiskProfileResponse response = riskProfilerService.calculateRiskProfile(request);
        return ResponseEntity.ok(successResponse(response));
    }

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendation(@Valid @RequestBody RecommendationRequest request) {
        RecommendationResponse response = portfolioAdvisorService.recommend(request);
        return ResponseEntity.ok(successResponse(response));
    }

    @PostMapping("/project")
    public ResponseEntity<Map<String, Object>> getProjection(@Valid @RequestBody ProjectionRequest request) {
        ProjectionResponse response = goalProjectionService.calculateProjection(request);
        return ResponseEntity.ok(successResponse(response));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(successResponse(Map.of("status", "UP")));
    }

    private Map<String, Object> successResponse(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("data", data);
        map.put("error", null);
        return map;
    }
}
