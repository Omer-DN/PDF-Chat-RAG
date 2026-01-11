package org.handson.ragllm.client;

import org.handson.ragllm.config.GeminiConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final GeminiConfig config;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl() != null ? config.getBaseUrl() : "https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** * שליפת embedding מ-Gemini
     * שים לב לשימוש ב-v1beta וב-embedContent
     */
    public float[] getEmbedding(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "models/text-embedding-004",
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/text-embedding-004:embedContent")
                            .queryParam("key", config.getApiKey())
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
                        List<Number> values = (List<Number>) embeddingMap.get("values");

                        float[] fValues = new float[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            fValues[i] = values.get(i).floatValue();
                        }
                        return fValues;
                    })
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Gemini Embedding Error: " + e.getResponseBodyAsString());
            return new float[768];
        } catch (Exception e) {
            System.err.println("General Embedding Error: " + e.getMessage());
            return new float[768];
        }
    }

    /** * יצירת תשובה (Chat)
     * שים לב לשימוש ב-v1 וב-generateContent
     */
    public String generateAnswer(String question, String context) {
        try {
            // בניית Prompt חזק שימנע הזיות (Hallucinations)
            String promptText = String.format(
                    "You are a professional assistant. Answer the question based ONLY on the provided context.\n\n" +
                            "CONTEXT:\n%s\n\n" +
                            "QUESTION:\n%s", context, question);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", promptText)))
                    )
            );

            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            // שים לב: חייב v1beta עבור gemini-1.5-flash
                            .path("/v1beta/models/gemini-1.5-flash:generateContent")
                            .queryParam("key", config.getApiKey())
                            .build())
                    .bodyValue(requestBody)
                    // ... המשך הקוד
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return "No answer generated.";

                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        return (String) parts.get(0).get("text");
                    })
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Gemini Chat Error: " + e.getResponseBodyAsString());
            return "Error from Gemini: " + e.getStatusText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}