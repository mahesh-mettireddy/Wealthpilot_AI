package com.leostar.wealthpilot.service;

import com.leostar.wealthpilot.model.RiskProfileRequest;
import com.leostar.wealthpilot.model.RiskProfileResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RiskProfilerService {

    public RiskProfileResponse calculateRiskProfile(RiskProfileRequest request) {
        int ageFactor = getAgeFactor(request.getAge());
        int horizonFactor = getHorizonFactor(request.getInvestmentHorizonYears());
        int riskComfortFactor = getRiskComfortFactor(request.getRiskComfort());
        int goalFactor = getGoalFactor(request.getGoalType());

        double scoreDouble = (ageFactor * 0.25) + (horizonFactor * 0.35) + (riskComfortFactor * 0.30) + (goalFactor * 0.10);
        int score = (int) Math.round(scoreDouble);

        String category;
        if (score <= 35) {
            category = "CONSERVATIVE";
        } else if (score <= 70) {
            category = "MODERATE";
        } else {
            category = "AGGRESSIVE";
        }

        RiskProfileResponse response = new RiskProfileResponse();
        response.setRiskScore(score);
        response.setRiskCategory(category);
        
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("ageFactor", ageFactor);
        breakdown.put("horizonFactor", horizonFactor);
        breakdown.put("riskComfortFactor", riskComfortFactor);
        breakdown.put("goalFactor", goalFactor);
        
        response.setFactorBreakdown(breakdown);
        return response;
    }

    private int getAgeFactor(int age) {
        if (age <= 30) return 100;
        if (age <= 45) return 70;
        if (age <= 60) return 40;
        return 15;
    }

    private int getHorizonFactor(int horizon) {
        if (horizon >= 15) return 100;
        if (horizon >= 7) return 70;
        if (horizon >= 3) return 40;
        return 10;
    }

    private int getRiskComfortFactor(String riskComfort) {
        switch (riskComfort) {
            case "HIGH": return 100;
            case "MEDIUM": return 55;
            case "LOW": return 15;
            default: throw new IllegalArgumentException("Unknown risk comfort: " + riskComfort);
        }
    }

    private int getGoalFactor(String goalType) {
        switch (goalType) {
            case "WEALTH_GROWTH": return 100;
            case "CHILD_EDUCATION":
            case "RETIREMENT": return 60;
            case "HOME_PURCHASE": return 40;
            case "EMERGENCY_FUND": return 10;
            default: throw new IllegalArgumentException("Unknown goal type: " + goalType);
        }
    }
}
