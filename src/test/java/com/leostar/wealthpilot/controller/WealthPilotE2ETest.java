package com.leostar.wealthpilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leostar.wealthpilot.client.AiAdvisorClient;
import com.leostar.wealthpilot.model.ProjectionRequest;
import com.leostar.wealthpilot.model.RecommendationRequest;
import com.leostar.wealthpilot.model.RiskProfileRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WealthPilotE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiAdvisorClient aiAdvisorClient;

    @Test
    public void testFullFlowEndToEnd() throws Exception {
        // Step 1: Risk Profile
        RiskProfileRequest riskReq = new RiskProfileRequest();
        riskReq.setAge(32);
        riskReq.setMonthlyIncome(85000.0);
        riskReq.setGoalType("RETIREMENT");
        riskReq.setInvestmentHorizonYears(20);
        riskReq.setMonthlyInvestmentAmount(15000.0);
        riskReq.setRiskComfort("MEDIUM");

        String riskResponseStr = mockMvc.perform(post("/api/risk-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(riskReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskCategory").value("AGGRESSIVE"))
                .andReturn().getResponse().getContentAsString();

        // Step 2: Recommend
        String validJson = "{\"allocation\":{\"equityPct\":80,\"debtPct\":15,\"goldPct\":5},\"recommendedFunds\":[{\"fundId\":\"EQ-002\",\"name\":\"Bluechip Growth Fund\",\"allocationPct\":80}],\"rationale\":\"Test rationale\"}";
        Mockito.when(aiAdvisorClient.getRecommendation(anyString())).thenReturn(validJson);

        RecommendationRequest recReq = new RecommendationRequest();
        recReq.setRiskCategory("AGGRESSIVE");
        recReq.setGoalType("RETIREMENT");
        recReq.setInvestmentHorizonYears(20);
        recReq.setMonthlyInvestmentAmount(15000.0);

        mockMvc.perform(post("/api/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allocation.equityPct").value(80));

        // Step 3: Project
        ProjectionRequest projReq = new ProjectionRequest();
        projReq.setAllocation(Map.of("equityPct", 55, "debtPct", 35, "goldPct", 10));
        projReq.setMonthlyInvestmentAmount(15000.0);
        projReq.setInvestmentHorizonYears(20);

        mockMvc.perform(post("/api/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.blendedAnnualReturnPct").value(9.85));
    }

    @Test
    public void testValidationErrorEnvelope() throws Exception {
        RiskProfileRequest badReq = new RiskProfileRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/risk-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isString());
    }
}
