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
    private final GeminiApiService geminiApiService;
    private final UserRepository userRepository; // הוסף
    private final QuestionHistoryRepository historyRepository; // הוסף

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

        for (int i = 0; i < chunks.size(); i++) {
            String text = chunks.get(i);
            float[] embedding = geminiApiService.getEmbedding(text);

            PdfTextChunk chunkEntity = new PdfTextChunk(pdfFile, text, i, embedding);
            chunkRepository.save(chunkEntity);
        }
    }

    /**
     * פונקציית ה-RAG המלאה: מקבלת שאלה, שולפת הקשר ווקטורי, מייצרת תשובה ושומרת להיסטוריה.
     */
    @Transactional
    public String askQuestion(Long pdfId, String question, Long userId) {

        // 1. אימות: מציאת המשתמש
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. אבטחה: מציאת הקובץ ולוודא שהוא שייך למשתמש
        PdfFile pdfFile = pdfRepository.findByIdAndUserId(pdfId, userId)
                .orElseThrow(() -> new RuntimeException("PDF not found or access denied for this user"));

        // 3. הפיכת השאלה לוקטור (Embedding)
        float[] questionVector = geminiApiService.getEmbedding(question);

        // 4. חיפוש ווקטורי ב-DB (Similarity Search)
        List<String> contextChunks = chunkRepository.findTopKTextByEmbedding(pdfId, questionVector, 5);

        if (contextChunks.isEmpty()) {
            return "מצטער, לא מצאתי מידע רלוונטי בקובץ ה-PDF כדי לענות על השאלה הזו.";
        }

        // חיבור כל חתיכות הטקסט להקשר אחד ארוך
        String context = String.join("\n---\n", contextChunks);

        // 5. שליחה ל-Gemini לקבלת תשובה סופית (Generative AI)
        String answer = geminiApiService.generateAnswerFromContext(question, context);

        // 6. שמירה להיסטוריה (Persistence)
        QuestionHistory history = new QuestionHistory(user, pdfFile, question, answer);
        historyRepository.save(history);

        return answer;
    }
}