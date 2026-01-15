package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // מאפשר ל-HTML לגשת לשרת
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfFileService;
    private final PdfTextExtractorService textExtractorService;
    private final PdfChunkService chunkService;
    private final RagService ragService;

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfChunkService chunkService,
            RagService ragService
    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;
        this.ragService = ragService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) throws Exception {

        // 1. שמירת פרטי הקובץ ב-DB
        PdfFile saved = pdfFileService.save(file, userId);

        // 2. חילוץ טקסט
        String text = textExtractorService.extractText(saved.getId());

        // 3. חלוקה ל-Chunks
        List<String> chunks = chunkService.splitTextIntoChunks(text);

        // 4. יצירת וקטורים (Embeddings) ושמירה ב-Vector Store
        ragService.saveChunksWithEmbeddings(saved.getId(), chunks);

        return Map.of(
                "message", "PDF uploaded and processed successfully",
                "pdfId", saved.getId(),
                "numChunks", chunks.size()
        );
    }
}