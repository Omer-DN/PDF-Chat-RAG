package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.service.PdfFileService;
import org.handson.ragllm.service.PdfTextChunkService;
import org.handson.ragllm.service.PdfTextExtractorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfTextChunkService chunkService;
    private final PdfFileService pdfFileService;
    private final PdfTextExtractorService textExtractorService;

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfTextChunkService chunkService

    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;

    }

    // =========================
    // Upload PDF
    // =========================
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {

        PdfFile saved = pdfFileService.save(file);

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

    @GetMapping("/{pdfId}/chunks")
    public Map<String, Object> getPdfChunks(@PathVariable Long pdfId) {
        String text = textExtractorService.extractText(pdfId);
        var chunks = chunkService.splitTextIntoChunks(text);

        return Map.of(
                "pdfId", pdfId,
                "numChunks", chunks.size(),
                "chunks", chunks
        );
    }
}
