package org.handson.ragllm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiEmbeddingService {

    private final String apiKey;

    public GeminiEmbeddingService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    // כאן יהיה הפונקציה שיוצרת embedding אמיתי
    public byte[] createEmbedding(String text) {
        // TODO: קריאה ל־Gemini API כדי לקבל embedding אמיתי
        // בינתיים מחזיר dummy
        return text.getBytes(); // להחליף ל float[] או byte[] מ-Gemini
    }

    public String getApiKey() {
        return apiKey;
    }
}
