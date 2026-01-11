package org.handson.ragllm.client;

import org.handson.ragllm.config.GeminiConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final GeminiConfig config;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
        // הדפסה לביקורת - תראה ב-Console אם זה מדפיס null או כתובת אמיתית
        System.out.println("DEBUG: Gemini Base URL is: " + config.getBaseUrl());

        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl() != null ? config.getBaseUrl() : "https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** שליפת embedding מ-Gemini */
    public float[] getEmbedding(String text) {
        try {
            // גוגל דורשת מבנה עטוף ב-content -> parts -> text
            Map<String, Object> requestBody = Map.of(
                    "model", "models/text-embedding-004",
                    "content", Map.of(
                            "parts", List.of(
                                    Map.of("text", text)
                            )
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
                        // שליפת הוקטור מתוך התגובה
                        Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
                        List<Number> values = (List<Number>) embeddingMap.get("values");

                        float[] fValues = new float[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            fValues[i] = values.get(i).floatValue();
                        }
                        return fValues;
                    })
                    .block();
        } catch (Exception e) {
            // הדפסת השגיאה המלאה לטרמינל כדי לראות מה גוגל אומרת ב-Body
            System.err.println("Gemini Error: " + e.getMessage());
            return new float[768]; // הגודל הסטנדרטי של מודל 004
        }
    }
    /** יצירת תשובה (Chat) */
    public String generateAnswer(String question, String context) {
        try {
            String promptText = "Context: " + context + "\n\nQuestion: " + question;

            // מבנה JSON עבור Gemini Generate Content
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", promptText)))
                    )
            );

            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-1.5-flash:generateContent")
                            .queryParam("key", config.getApiKey())
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                        return parts.get(0).get("text");
                    })
                    .block();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}