package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfFileService;
    private final PdfTextExtractorService textExtractorService;
    private final PdfChunkService chunkService;
    private final PdfTextChunkStorageService chunkStorageService;
    private final PdfChunkEmbeddingService embeddingService;

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfChunkService chunkService,
            PdfTextChunkStorageService chunkStorageService,
            PdfChunkEmbeddingService embeddingService
    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;
        this.chunkStorageService = chunkStorageService;
        this.embeddingService = embeddingService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {
        PdfFile saved = pdfFileService.save(file);
        String text = textExtractorService.extractText(saved.getId());

        // Split into chunks
        List<String> chunks = chunkService.splitTextIntoChunks(text);

        // Save chunks
        chunkStorageService.saveChunks(saved.getId(), chunks);

        // Compute and save embeddings
        List<Long> chunkIds = chunkStorageService.getChunks(saved.getId())
                .stream()
                .map(c -> c.getId())
                .toList();

        for (Long chunkId : chunkIds) {
            // Dummy embedding: for now fill with zeros (1536 dim)
            float[] embedding = new float[1536];
            embeddingService.saveEmbedding(chunkId, embedding);
        }

        return Map.of(
                "message", "PDF uploaded successfully with embeddings",
                "pdfId", saved.getId(),
                "filename", saved.getFilename()
        );
    }

    @GetMapping("/{pdfId}/chunks")
    public Map<String, Object> getPdfChunks(@PathVariable Long pdfId) {
        List<String> chunks = chunkService.splitTextIntoChunks(
                textExtractorService.extractText(pdfId)
        );
        return Map.of(
                "pdfId", pdfId,
                "numChunks", chunks.size(),
                "chunks", chunks
        );
    }
}
