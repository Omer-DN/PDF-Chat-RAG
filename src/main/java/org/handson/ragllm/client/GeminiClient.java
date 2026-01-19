package org.handson.ragllm.client;

import org.handson.ragllm.config.GeminiConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;

    public GeminiClient(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }


    public float[] getEmbedding(String text) {
        String modelId = geminiConfig.getEmbeddingModel();
        // embeddings תמיד משתמשים ב-v1 API
        String url = geminiConfig.getEmbeddingBaseUrl() + modelId + ":embedContent?key=" + geminiConfig.getApiKey();

        Map<String, Object> request = Map.of(
                "model", "models/" + modelId,
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        try {
            //System.out.println("DEBUG: Sending Embedding request to: " + url);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response == null || !response.containsKey("embedding")) {
                throw new RuntimeException("Empty response from Gemini API for embedding");
            }

            Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
            List<Double> values = (List<Double>) embeddingMap.get("values");

            float[] floatVector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                floatVector[i] = values.get(i).floatValue();
            }

            return floatVector;
        } catch (Exception e) {
            System.err.println("❌ Embedding Error: " + e.getMessage());
            throw new RuntimeException("Failed to get embedding: " + e.getMessage());
        }
    }

    /**
     * בדיקת המודלים הזמינים
     */
    public void listAvailableModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/" + geminiConfig.getApiVersion() + "/models?key=" + geminiConfig.getApiKey();
            //System.out.println("DEBUG: Fetching available models from: " + url);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("models")) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
                /*System.out.println("Available models:");
                for (Map<String, Object> model : models) {
                    String name = (String) model.get("name");
                    System.out.println("  - " + name);
                }*/
            }
        } catch (Exception e) {
            System.err.println("Failed to list models: " + e.getMessage());
        }
    }

    /**
     * יצירת טקסט (תשובה) מ-Prompt - מותאם ל-v1
     */
    public String generateContent(String prompt) {
        // שימוש במודל מוגדר בקונפיגורציה
        String modelId = geminiConfig.getGenerateModel();
        
        // ננסה כמה וריאציות של שם המודל
        String[] modelVariations = {
            modelId,  // ללא קידומת
            "models/" + modelId,  // עם קידומת
            modelId.replace("-", "_"),  // עם underscore במקום dash
        };
        
        Exception lastException = null;
        
        for (String modelName : modelVariations) {
            try {
                String url = geminiConfig.getFullBaseUrl() + modelName + ":generateContent?key=" + geminiConfig.getApiKey();

                Map<String, Object> request = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(Map.of("text", prompt)))
                        )
                );

                System.out.println("DEBUG: Trying model '" + modelName + "' - URL: " + url);
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

                if (response == null || !response.containsKey("candidates")) {
                    throw new RuntimeException("No candidates in response from Gemini");
                }

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

                // בדיקה אם הוחזר תוכן (לפעמים הרשימה ריקה בגלל חסימת בטיחות)
                if (candidates.isEmpty()) {
                    return "Gemini returned no candidates (Safety block?)";
                }

                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                System.out.println("✅ Successfully used model: " + modelName);
                return (String) parts.get(0).get("text");
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    System.out.println("⚠️ Model '" + modelName + "' not found, trying next variation...");
                    lastException = e;
                    continue; // נסה את הווריאציה הבאה
                }
                // אם זו שגיאה אחרת (לא 404), זרוק אותה
                String errorMessage = String.format("HTTP Error %d: %s", e.getStatusCode().value(), e.getResponseBodyAsString());
                System.err.println("❌ Generation HTTP Error: " + errorMessage);
                throw new RuntimeException("Failed to generate content: " + errorMessage, e);
            } catch (Exception e) {
                System.err.println("⚠️ Error with model '" + modelName + "': " + e.getMessage());
                lastException = e;
                continue; // נסה את הווריאציה הבאה
            }
        }
        
        // אם הגענו לכאן, כל הווריאציות נכשלו
        System.err.println("❌ All model variations failed for: " + modelId);
        
        // נסה להציג את המודלים הזמינים
        System.out.println("Attempting to list available models...");
        listAvailableModels();
        
        if (lastException instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) lastException;
            String errorMessage = String.format("HTTP Error %d: %s", httpEx.getStatusCode().value(), httpEx.getResponseBodyAsString());
            throw new RuntimeException("Model '" + modelId + "' not found after trying all variations. " +
                "Please check available models above or try a different model name.", httpEx);
        }
        
        throw new RuntimeException("Failed to generate content with model '" + modelId + "' after trying all variations.", lastException);
    }
}