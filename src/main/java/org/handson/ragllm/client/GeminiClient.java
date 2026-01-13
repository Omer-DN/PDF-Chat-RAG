package org.handson.ragllm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";


    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * הפקת Embedding (וקטור) מטקסט
     */
    public float[] getEmbedding(String text) {
        String url = GEMINI_BASE_URL + "embedding-001:embedContent?key=" + apiKey;

        Map<String, Object> request = Map.of(
                "model", "models/embedding-001",
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            // חילוץ המערך מהמבנה של Google: response.embedding.values
            Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
            List<Double> values = (List<Double>) embeddingMap.get("values");

            float[] floatVector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                floatVector[i] = values.get(i).floatValue();
            }
            return floatVector;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding from Gemini: " + e.getMessage());
        }
    }

    /**
     * יצירת טקסט (תשובה) מ-Prompt
     */
    public String generateContent(String prompt) {
        String url = GEMINI_BASE_URL + "gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            // חילוץ התשובה מהמבנה: candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate content from Gemini: " + e.getMessage());
        }
    }
}