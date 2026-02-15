package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfTextChunkStorageService {

    private final PdfTextChunkRepository repository;
    private final GeminiClient geminiClient; // הוספנו את הלקוח של ג'מיני

    public PdfTextChunkStorageService(PdfTextChunkRepository repository, GeminiClient geminiClient) {
        this.repository = repository;
        this.geminiClient = geminiClient;
    }

    @Transactional
    public void saveChunks(Long pdfId, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            PdfTextChunk chunk = new PdfTextChunk(pdfId, chunks.get(i), i);
            float[] embedding = geminiClient.getEmbeddingForDocument(chunks.get(i));
            chunk.setEmbedding(embedding);
            repository.save(chunk);
        }
    }

    public String askQuestion(Long pdfId, String question) {
        // 1. הפיכת השאלה לוקטור
        float[] questionEmbedding = geminiClient.getEmbeddingForQuery(question);

        // 2. שליפת הטקסטים בלבד (מונע את שגיאת ה-PSQLException)
        List<String> relevantTexts = repository.findTopKTextByEmbedding(pdfId, questionEmbedding, 5);

        if (relevantTexts.isEmpty()) {
            return "לא נמצא מידע רלוונטי במיסמך.";
        }

        // 3. בניית הקונטקסט
        String context = String.join("\n\n", relevantTexts);

        // 4. קבלת תשובה מג'מיני
        return geminiClient.generateAnswer(question, context);
    }

    /**
     * שומר מקטעים עם וקטור embedding ב-DB (768 מימדים – gemini-embedding-001 עם outputDimensionality).
     */
    @Transactional
    public void saveChunksWithEmbeddings(Long pdfId, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            String text = chunks.get(i);
            PdfTextChunk chunk = new PdfTextChunk(pdfId, text, i);
            float[] vector = geminiClient.getEmbeddingForDocument(text);
            chunk.setEmbedding(vector);
            repository.save(chunk);
        }
    }

    /**
     * מרענן embeddings לכל המקטעים של מסמך (למסמכים עם וקטורי אפס).
     */
    @Transactional
    public int reEmbedChunks(Long pdfId) {
        List<PdfTextChunk> chunks = repository.findByPdfIdOrderByChunkNumberAsc(pdfId);
        for (PdfTextChunk chunk : chunks) {
            float[] vector = geminiClient.getEmbeddingForDocument(chunk.getText());
            chunk.setEmbedding(vector);
            repository.save(chunk);
        }
        return chunks.size();
    }
}