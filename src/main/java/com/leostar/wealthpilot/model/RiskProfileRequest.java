package com.leostar.wealthpilot.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class RiskProfileRequest {

    @NotNull(message = "Missing required field: age")
    @Min(value = 18, message = "age must be between 18 and 75")
    @Max(value = 75, message = "age must be between 18 and 75")
    private Integer age;

    @NotNull(message = "Missing required field: monthlyIncome")
    @Positive(message = "monthlyIncome must be greater than 0")
    private Double monthlyIncome;

    @NotNull(message = "Missing required field: goalType")
    @Pattern(regexp = "^(RETIREMENT|HOME_PURCHASE|CHILD_EDUCATION|WEALTH_GROWTH|EMERGENCY_FUND)$", message = "goalType must be one of the allowed values")
    private String goalType;

    @NotNull(message = "Missing required field: investmentHorizonYears")
    @Min(value = 1, message = "investmentHorizonYears must be between 1 and 40")
    @Max(value = 40, message = "investmentHorizonYears must be between 1 and 40")
    private Integer investmentHorizonYears;

    @NotNull(message = "Missing required field: monthlyInvestmentAmount")
    @Positive(message = "monthlyInvestmentAmount must be greater than 0")
    private Double monthlyInvestmentAmount;

    @NotNull(message = "Missing required field: riskComfort")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "riskComfort must be one of the allowed values")
    private String riskComfort;

    @Min(value = 0, message = "existingInvestmentsPctEquity must be between 0 and 100")
    @Max(value = 100, message = "existingInvestmentsPctEquity must be between 0 and 100")
    private Integer existingInvestmentsPctEquity = 0;

    // Getters and Setters
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public Integer getInvestmentHorizonYears() { return investmentHorizonYears; }
    public void setInvestmentHorizonYears(Integer investmentHorizonYears) { this.investmentHorizonYears = investmentHorizonYears; }

    public Double getMonthlyInvestmentAmount() { return monthlyInvestmentAmount; }
    public void setMonthlyInvestmentAmount(Double monthlyInvestmentAmount) { this.monthlyInvestmentAmount = monthlyInvestmentAmount; }

    public String getRiskComfort() { return riskComfort; }
    public void setRiskComfort(String riskComfort) { this.riskComfort = riskComfort; }

    public Integer getExistingInvestmentsPctEquity() { return existingInvestmentsPctEquity; }
    public void setExistingInvestmentsPctEquity(Integer existingInvestmentsPctEquity) { this.existingInvestmentsPctEquity = existingInvestmentsPctEquity; }
}
