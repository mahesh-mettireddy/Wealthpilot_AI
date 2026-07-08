package com.leostar.wealthpilot.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class ProjectionRequest {

    @NotNull(message = "allocation is required")
    private Map<String, Integer> allocation;

    @NotNull(message = "monthlyInvestmentAmount is required")
    private Double monthlyInvestmentAmount;

    @NotNull(message = "investmentHorizonYears is required")
    private Integer investmentHorizonYears;

    // Getters and Setters
    public Map<String, Integer> getAllocation() { return allocation; }
    public void setAllocation(Map<String, Integer> allocation) { this.allocation = allocation; }

    public Double getMonthlyInvestmentAmount() { return monthlyInvestmentAmount; }
    public void setMonthlyInvestmentAmount(Double monthlyInvestmentAmount) { this.monthlyInvestmentAmount = monthlyInvestmentAmount; }

    public Integer getInvestmentHorizonYears() { return investmentHorizonYears; }
    public void setInvestmentHorizonYears(Integer investmentHorizonYears) { this.investmentHorizonYears = investmentHorizonYears; }
}
