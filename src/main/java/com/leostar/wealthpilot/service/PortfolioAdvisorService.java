package com.leostar.wealthpilot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leostar.wealthpilot.client.AiAdvisorClient;
import com.leostar.wealthpilot.model.Fund;
import com.leostar.wealthpilot.model.RecommendationRequest;
import com.leostar.wealthpilot.model.RecommendationResponse;
import com.leostar.wealthpilot.repository.FundDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioAdvisorService {

    private final FundDataRepository fundDataRepository;
    private final AiAdvisorClient aiAdvisorClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PortfolioAdvisorService(FundDataRepository fundDataRepository, AiAdvisorClient aiAdvisorClient) {
        this.fundDataRepository = fundDataRepository;
        this.aiAdvisorClient = aiAdvisorClient;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        List<Fund> allFunds = fundDataRepository.getAllFunds();
        String contextData = null;
        try {
            contextData = objectMapper.writeValueAsString(allFunds);
        } catch (JsonProcessingException e) {
            contextData = "[]";
        }

        String basePrompt = String.format(
            "You are a WealthPilot AI Advisor.\n" +
            "User Profile: Risk Category: %s, Goal: %s, Horizon: %d years, Monthly Amount: %f\n" +
            "Fund Dataset: %s\n" +
            "Return exactly one JSON object matching this schema, no markdown, no explanation outside JSON:\n" +
            "{\n" +
            "  \"allocation\": {\"equityPct\": 55, \"debtPct\": 35, \"goldPct\": 10},\n" +
            "  \"recommendedFunds\": [{\"fundId\": \"EQ-001\", \"name\": \"...\", \"allocationPct\": 30}],\n" +
            "  \"rationale\": \"...\"\n" +
            "}\n",
            request.getRiskCategory(), request.getGoalType(), request.getInvestmentHorizonYears(), request.getMonthlyInvestmentAmount(), contextData
        );

        RecommendationResponse response = attemptAiCall(basePrompt, allFunds);
        if (response != null) {
            return response;
        }

        // Retry once with stricter instruction
        String retryPrompt = basePrompt + "\nCRITICAL: Return ONLY valid JSON. Your response must start with { and end with }.";
        response = attemptAiCall(retryPrompt, allFunds);
        if (response != null) {
            return response;
        }

        // Fallback
        return generateFallback(request.getRiskCategory(), allFunds);
    }

    private RecommendationResponse attemptAiCall(String prompt, List<Fund> allFunds) {
        try {
            String aiRaw = aiAdvisorClient.getRecommendation(prompt);
            // strip markdown if present
            aiRaw = aiRaw.replace("```json", "").replace("```", "").trim();
            RecommendationResponse response = objectMapper.readValue(aiRaw, RecommendationResponse.class);
            if (validateResponse(response, allFunds)) {
                return response;
            }
        } catch (Exception e) {
            System.err.println("AI call failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private boolean validateResponse(RecommendationResponse response, List<Fund> allFunds) {
        if (response.getAllocation() == null || response.getRecommendedFunds() == null || response.getRationale() == null) {
            return false;
        }
        int eq = response.getAllocation().getOrDefault("equityPct", 0);
        int dt = response.getAllocation().getOrDefault("debtPct", 0);
        int gd = response.getAllocation().getOrDefault("goldPct", 0);
        if (eq + dt + gd != 100) return false;

        Set<String> validFundIds = allFunds.stream().map(Fund::getFundId).collect(Collectors.toSet());
        for (RecommendationResponse.RecommendedFund rf : response.getRecommendedFunds()) {
            if (!validFundIds.contains(rf.getFundId())) {
                return false;
            }
        }
        if (response.getRationale().trim().isEmpty()) return false;
        return true;
    }

    private RecommendationResponse generateFallback(String riskCategory, List<Fund> allFunds) {
        int eqPct = 0, dtPct = 0, gdPct = 0;
        switch (riskCategory) {
            case "CONSERVATIVE": eqPct = 20; dtPct = 70; gdPct = 10; break;
            case "MODERATE": eqPct = 55; dtPct = 35; gdPct = 10; break;
            case "AGGRESSIVE": eqPct = 80; dtPct = 15; gdPct = 5; break;
        }

        RecommendationResponse res = new RecommendationResponse();
        res.setAllocation(Map.of("equityPct", eqPct, "debtPct", dtPct, "goldPct", gdPct));
        res.setRationale("This is a standard model portfolio for your risk profile. (Generated via fallback due to advisory engine unavailability).");
        res.setUsedFallback(true);

        List<RecommendationResponse.RecommendedFund> rfs = new ArrayList<>();
        addTopFunds(rfs, allFunds, "EQUITY", eqPct);
        addTopFunds(rfs, allFunds, "DEBT", dtPct);
        addTopFunds(rfs, allFunds, "GOLD", gdPct);
        res.setRecommendedFunds(rfs);

        return res;
    }

    private void addTopFunds(List<RecommendationResponse.RecommendedFund> list, List<Fund> allFunds, String category, int totalPct) {
        if (totalPct <= 0) return;
        List<Fund> subset = allFunds.stream()
                .filter(f -> category.equals(f.getCategory()))
                .sorted((f1, f2) -> Double.compare(f2.getHistoricalAnnualReturnPct(), f1.getHistoricalAnnualReturnPct()))
                .limit(2)
                .collect(Collectors.toList());

        if (subset.size() == 2) {
            list.add(new RecommendationResponse.RecommendedFund(subset.get(0).getFundId(), subset.get(0).getName(), totalPct / 2));
            list.add(new RecommendationResponse.RecommendedFund(subset.get(1).getFundId(), subset.get(1).getName(), totalPct - (totalPct / 2)));
        } else if (subset.size() == 1) {
            list.add(new RecommendationResponse.RecommendedFund(subset.get(0).getFundId(), subset.get(0).getName(), totalPct));
        }
    }
}
