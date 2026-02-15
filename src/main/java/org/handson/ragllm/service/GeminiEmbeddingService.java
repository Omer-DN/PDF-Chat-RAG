package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.config.GeminiConfig;
import org.springframework.stereotype.Service;

@Service
public class GeminiEmbeddingService {

    private final GeminiClient geminiClient;
    private final GeminiConfig geminiConfig;

    // הזרקה דרך ה-Constructor - הדרך הנכונה ב-Spring
    // זה מונע את השגיאה "Could not resolve placeholder"
    public GeminiEmbeddingService(GeminiClient geminiClient, GeminiConfig geminiConfig) {
        this.geminiClient = geminiClient;
        this.geminiConfig = geminiConfig;
    }

    public float[] embedText(String text) {
        // אנחנו קוראים לקליינט שבנינו, הוא כבר יודע להשתמש במפתח ובכתובת
        return geminiClient.getEmbeddingForDocument(text);
    }
}