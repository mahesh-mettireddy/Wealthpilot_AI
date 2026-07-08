package com.leostar.wealthpilot.service;

import com.leostar.wealthpilot.model.RiskProfileRequest;
import com.leostar.wealthpilot.model.RiskProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RiskProfilerServiceTest {

    @Autowired
    private RiskProfilerService riskProfilerService;

    @Test
    public void testCaseA() {
        RiskProfileRequest request = new RiskProfileRequest();
        request.setAge(25);
        request.setInvestmentHorizonYears(20);
        request.setRiskComfort("HIGH");
        request.setGoalType("WEALTH_GROWTH");
        
        RiskProfileResponse response = riskProfilerService.calculateRiskProfile(request);
        assertEquals("AGGRESSIVE", response.getRiskCategory());
    }

    @Test
    public void testCaseB() {
        RiskProfileRequest request = new RiskProfileRequest();
        request.setAge(55);
        request.setInvestmentHorizonYears(3);
        request.setRiskComfort("LOW");
        request.setGoalType("EMERGENCY_FUND");
        
        RiskProfileResponse response = riskProfilerService.calculateRiskProfile(request);
        assertEquals("CONSERVATIVE", response.getRiskCategory());
    }

    @Test
    public void testCaseC() {
        RiskProfileRequest request = new RiskProfileRequest();
        request.setAge(35);
        request.setInvestmentHorizonYears(10);
        request.setRiskComfort("MEDIUM");
        request.setGoalType("RETIREMENT");
        
        RiskProfileResponse response = riskProfilerService.calculateRiskProfile(request);
        assertEquals("MODERATE", response.getRiskCategory());
    }
}
