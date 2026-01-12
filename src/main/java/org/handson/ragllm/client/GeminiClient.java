package org.handson.ragllm.client;

import org.handson.ragllm.config.GeminiConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final GeminiConfig config;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder().build();
    }

    public float[] getEmbedding(String text) {
        try {
            // שימוש ב-v1beta עם המפתח החדש
            String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + config.getApiKey();

            Map<String, Object> requestBody = Map.of(
                    "model", "models/text-embedding-004",
                    "content", Map.of("parts", List.of(Map.of("text", text)))
            );

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
                        List<Double> values = (List<Double>) embedding.get("values");
                        float[] fValues = new float[values.size()];
                        for (int i = 0; i < values.size(); i++) fValues[i] = values.get(i).floatValue();
                        return fValues;
                    }).block();
        } catch (Exception e) {
            return new float[768];
        }
    }

    public String generateAnswer(String question, String context) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5:generateText?key=" + config.getApiKey();

            String promptText = String.format(
                    "ענה על השאלה רק מתוך הטקסט הבא:\n\n--- PDF CHUNK ---\n%s\n------------------\n\nשאלה:\n%s\n\nאם אין תשובה בטקסט, אמור שאין מידע.",
                    context, question
            );

            Map<String, Object> requestBody = Map.of(
                    "prompt", Map.of("text", promptText),
                    "temperature", 0.0
            );

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map> candidates = (List<Map>) response.get("candidates");
                        return (String) candidates.get(0).get("output");
                    })
                    .block();

        } catch (Exception e) {
            return "Error from Gemini: " + e.getMessage();
        }
    }

}