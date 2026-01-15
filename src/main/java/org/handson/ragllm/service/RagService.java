package org.handson.ragllm.service;

import org.handson.ragllm.model.*;
import org.handson.ragllm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RagService {

    private final PdfRepository pdfRepository;
    private final PdfTextChunkRepository chunkRepository;
    private final GeminiApiService geminiApiService; // הזרקה של ה-API המאוחד
    private final UserRepository userRepository;
    private final QuestionHistoryRepository historyRepository;

    public RagService(PdfRepository pdfRepository,
                      PdfTextChunkRepository chunkRepository,
                      GeminiApiService geminiApiService,
                      UserRepository userRepository,
                      QuestionHistoryRepository historyRepository) {
        this.pdfRepository = pdfRepository;
        this.chunkRepository = chunkRepository;
        this.geminiApiService = geminiApiService;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void saveChunksWithEmbeddings(Long pdfId, List<String> chunks) {
        PdfFile pdfFile = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found"));

        System.out.println("Starting embeddings for " + chunks.size() + " chunks...");

        for (int i = 0; i < chunks.size(); i++) {
            String text = chunks.get(i);
            float[] embedding = geminiApiService.getEmbedding(text); // כאן קורה החיבור לגוגל

            if (embedding != null) {
                PdfTextChunk chunkEntity = new PdfTextChunk(pdfFile, text, i, embedding);
                chunkRepository.save(chunkEntity);
            }
        }
        System.out.println("✅ All chunks saved to database.");
    }

    @Transactional
    public String askQuestion(Long pdfId, String question, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PdfFile pdfFile = pdfRepository.findByIdAndUserId(pdfId, userId)
                .orElseThrow(() -> new RuntimeException("PDF not found or access denied"));

        // 1. הפיכת השאלה לוקטור
        float[] questionVector = geminiApiService.getEmbedding(question);

        // 2. חיפוש דמיון ב-DB
        List<String> contextChunks = chunkRepository.findTopKTextByEmbedding(pdfId, questionVector, 5);
        String context = String.join("\n---\n", contextChunks);

        // 3. יצירת תשובה מ-Gemini
        String answer = geminiApiService.generateAnswerFromContext(question, context);

        // 4. שמירה להיסטוריה
        historyRepository.save(new QuestionHistory(user, pdfFile, question, answer));

        return answer;
    }
}