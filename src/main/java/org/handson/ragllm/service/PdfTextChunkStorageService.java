package org.handson.ragllm.service;

import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.model.QuestionAnswer;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfTextChunkStorageService {

    private static final int MAX_QA_IN_CONTEXT = 20;

    private final PdfTextChunkRepository repository;
    private final GeminiClient geminiClient;
    private final QuestionAnswerService questionAnswerService;

    public PdfTextChunkStorageService(PdfTextChunkRepository repository, GeminiClient geminiClient,
                                       QuestionAnswerService questionAnswerService) {
        this.repository = repository;
        this.geminiClient = geminiClient;
        this.questionAnswerService = questionAnswerService;
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

    /**
     * שאלה על המסמך – הקונטקסט כולל מקטעי טקסט רלוונטיים + היסטוריית השאלות והתשובות של הקובץ.
     */
    public String askQuestion(Long pdfId, Long userId, String question) {
        // 1. הפיכת השאלה לוקטור
        float[] questionEmbedding = geminiClient.getEmbeddingForQuery(question);

        // 2. שליפת מקטעי טקסט רלוונטיים
        List<String> relevantTexts = repository.findTopKTextByEmbedding(pdfId, questionEmbedding, 5);

        // 3. שליפת היסטוריית שאלות ותשובות של הקובץ (חלק מה"chunk" של המסמך)
        List<QuestionAnswer> qaHistory = questionAnswerService.findByUserIdAndPdfId(userId, pdfId);
        int from = Math.max(0, qaHistory.size() - MAX_QA_IN_CONTEXT);
        List<QuestionAnswer> recentQa = qaHistory.subList(from, qaHistory.size());

        // 4. בניית קונטקסט: מקטעי PDF + שאלות ותשובות קודמות
        StringBuilder context = new StringBuilder();
        if (!relevantTexts.isEmpty()) {
            context.append("--- קטעים מהמסמך ---\n\n");
            context.append(String.join("\n\n", relevantTexts));
        }
        if (!recentQa.isEmpty()) {
            if (context.length() > 0) context.append("\n\n");
            context.append("--- שאלות ותשובות קודמות על המסמך ---\n\n");
            for (QuestionAnswer qa : recentQa) {
                context.append("שאלה: ").append(qa.getQuestion()).append("\n");
                context.append("תשובה: ").append(qa.getAnswer()).append("\n\n");
            }
        }

        if (context.length() == 0) {
            return "לא נמצא מידע רלוונטי במיסמך.";
        }

        // 5. קבלת תשובה מג'מיני
        return geminiClient.generateAnswer(question, context.toString());
    }

    /**
     * שאלה על המסמך עם סטרימינג – אותו קונטקסט כמו askQuestion, מחזיר Flux של מקטעי טקסט.
     */
    public Flux<String> askQuestionStreaming(Long pdfId, Long userId, String question) {
        float[] questionEmbedding = geminiClient.getEmbeddingForQuery(question);
        List<String> relevantTexts = repository.findTopKTextByEmbedding(pdfId, questionEmbedding, 5);
        List<QuestionAnswer> qaHistory = questionAnswerService.findByUserIdAndPdfId(userId, pdfId);
        int from = Math.max(0, qaHistory.size() - MAX_QA_IN_CONTEXT);
        List<QuestionAnswer> recentQa = qaHistory.subList(from, qaHistory.size());

        StringBuilder context = new StringBuilder();
        if (!relevantTexts.isEmpty()) {
            context.append("--- קטעים מהמסמך ---\n\n");
            context.append(String.join("\n\n", relevantTexts));
        }
        if (!recentQa.isEmpty()) {
            if (context.length() > 0) context.append("\n\n");
            context.append("--- שאלות ותשובות קודמות על המסמך ---\n\n");
            for (QuestionAnswer qa : recentQa) {
                context.append("שאלה: ").append(qa.getQuestion()).append("\n");
                context.append("תשובה: ").append(qa.getAnswer()).append("\n\n");
            }
        }

        if (context.length() == 0) {
            return Flux.just("לא נמצא מידע רלוונטי במיסמך.");
        }

        return geminiClient.generateAnswerStreaming(question, context.toString());
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