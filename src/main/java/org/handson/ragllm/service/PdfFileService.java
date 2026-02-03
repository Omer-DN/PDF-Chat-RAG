package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.QuestionHistory;
import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.PdfRepository;
import org.handson.ragllm.repository.QuestionHistoryRepository;
import org.handson.ragllm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PdfFileService {

    private final PdfRepository repository;
    private final UserRepository userRepository;
    private final GeminiApiService embeddingService;
    private final PdfChunkService chunkService;
    private final QuestionHistoryRepository historyRepository; // נוסף

    public PdfFileService(PdfRepository repository,
                          UserRepository userRepository,
                          GeminiApiService embeddingService,
                          PdfChunkService chunkService,
                          QuestionHistoryRepository historyRepository) { // הוזרק בבנאי
        this.repository = repository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.chunkService = chunkService;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public PdfFile save(MultipartFile file, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PdfFile pdf = new PdfFile(file.getOriginalFilename(), file.getBytes(), LocalDateTime.now());
        pdf.setUser(user);
        PdfFile savedPdf = repository.save(pdf);

        String fullText = extractTextFromPdf(file.getBytes());

        // שימוש ב-PdfChunkService לחיתוך הטקסט
        List<String> textChunks = chunkService.splitTextIntoChunks(fullText);

        for (int i = 0; i < textChunks.size(); i++) {
            String text = textChunks.get(i);
            float[] vector = embeddingService.getEmbedding(text);
            chunkService.saveChunk(text, vector, savedPdf, i);
        }

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
}