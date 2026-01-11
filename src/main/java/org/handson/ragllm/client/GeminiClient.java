package org.handson.ragllm.client;

import org.handson.ragllm.config.GeminiConfig;
import org.springframework.stereotype.Component;

// אפשר להחליף ל-WebClient כשנחבר אמיתי
@Component
public class GeminiClient {

    private final GeminiConfig config;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
    }

    /**
     * מחזיר embedding של טקסט
     * כרגע Mock – מחזיר float[] אפסיים
     */
    public float[] getEmbedding(String text) {
        // בעתיד כאן קריאה אמיתית ל-Gemini API עם WebClient
        int dim = 1536; // מימד סטנדרטי ל-embeddings
        return new float[dim]; // dummy zeros כרגע
    }
}
