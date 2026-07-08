package com.leostar.wealthpilot.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RecommendationRequest {

    @NotNull(message = "riskCategory is required")
    @Pattern(regexp = "^(CONSERVATIVE|MODERATE|AGGRESSIVE)$", message = "invalid riskCategory")
    private String riskCategory;

    @NotNull(message = "goalType is required")
    private String goalType;

    @NotNull(message = "investmentHorizonYears is required")
    private Integer investmentHorizonYears;

    @NotNull(message = "monthlyInvestmentAmount is required")
    private Double monthlyInvestmentAmount;

    // Getters and Setters
    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public Integer getInvestmentHorizonYears() { return investmentHorizonYears; }
    public void setInvestmentHorizonYears(Integer investmentHorizonYears) { this.investmentHorizonYears = investmentHorizonYears; }

    public Double getMonthlyInvestmentAmount() { return monthlyInvestmentAmount; }
    public void setMonthlyInvestmentAmount(Double monthlyInvestmentAmount) { this.monthlyInvestmentAmount = monthlyInvestmentAmount; }
}
