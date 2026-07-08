package com.leostar.wealthpilot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// @Component - Disabled in favor of GeminiAdvisorClient
public class AnthropicAdvisorClient implements AiAdvisorClient {

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getRecommendation(String prompt) {
        if (apiKey == null || apiKey.isEmpty() || "REPLACE_ME".equals(apiKey)) {
            throw new RuntimeException("Anthropic API Key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
            "model", "claude-3-haiku-20240307",
            "max_tokens", 1024,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.anthropic.com/v1/messages", 
                    entity, 
                    Map.class
            );

            Map<String, Object> respBody = response.getBody();
            if (respBody != null && respBody.containsKey("content")) {
                List<Map<String, Object>> contentList = (List<Map<String, Object>>) respBody.get("content");
                if (!contentList.isEmpty()) {
                    return (String) contentList.get(0).get("text");
                }
            }
            throw new RuntimeException("Invalid response from Anthropic API");
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Anthropic API: " + e.getMessage(), e);
        }
    }
}
