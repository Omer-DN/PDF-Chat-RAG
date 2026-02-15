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
            String model = config.getModel() != null ? config.getModel() : "gemini-2.0-flash";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + config.getApiKey();

            String promptText = String.format(
                    "ענה על השאלה רק מתוך הטקסט הבא:\n\n--- PDF CHUNK ---\n%s\n------------------\n\nשאלה:\n%s\n\nאם אין תשובה בטקסט, אמור שאין מידע.",
                    context, question
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText)))),
                    "generationConfig", Map.of("temperature", 0.0)
            );

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map> candidates = (List<Map>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return "לא התקבלה תשובה מהמודל.";
                        Map content = (Map) candidates.get(0).get("content");
                        if (content == null) return "לא התקבלה תשובה מהמודל.";
                        List<Map> parts = (List<Map>) content.get("parts");
                        if (parts == null || parts.isEmpty()) return "לא התקבלה תשובה מהמודל.";
                        return (String) parts.get(0).get("text");
                    })
                    .block();

        } catch (Exception e) {
            return "Error from Gemini: " + e.getMessage();
        }
    }

}