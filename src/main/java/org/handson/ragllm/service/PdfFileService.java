package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.QuestionHistory;
import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.PdfRepository;
import org.handson.ragllm.repository.QuestionHistoryRepository;
import org.handson.ragllm.repository.UserRepository;
import org.handson.ragllm.storage.StorageStrategy;
import org.handson.ragllm.storage.StorageStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfFileService {

    private final PdfRepository repository;
    private final UserRepository userRepository;
    private final GeminiApiService embeddingService;
    private final PdfChunkService chunkService;
    private final QuestionHistoryRepository historyRepository;
    private final StorageStrategyFactory storageStrategyFactory;

    public PdfFileService(PdfRepository repository,
                          UserRepository userRepository,
                          GeminiApiService embeddingService,
                          PdfChunkService chunkService,
                          QuestionHistoryRepository historyRepository,
                          StorageStrategyFactory storageStrategyFactory) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.chunkService = chunkService;
        this.historyRepository = historyRepository;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Transactional
    public PdfFile save(MultipartFile file, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] fileBytes = file.getBytes();
        long fileSize = fileBytes.length;
        
        PdfFile pdf = new PdfFile(file.getOriginalFilename(), fileBytes, LocalDateTime.now());
        pdf.setUser(user);
        PdfFile savedPdf = repository.save(pdf);

        String fullText = extractTextFromPdf(fileBytes);

        // שימוש ב-PdfChunkService לחיתוך הטקסט
        List<String> textChunks = chunkService.splitTextIntoChunks(fullText);

        // בחירת אסטרטגיית אחסון לפי גודל הקובץ
        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(fileSize);
        
        // יצירת embeddings לכל ה-chunks
        List<float[]> vectors = new ArrayList<>();
        for (String chunk : textChunks) {
            float[] vector = embeddingService.getEmbedding(chunk);
            vectors.add(vector);
        }

        // שמירה באמצעות האסטרטגיה הנבחרת
        storageStrategy.saveChunks(savedPdf, textChunks, vectors);

        return savedPdf;
    }

    private String extractTextFromPdf(byte[] data) throws IOException {
        try (PDDocument document = PDDocument.load(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // א. שחזור צ'אט של משתמש עם שאלות ותשובות לכל קובץ
    @Transactional(readOnly = true)
    public List<QuestionHistory> getChatHistory(Long userId, Long pdfId) {
        return historyRepository.findByUserIdAndPdfFileIdOrderByCreatedAtAsc(userId, pdfId);
    }

    // ב. שליפת היסטוריית הקבצים של המשתמש
    @Transactional(readOnly = true)
    public List<PdfFile> getUserFiles(Long userId) {
        return repository.findByUserId(userId);
    }

    // פונקציית עזר לשמירת שאלה חדשה להיסטוריה (לקרוא לה מה-Controller אחרי תשובת ה-AI)
    @Transactional
    public void saveQuestionToHistory(Long userId, Long pdfId, String question, String answer) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PdfFile pdf = repository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found"));

        QuestionHistory history = new QuestionHistory(user, pdf, question, answer);
        historyRepository.save(history);
    }

    /**
     * מחיקת היסטוריית צ'אט של משתמש עבור קובץ ספציפי
     */
    @Transactional
    public void deleteChatHistory(Long userId, Long pdfId) {
        // וודא שהקובץ שייך למשתמש
        PdfFile pdf = repository.findByIdAndUserId(pdfId, userId)
                .orElseThrow(() -> new RuntimeException("PDF not found or access denied"));
        
        // מחק את כל היסטוריית הצ'אט
        historyRepository.deleteByUserIdAndPdfFileId(userId, pdfId);
    }

    /**
     * מחיקת קובץ PDF וכל הנתונים הקשורים אליו
     */
    @Transactional
    public void deletePdfFile(Long userId, Long pdfId) {
        // וודא שהקובץ שייך למשתמש
        PdfFile pdf = repository.findByIdAndUserId(pdfId, userId)
                .orElseThrow(() -> new RuntimeException("PDF not found or access denied"));
        
        try {
            // מחק את כל היסטוריית הצ'אט
            historyRepository.deleteByUserIdAndPdfFileId(userId, pdfId);
            
            // מחק את כל ה-chunks באמצעות StorageStrategy
            // נסה למצוא את האסטרטגיה המתאימה לפי גודל הקובץ
            long fileSize = pdf.getData().length;
            try {
                StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(fileSize);
                storageStrategy.deleteChunks(pdfId);
            } catch (IllegalArgumentException e) {
                // אם אין אסטרטגיה תומכת, נסה למחוק משתי האסטרטגיות
                // זה יכול לקרות אם הקובץ נשמר באסטרטגיה אחת אבל הגודל השתנה
                // או אם יש בעיה עם ה-StorageStrategyFactory
                try {
                    // נסה למחוק מ-PostgreSQL (אם יש chunks שם)
                    List<StorageStrategy> allStrategies = storageStrategyFactory.getAllStrategies();
                    for (StorageStrategy strategy : allStrategies) {
                        try {
                            strategy.deleteChunks(pdfId);
                        } catch (Exception ex) {
                            // המשך לנסות עם האסטרטגיה הבאה
                        }
                    }
                } catch (Exception ex) {
                    // אם זה נכשל, זה בסדר - אולי ה-chunks לא קיימים
                    // נמשיך למחוק את הקובץ עצמו
                }
            }
            
            // מחק את הקובץ עצמו
            repository.delete(pdf);
        } catch (RuntimeException e) {
            // אם זו שגיאה שאנחנו יודעים עליה, נזרוק אותה כמו שהיא
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting PDF file: " + e.getMessage(), e);
        }
    }
}