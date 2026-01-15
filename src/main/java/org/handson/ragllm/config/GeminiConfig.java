package org.handson.ragllm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {
    private String apiKey;
    private String baseUrl;
    private String generateModel = "gemini-1.5-flash"; // מודל ברירת מחדל ל-generateContent
    private String embeddingModel = "text-embedding-004"; // מודל ל-embeddings
    private String apiVersion = "v1beta"; // גרסת API - v1 או v1beta
    
    /**
     * בונה את ה-URL המלא עם ה-base URL עבור generateContent
     */
    public String getFullBaseUrl() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "https://generativelanguage.googleapis.com/" + apiVersion + "/models/";
        }
        // הסרת / בסוף אם קיים
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        // נוודא שזה כולל את /{version}/models/
        if (!url.contains("/" + apiVersion + "/models/")) {
            url = url + "/" + apiVersion + "/models/";
        }
        return url;
    }
    
    /**
     * מחזיר את שם המודל המלא עם הקידומת models/
     */
    public String getFullModelName(String modelId) {
        // ב-v1beta, המודלים לפעמים דורשים את הקידומת models/
        if (modelId.startsWith("models/")) {
            return modelId;
        }
        return "models/" + modelId;
    }
    
    /**
     * בונה את ה-URL המלא עם ה-base URL עבור embeddings (תמיד v1)
     */
    public String getEmbeddingBaseUrl() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "https://generativelanguage.googleapis.com/v1/models/";
        }
        // הסרת / בסוף אם קיים
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        // embeddings תמיד משתמשים ב-v1
        if (!url.contains("/v1/models/")) {
            url = url + "/v1/models/";
        }
        return url;
    }
}
