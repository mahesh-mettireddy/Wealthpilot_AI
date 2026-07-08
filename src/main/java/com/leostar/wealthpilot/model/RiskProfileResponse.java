package com.leostar.wealthpilot.model;

import java.util.Map;

public class RiskProfileResponse {

    private int riskScore;
    private String riskCategory;
    private Map<String, Integer> factorBreakdown;

    // Getters and Setters
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public Map<String, Integer> getFactorBreakdown() { return factorBreakdown; }
    public void setFactorBreakdown(Map<String, Integer> factorBreakdown) { this.factorBreakdown = factorBreakdown; }
}
