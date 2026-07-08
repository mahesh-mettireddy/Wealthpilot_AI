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

@Component
public class GeminiAdvisorClient implements AiAdvisorClient {

    @Value("${spring.ai.gemini.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String resolvedModelName = null;

    private synchronized String getModelName() {
        if (resolvedModelName != null) return resolvedModelName;
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("models")) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                
                // First pass: try to find a 1.5 flash model
                for (Map<String, Object> model : models) {
                    String name = (String) model.get("name");
                    List<String> methods = (List<String>) model.get("supportedGenerationMethods");
                    if (name != null && name.contains("gemini-1.5-flash") && methods != null && methods.contains("generateContent")) {
                        resolvedModelName = name.replace("models/", "");
                        return resolvedModelName;
                    }
                }
                
                // Second pass: try to find ANY gemini model supporting generateContent
                for (Map<String, Object> model : models) {
                    String name = (String) model.get("name");
                    List<String> methods = (List<String>) model.get("supportedGenerationMethods");
                    if (name != null && name.contains("gemini") && methods != null && methods.contains("generateContent")) {
                        resolvedModelName = name.replace("models/", "");
                        return resolvedModelName;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch models list: " + e.getMessage());
        }
        // Fallback to standard alias
        resolvedModelName = "gemini-1.5-flash-latest";
        return resolvedModelName;
    }

    @Override
    public String getRecommendation(String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty() || "REPLACE_ME".equals(apiKey)) {
            throw new RuntimeException("Gemini API Key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            String model = getModelName();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> respBody = response.getBody();
            if (respBody != null && respBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) respBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            throw new RuntimeException("Invalid response format from Gemini API");
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
}
