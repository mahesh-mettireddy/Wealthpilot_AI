package com.leostar.wealthpilot.model;

import java.util.List;
import java.util.Map;

public class RecommendationResponse {

    private Map<String, Integer> allocation;
    private List<RecommendedFund> recommendedFunds;
    private String rationale;
    private boolean usedFallback = false;

    // Getters and Setters
    public Map<String, Integer> getAllocation() { return allocation; }
    public void setAllocation(Map<String, Integer> allocation) { this.allocation = allocation; }

    public List<RecommendedFund> getRecommendedFunds() { return recommendedFunds; }
    public void setRecommendedFunds(List<RecommendedFund> recommendedFunds) { this.recommendedFunds = recommendedFunds; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    
    public boolean isUsedFallback() { return usedFallback; }
    public void setUsedFallback(boolean usedFallback) { this.usedFallback = usedFallback; }

    public static class RecommendedFund {
        private String fundId;
        private String name;
        private int allocationPct;
        
        public RecommendedFund() {}
        public RecommendedFund(String fundId, String name, int allocationPct) {
            this.fundId = fundId;
            this.name = name;
            this.allocationPct = allocationPct;
        }

        public String getFundId() { return fundId; }
        public void setFundId(String fundId) { this.fundId = fundId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAllocationPct() { return allocationPct; }
        public void setAllocationPct(int allocationPct) { this.allocationPct = allocationPct; }
    }
}
