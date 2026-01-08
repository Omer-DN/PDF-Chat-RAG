package org.handson.ragllm.service;

import org.handson.ragllm.config.GeminiConfig;
import org.handson.ragllm.model.PdfChunkEmbedding;
import org.handson.ragllm.repository.PdfChunkEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class PdfChunkEmbeddingService {

    private final PdfChunkEmbeddingRepository repository;
    private final GeminiConfig geminiConfig;

    public PdfChunkEmbeddingService(PdfChunkEmbeddingRepository repository, GeminiConfig geminiConfig) {
        this.repository = repository;
        this.geminiConfig = geminiConfig;
    }

    // =========================
    // יצירת embedding ושמירה
    // =========================
    public void createAndSaveEmbedding(Long chunkId, String text) {
        // 1️⃣ יצירת embedding - placeholder / בעתיד קריאה ל-Gemini API
        float[] embedding = callGeminiApi(text);

        // 2️⃣ המרה ל-byte[] כדי להתאים לשדה ב-DB
        byte[] embeddingBytes = floatArrayToByteArray(embedding);

        // 3️⃣ שמירה ב-DB
        PdfChunkEmbedding entity = new PdfChunkEmbedding(chunkId, embeddingBytes);
        repository.save(entity);
    }

    // =========================
    // המרה מ-float[] ל-byte[]
    // =========================
    public byte[] floatArrayToByteArray(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    // =========================
    // קריאה עתידית ל-Gemini API
    // =========================
    private float[] callGeminiApi(String text) {
        String apiKey = geminiConfig.getApiKey();
        // כאן נשלח את הטקסט ל-Gemini ונקבל embedding אמיתי
        // כרגע placeholder
        return new float[1536];
    }
    public void saveEmbedding(Long chunkId, byte[] embeddingBytes) {
        PdfChunkEmbedding embedding = new PdfChunkEmbedding(chunkId, embeddingBytes);
        repository.save(embedding);
    }
}
