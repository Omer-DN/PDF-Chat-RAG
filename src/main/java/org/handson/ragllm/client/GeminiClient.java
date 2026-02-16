package org.handson.ragllm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.handson.ragllm.config.GeminiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final GeminiConfig config;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder().build();
    }

    /** embedding למקטע מסמך (לשמירה ב-DB) – taskType RETRIEVAL_DOCUMENT */
    public float[] getEmbeddingForDocument(String text) {
        return getEmbedding(text, "RETRIEVAL_DOCUMENT");
    }

    /** embedding לשאילתת משתמש (לחיפוש) – taskType RETRIEVAL_QUERY */
    public float[] getEmbeddingForQuery(String text) {
        return getEmbedding(text, "RETRIEVAL_QUERY");
    }

    private float[] getEmbedding(String text, String taskType) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            log.error("GEMINI_API_KEY is not set");
            throw new RuntimeException("יצירת embedding נכשלה: מפתח API חסר (GEMINI_API_KEY)");
        }
        try {
            String modelName = config.getEmbeddingModel() != null ? config.getEmbeddingModel() : "gemini-embedding-001";
            int dims = config.getEmbeddingDimensions() > 0 ? config.getEmbeddingDimensions() : 768;
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":embedContent?key=" + config.getApiKey();

            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", "models/" + modelName);
            requestBody.put("content", Map.of("parts", List.of(Map.of("text", text != null ? text : ""))));
            requestBody.put("taskType", taskType != null ? taskType : "RETRIEVAL_DOCUMENT");
            requestBody.put("outputDimensionality", dims);

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(this::parseEmbeddingResponse)
                    .block();
        } catch (Exception e) {
            log.error("Gemini getEmbedding failed for text length={}, taskType={}. Error: {}", text != null ? text.length() : 0, taskType, e.getMessage(), e);
            throw new RuntimeException("יצירת embedding נכשלה: " + e.getMessage(), e);
        }
    }

    private float[] parseEmbeddingResponse(Map<String, Object> response) {
        Object emb = response.get("embedding");
        if (emb == null) {
            log.error("Gemini embedding response missing 'embedding' key. Response keys: {}", response.keySet());
            throw new IllegalStateException("Gemini API response missing embedding");
        }
        Map<String, Object> embedding = (Map<String, Object>) emb;
        Object vals = embedding.get("values");
        if (vals == null || !(vals instanceof List)) {
            log.error("Gemini embedding response missing 'values'. embedding keys: {}", embedding != null ? embedding.keySet() : "null");
            throw new IllegalStateException("Gemini API response missing embedding values");
        }
        List<?> values = (List<?>) vals;
        float[] fValues = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Object v = values.get(i);
            if (v instanceof Number) fValues[i] = ((Number) v).floatValue();
            else throw new IllegalStateException("Embedding value not a number: " + v);
        }
        return fValues;
    }

    public String generateAnswer(String question, String context) {
        try {
            String model = config.getModel() != null ? config.getModel() : "gemini-2.0-flash";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + config.getApiKey();

            String promptText = String.format(
                    "ענה על השאלה בהתבסס על הטקסט הבא (קטעים מהמסמך ו/או שאלות ותשובות קודמות עליו):\n\n%s\n\nשאלה:\n%s\n\nאם אין תשובה בטקסט, אמור שאין מידע.",
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

    /**
     * סטרימינג תשובה מ-Gemini – מחזיר Flux של מקטעי טקסט (להצגה הדרגתית).
     */
    public Flux<String> generateAnswerStreaming(String question, String context) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            return Flux.error(new RuntimeException("GEMINI_API_KEY is not set"));
        }
        String model = config.getModel() != null ? config.getModel() : "gemini-2.0-flash";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":streamGenerateContent?key=" + config.getApiKey() + "&alt=sse";

        String promptText = String.format(
                "ענה על השאלה בהתבסס על הטקסט הבא (קטעים מהמסמך ו/או שאלות ותשובות קודמות עליו):\n\n%s\n\nשאלה:\n%s\n\nאם אין תשובה בטקסט, אמור שאין מידע.",
                context, question
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText)))),
                "generationConfig", Map.of("temperature", 0.0)
        );

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .mapNotNull(this::extractTextFromSseData)
                .filter(s -> !s.isEmpty());
    }

    private String extractTextFromSseData(ServerSentEvent<String> event) {
        String data = event != null ? event.data() : null;
        if (data == null || data.isBlank()) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = new ObjectMapper().readValue(data, Map.class);
            List<?> candidates = (List<?>) map.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) ((Map<String, Object>) candidates.get(0)).get("content");
            if (content == null) return null;
            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return null;
            Object text = ((Map<String, Object>) parts.get(0)).get("text");
            return text != null ? text.toString() : null;
        } catch (Exception e) {
            log.trace("Failed to parse SSE chunk: {}", e.getMessage());
            return null;
        }
    }

}