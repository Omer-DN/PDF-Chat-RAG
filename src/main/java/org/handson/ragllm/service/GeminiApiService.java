package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiApiService {

    private final GeminiClient geminiClient;

    public GeminiApiService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * הופך טקסט לוקטור (Embedding) דרך הקליינט
     */
    public float[] getEmbedding(String text) {
        return geminiClient.getEmbedding(text);
    }

    /**
     * מייצר תשובה תוך שימוש ב-Prompt Engineering מקצועי
     */
    public String generateAnswerFromContext(String question, String context) {
        String prompt = String.format("""
            You are a helpful assistant. Use the provided context to answer the user's question accurately.
            If the answer is not in the context, say that you don't know based on the document.
            
            Context:
            %s
            
            Question:
            %s
            
            Answer:
            """, context, question);

        // קריאה לקליינט שיבצע את ה-Generate Content
        return geminiClient.generateContent(prompt);
    }
}