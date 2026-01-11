package org.handson.ragllm.embedding;

/**
 * חוזה אחיד לכל ספק Embeddings (Mock / Gemini / OpenAI בעתיד)
 */
public interface EmbeddingClient {

    /**
     * מקבל טקסט ומחזיר embedding כ-float[]
     */
    float[] embed(String text);
}
