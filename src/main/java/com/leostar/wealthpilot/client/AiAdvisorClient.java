package com.leostar.wealthpilot.client;

public interface AiAdvisorClient {
    /**
     * Calls the AI model with the given prompt and returns the raw response string.
     */
    String getRecommendation(String prompt);
}
