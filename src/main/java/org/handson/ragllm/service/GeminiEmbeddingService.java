package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiEmbeddingService {

    private final GeminiClient geminiClient;

    // הזרקה דרך ה-Constructor - הדרך הנכונה ב-Spring
    // זה מונע את השגיאה "Could not resolve placeholder"
    public GeminiEmbeddingService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
     }

    public float[] embedText(String text) {
        // אנחנו קוראים לקליינט שבנינו, הוא כבר יודע להשתמש במפתח ובכתובת
        return geminiClient.getEmbedding(text);
    }
}