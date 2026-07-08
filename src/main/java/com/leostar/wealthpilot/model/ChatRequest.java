package com.leostar.wealthpilot.model;

import java.util.List;

public class ChatRequest {

    private String message;
    private List<ChatMessage> history;
    private RiskProfileResponse riskProfile;
    private RecommendationResponse recommendedPortfolio;
    private ProjectionResponse projection;

    public static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }
    public RiskProfileResponse getRiskProfile() { return riskProfile; }
    public void setRiskProfile(RiskProfileResponse riskProfile) { this.riskProfile = riskProfile; }
    public RecommendationResponse getRecommendedPortfolio() { return recommendedPortfolio; }
    public void setRecommendedPortfolio(RecommendationResponse recommendedPortfolio) { this.recommendedPortfolio = recommendedPortfolio; }
    public ProjectionResponse getProjection() { return projection; }
    public void setProjection(ProjectionResponse projection) { this.projection = projection; }
}
