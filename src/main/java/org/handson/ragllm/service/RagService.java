package org.handson.ragllm.service;

import org.handson.ragllm.model.*;
import org.handson.ragllm.repository.*;
import org.handson.ragllm.storage.StorageStrategy;
import org.handson.ragllm.storage.StorageStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RagService {

    private final PdfRepository pdfRepository;
    private final PdfTextChunkRepository chunkRepository;
    private final GeminiApiService geminiApiService;
    private final UserRepository userRepository;
    private final QuestionHistoryRepository historyRepository;
    private final StorageStrategyFactory storageStrategyFactory;

    public RagService(PdfRepository pdfRepository,
                      PdfTextChunkRepository chunkRepository,
                      GeminiApiService geminiApiService,
                      UserRepository userRepository,
                      QuestionHistoryRepository historyRepository,
                      StorageStrategyFactory storageStrategyFactory) {
        this.pdfRepository = pdfRepository;
        this.chunkRepository = chunkRepository;
        this.geminiApiService = geminiApiService;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Transactional
    public String askQuestion(Long pdfId, String question, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PdfFile pdfFile = pdfRepository.findByIdAndUserId(pdfId, userId)
                .orElseThrow(() -> new RuntimeException("PDF not found or access denied"));

        // 1. הפיכת השאלה לוקטור
        float[] questionVector = geminiApiService.getEmbedding(question);

        // 2. בחירת אסטרטגיית אחסון לפי גודל הקובץ
        long fileSize = pdfFile.getData().length;
        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(fileSize);
        
        // 3. חיפוש דמיון באמצעות האסטרטגיה הנבחרת
        List<String> contextChunks = storageStrategy.searchSimilarChunks(pdfId, questionVector, 5);
        String context = String.join("\n---\n", contextChunks);

        // 4. יצירת תשובה מ-Gemini
        String answer = geminiApiService.generateAnswerFromContext(question, context);

        // 5. שמירה להיסטוריה
        historyRepository.save(new QuestionHistory(user, pdfFile, question, answer));

        return answer;
    }
}