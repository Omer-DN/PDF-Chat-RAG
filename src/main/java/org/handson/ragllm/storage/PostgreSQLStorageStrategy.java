package org.handson.ragllm.storage;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * אסטרטגיית אחסון ב-PostgreSQL - מתאימה לקבצים קטנים ובינוניים
 */
@Component
public class PostgreSQLStorageStrategy implements StorageStrategy {

    private final PdfTextChunkRepository chunkRepository;
    private final long maxFileSizeBytes;

    public PostgreSQLStorageStrategy(
            PdfTextChunkRepository chunkRepository,
            @Value("${pdf.max.size.postgres:10485760}") long maxFileSizeBytes) {
        this.chunkRepository = chunkRepository;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public void saveChunks(PdfFile pdfFile, List<String> chunks, List<float[]> vectors) {
        for (int i = 0; i < chunks.size(); i++) {
            PdfTextChunk chunk = new PdfTextChunk(pdfFile, chunks.get(i), i, vectors.get(i));
            chunkRepository.save(chunk);
        }
    }

    @Override
    public List<String> searchSimilarChunks(Long pdfId, float[] queryVector, int topK) {
        return chunkRepository.findTopKTextByEmbedding(pdfId, queryVector, topK);
    }

    @Override
    public void deleteChunks(Long pdfId) {
        chunkRepository.deleteByPdfFileId(pdfId);
    }

    @Override
    public boolean supportsFileSize(long fileSizeBytes) {
        // PostgreSQL תומך בקבצים עד הגודל המקסימלי המוגדר (ברירת מחדל: 10MB)
        return fileSizeBytes <= maxFileSizeBytes;
    }
}
