package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.repository.PdfRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfTextExtractorService {

    private final PdfRepository pdfRepository;
    private final PdfChunkService pdfChunkService;
    private final PdfChunkStorageService chunkStorageService;

    public PdfTextExtractorService(
            PdfRepository pdfRepository,
            PdfChunkService pdfChunkService,
            PdfChunkStorageService chunkStorageService
    ) {
        this.pdfRepository = pdfRepository;
        this.pdfChunkService = pdfChunkService;
        this.chunkStorageService = chunkStorageService;
    }

    public String extractText(Long pdfId) {
        PdfFile pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found: " + pdfId));

        // כאן אתה משתמש ב־PDFBox או כל כלי אחר
        String text = new String(pdf.getData()); // לשלב הבא: שימוש ב־PDFBox כדי לקרוא טקסט אמיתי

        // פיצול ל־chunks ושמירה
        List<String> chunks = pdfChunkService.splitTextIntoChunks(text);
        chunkStorageService.saveChunks(pdfId, chunks);

        return text;
    }
}
