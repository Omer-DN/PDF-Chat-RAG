package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfChunkService {

    private final PdfTextChunkRepository chunkRepository;
    private final GeminiEmbeddingService embeddingService;
    private static final int CHUNK_SIZE = 1000; // הגדלנו מעט לביצועים טובים יותר

    public PdfChunkService(PdfTextChunkRepository chunkRepository, GeminiEmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    // מתודה 1: פיצול טקסט
    public List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }

    // מתודה 2: שמירת צ'אנק בודד (הפונקציה שהייתה חסרה לך!)
    public void saveChunk(String content, float[] vector, PdfFile pdfFile, int index) {
        PdfTextChunk chunk = new PdfTextChunk(pdfFile, content, index, vector);
        chunkRepository.save(chunk);
    }
}