package com.leostar.wealthpilot.service;

import com.leostar.wealthpilot.client.AiAdvisorClient;
import com.leostar.wealthpilot.model.RecommendationRequest;
import com.leostar.wealthpilot.model.RecommendationResponse;
import com.leostar.wealthpilot.repository.FundDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PortfolioAdvisorServiceTest {

    @Mock
    private AiAdvisorClient aiAdvisorClient;

    @Autowired
    private FundDataRepository fundDataRepository;

    private PortfolioAdvisorService portfolioAdvisorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        portfolioAdvisorService = new PortfolioAdvisorService(fundDataRepository, aiAdvisorClient);
    }

    @Test
    public void testValidAiResponse() {
        String validJson = "{\"allocation\":{\"equityPct\":60,\"debtPct\":30,\"goldPct\":10},\"recommendedFunds\":[{\"fundId\":\"EQ-001\",\"name\":\"Nifty 50 Index Fund\",\"allocationPct\":60},{\"fundId\":\"DT-001\",\"name\":\"Short Duration Debt Fund\",\"allocationPct\":30},{\"fundId\":\"GD-001\",\"name\":\"Gold ETF Fund of Fund\",\"allocationPct\":10}],\"rationale\":\"Test rationale\"}";
        when(aiAdvisorClient.getRecommendation(anyString())).thenReturn(validJson);

        RecommendationRequest request = new RecommendationRequest();
        request.setRiskCategory("MODERATE");
        request.setGoalType("RETIREMENT");
        request.setInvestmentHorizonYears(10);
        request.setMonthlyInvestmentAmount(10000.0);

        RecommendationResponse response = portfolioAdvisorService.recommend(request);

        assertNotNull(response);
        assertFalse(response.isUsedFallback());
        assertEquals(60, response.getAllocation().get("equityPct"));
        assertEquals("Test rationale", response.getRationale());
        verify(aiAdvisorClient, times(1)).getRecommendation(anyString());
    }

    @Test
    public void testRetryOnMalformedResponse() {
        String malformedJson = "This is not json";
        String validJson = "{\"allocation\":{\"equityPct\":55,\"debtPct\":35,\"goldPct\":10},\"recommendedFunds\":[{\"fundId\":\"EQ-002\",\"name\":\"Bluechip Growth Fund\",\"allocationPct\":55},{\"fundId\":\"DT-002\",\"name\":\"Corporate Bond Fund\",\"allocationPct\":35},{\"fundId\":\"GD-002\",\"name\":\"Sovereign Gold Bond Linked Fund\",\"allocationPct\":10}],\"rationale\":\"Retry rationale\"}";
        
        when(aiAdvisorClient.getRecommendation(anyString()))
            .thenReturn(malformedJson)
            .thenReturn(validJson);

        RecommendationRequest request = new RecommendationRequest();
        request.setRiskCategory("MODERATE");
        request.setGoalType("RETIREMENT");
        request.setInvestmentHorizonYears(10);
        request.setMonthlyInvestmentAmount(10000.0);

        RecommendationResponse response = portfolioAdvisorService.recommend(request);

        assertNotNull(response);
        assertFalse(response.isUsedFallback());
        assertEquals(55, response.getAllocation().get("equityPct"));
        assertEquals("Retry rationale", response.getRationale());
        verify(aiAdvisorClient, times(2)).getRecommendation(anyString());
    }

    @Test
    public void testFallbackOnTwoFailures() {
        String malformedJson = "This is not json either";
        
        when(aiAdvisorClient.getRecommendation(anyString())).thenReturn(malformedJson);

        RecommendationRequest request = new RecommendationRequest();
        request.setRiskCategory("AGGRESSIVE");
        request.setGoalType("RETIREMENT");
        request.setInvestmentHorizonYears(10);
        request.setMonthlyInvestmentAmount(10000.0);

        RecommendationResponse response = portfolioAdvisorService.recommend(request);

        assertNotNull(response);
        assertTrue(response.isUsedFallback());
        assertEquals(80, response.getAllocation().get("equityPct"));
        assertEquals(15, response.getAllocation().get("debtPct"));
        assertEquals(5, response.getAllocation().get("goldPct"));
        assertNotNull(response.getRationale());
        assertTrue(response.getRationale().contains("fallback"));
        verify(aiAdvisorClient, times(2)).getRecommendation(anyString());
    }
}
