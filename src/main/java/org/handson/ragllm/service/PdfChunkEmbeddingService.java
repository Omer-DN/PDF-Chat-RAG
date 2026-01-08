package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfChunkEmbedding;
import org.handson.ragllm.repository.PdfChunkEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class PdfChunkEmbeddingService {

    private final PdfChunkEmbeddingRepository repository;

    public PdfChunkEmbeddingService(PdfChunkEmbeddingRepository repository) {
        this.repository = repository;
    }

    // Convert float[] to byte[]
    public byte[] floatArrayToBytes(float[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
        for (float f : array) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    // Convert byte[] back to float[]
    public float[] bytesToFloatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] array = new float[bytes.length / 4];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }

    // Save embedding
    public void saveEmbedding(Long chunkId, float[] embedding) {
        byte[] data = floatArrayToBytes(embedding);
        repository.save(new PdfChunkEmbedding(chunkId, data));
    }

    // Fetch embeddings for given chunkIds
    public List<PdfChunkEmbedding> getEmbeddings(List<Long> chunkIds) {
        return repository.findByChunkIdIn(chunkIds);
    }
}
