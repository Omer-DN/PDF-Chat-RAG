package org.handson.ragllm.service;

import jakarta.transaction.Transactional;
import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.PdfRepository;
import org.handson.ragllm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PdfFileService {

    private final PdfRepository repository;
    private final UserRepository userRepository;
    private final GeminiEmbeddingService embeddingService;
    private final PdfChunkService chunkService;

    public PdfFileService(PdfRepository repository, UserRepository userRepository,
                          GeminiEmbeddingService embeddingService, PdfChunkService chunkService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.chunkService = chunkService;
    }

    @Transactional
    public PdfFile save(MultipartFile file, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PdfFile pdf = new PdfFile(file.getOriginalFilename(), file.getBytes(), LocalDateTime.now());
        pdf.setUser(user);
        PdfFile savedPdf = repository.save(pdf);

        String fullText = extractTextFromPdf(file.getBytes());

        // שימוש ב-PdfChunkService החדש שלנו
        java.util.List<String> textChunks = chunkService.splitTextIntoChunks(fullText);

        for (int i = 0; i < textChunks.size(); i++) {
            String text = textChunks.get(i);
            float[] vector = embeddingService.embedText(text);
            chunkService.saveChunk(text, vector, savedPdf, i); // עכשיו זה יעבוד!
        }

        return savedPdf;
    }

    private String extractTextFromPdf(byte[] data) throws IOException {
        try (PDDocument document = PDDocument.load(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}