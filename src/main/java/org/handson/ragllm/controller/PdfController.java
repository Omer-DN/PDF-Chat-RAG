package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfFileService;
    private final PdfTextExtractorService textExtractorService;
    private final PdfChunkService chunkService;
    private final PdfTextChunkStorageService chunkStorageService; // ← הוספנו

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfChunkService chunkService,
            PdfTextChunkStorageService chunkStorageService // ← הוספנו
    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;
        this.chunkStorageService = chunkStorageService; // ← הוספנו
    }

    // =========================
    // Upload PDF
    // =========================
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {

        PdfFile saved = pdfFileService.save(file);
        String text = textExtractorService.extractText(saved.getId());

        // Split into chunks
        var chunks = chunkService.splitTextIntoChunks(text);

        // Save chunks
        chunkStorageService.saveChunks(saved.getId(), chunks);

        return Map.of(
                "message", "PDF uploaded successfully",
                "pdfId", saved.getId(),
                "filename", saved.getFilename()
        );
    }

    // =========================
    // Extract Text (DEBUG)
    // =========================
    @GetMapping("/{pdfId}/text")
    public Map<String, Object> getPdfText(@PathVariable Long pdfId) {

        String text = textExtractorService.extractText(pdfId);

        return Map.of(
                "pdfId", pdfId,
                "length", text.length(),
                "text", text
        );
    }

    // =========================
    // Get Chunks (DEBUG)
    // =========================
    @GetMapping("/{pdfId}/chunks")
    public Map<String, Object> getPdfChunks(@PathVariable Long pdfId) {
        var chunks = chunkStorageService.getChunks(pdfId);

        return Map.of(
                "pdfId", pdfId,
                "numChunks", chunks.size(),
                "chunks", chunks
        );
    }
}
