package com.leostar.wealthpilot.service;

import com.leostar.wealthpilot.model.ProjectionRequest;
import com.leostar.wealthpilot.model.ProjectionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class GoalProjectionServiceTest {

    @Autowired
    private GoalProjectionService goalProjectionService;

    @Test
    public void testProjectionCase1() {
        // P = 10000, r = 10%, years = 10
        ProjectionRequest request = new ProjectionRequest();
        request.setMonthlyInvestmentAmount(10000.0);
        request.setInvestmentHorizonYears(10);
        
        // 10% annual return
        Map<String, Integer> allocation = new HashMap<>();
        allocation.put("equityPct", 100); // 12%
        // We want blended return to be exactly 10%.
        // 12x + 7y + 8z = 10, x+y+z=1
        // Let's just create an allocation that yields 10%.
        // Equity 60% (12*0.6=7.2), Debt 40% (7*0.4=2.8) -> 10%
        allocation.put("equityPct", 60);
        allocation.put("debtPct", 40);
        allocation.put("goldPct", 0);
        request.setAllocation(allocation);

        ProjectionResponse response = goalProjectionService.calculateProjection(request);
        
        long expectedFV = 2050000; // ~ 20.5 lakh
        long actualFV = response.getProjectedCorpus();
        double variance = Math.abs((double)(actualFV - expectedFV) / expectedFV);
        
        assertTrue(variance <= 0.05, "Variance is too high: " + actualFV + " vs expected " + expectedFV);
    }

    @Test
    public void testProjectionCase2() {
        // P = 5000, r = 8%, years = 5
        ProjectionRequest request = new ProjectionRequest();
        request.setMonthlyInvestmentAmount(5000.0);
        request.setInvestmentHorizonYears(5);
        
        Map<String, Integer> allocation = new HashMap<>();
        // We want blended return to be 8%. 
        // Gold is 8%.
        allocation.put("equityPct", 0);
        allocation.put("debtPct", 0);
        allocation.put("goldPct", 100);
        request.setAllocation(allocation);

        ProjectionResponse response = goalProjectionService.calculateProjection(request);
        
        long expectedFV = 370000; // ~ 3.7 lakh
        long actualFV = response.getProjectedCorpus();
        double variance = Math.abs((double)(actualFV - expectedFV) / expectedFV);
        
        assertTrue(variance <= 0.05, "Variance is too high: " + actualFV + " vs expected " + expectedFV);
    }
}
