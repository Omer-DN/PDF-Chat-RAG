package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.model.PdfChunkEmbedding;
import org.handson.ragllm.repository.PdfChunkEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class PdfChunkEmbeddingService {

    private final PdfChunkEmbeddingRepository repository;
    private final GeminiClient geminiClient;

    public PdfChunkEmbeddingService(
            PdfChunkEmbeddingRepository repository,
            GeminiClient geminiClient


    ) {
        this.repository = repository;
        this.geminiClient = geminiClient;

    }

    // =========================
    // יצירת embedding ושמירה
    // =========================
    public void createAndSaveEmbedding(Long chunkId, String text) {

        // 1️⃣ יצירת embedding (דרך Client)
        float[] embedding = geminiClient.getEmbedding(text);

        // 2️⃣ המרה ל-byte[]
        byte[] embeddingBytes = floatArrayToByteArray(embedding);

        // 3️⃣ שמירה ב-DB
        PdfChunkEmbedding entity = new PdfChunkEmbedding(chunkId, embeddingBytes);
        repository.save(entity);
    }

    // =========================
    // המרה מ-float[] ל-byte[]
    // =========================
    private byte[] floatArrayToByteArray(float[] floats) {
        ByteBuffer buffer =
                ByteBuffer.allocate(floats.length * Float.BYTES);

        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }
}
