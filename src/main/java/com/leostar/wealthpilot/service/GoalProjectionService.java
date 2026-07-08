package com.leostar.wealthpilot.service;

import com.leostar.wealthpilot.model.ProjectionRequest;
import com.leostar.wealthpilot.model.ProjectionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoalProjectionService {

    public ProjectionResponse calculateProjection(ProjectionRequest request) {
        Map<String, Integer> allocation = request.getAllocation();
        int equityPct = allocation.getOrDefault("equityPct", 0);
        int debtPct = allocation.getOrDefault("debtPct", 0);
        int goldPct = allocation.getOrDefault("goldPct", 0);

        // Calculate blended annual return
        double blendedAnnualReturnPct = (equityPct * 12.0 + debtPct * 7.0 + goldPct * 8.0) / 100.0;
        
        double P = request.getMonthlyInvestmentAmount();
        double r = (blendedAnnualReturnPct / 100.0) / 12.0;
        int years = request.getInvestmentHorizonYears();

        List<ProjectionResponse.YearlyProjection> yearlyBreakdown = new ArrayList<>();

        for (int year = 1; year <= years; year++) {
            int n = year * 12;
            double FV = P * ((Math.pow(1 + r, n) - 1) / r) * (1 + r);
            yearlyBreakdown.add(new ProjectionResponse.YearlyProjection(year, Math.round(FV)));
        }

        long finalCorpus = yearlyBreakdown.isEmpty() ? 0 : yearlyBreakdown.get(yearlyBreakdown.size() - 1).getCorpus();
        long totalInvested = Math.round(P * years * 12);

        ProjectionResponse response = new ProjectionResponse();
        // Return rounded to 2 decimal places for return pct
        response.setBlendedAnnualReturnPct(Math.round(blendedAnnualReturnPct * 100.0) / 100.0);
        response.setProjectedCorpus(finalCorpus);
        response.setTotalInvested(totalInvested);
        response.setYearlyBreakdown(yearlyBreakdown);

        return response;
    }
}
