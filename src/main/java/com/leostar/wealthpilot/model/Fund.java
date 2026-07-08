package com.leostar.wealthpilot.model;

import java.util.List;

public class Fund {
    private String fundId;
    private String name;
    private String category;
    private String subCategory;
    private double historicalAnnualReturnPct;
    private String riskRating;
    private List<String> suitableFor;
    private double expenseRatioPct;

    // Getters and Setters
    public String getFundId() { return fundId; }
    public void setFundId(String fundId) { this.fundId = fundId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public double getHistoricalAnnualReturnPct() { return historicalAnnualReturnPct; }
    public void setHistoricalAnnualReturnPct(double historicalAnnualReturnPct) { this.historicalAnnualReturnPct = historicalAnnualReturnPct; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }

    public List<String> getSuitableFor() { return suitableFor; }
    public void setSuitableFor(List<String> suitableFor) { this.suitableFor = suitableFor; }

    public double getExpenseRatioPct() { return expenseRatioPct; }
    public void setExpenseRatioPct(double expenseRatioPct) { this.expenseRatioPct = expenseRatioPct; }
}
