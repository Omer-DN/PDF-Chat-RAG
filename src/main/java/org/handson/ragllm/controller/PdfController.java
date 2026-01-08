package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        // 1️⃣ שמירת הקובץ
        PdfFile saved = pdfFileService.save(file);

        // 2️⃣ חילוץ הטקסט
        String text = textExtractorService.extractText(saved.getId());

        // 3️⃣ פיצול לטקסט chunks
        List<String> chunks = chunkService.splitTextIntoChunks(text);

        // 4️⃣ שמירת ה-chunks בבסיס נתונים
        chunkStorageService.saveChunks(saved.getId(), chunks);

        // 5️⃣ יצירת embeddings ושמירה בבסיס נתונים
        List<PdfTextChunk> storedChunks = chunkStorageService.getChunks(saved.getId());
        for (PdfTextChunk chunk : storedChunks) {
            // ליצירת embedding אמיתי בעתיד: float[] מ-Gemini
            float[] embedding = new float[1536]; // dummy zeros כרגע
            // המר float[] ל-byte[] לפני שמירה
            byte[] embeddingBytes = embeddingService.floatArrayToByteArray(embedding);
            embeddingService.saveEmbedding(chunk.getId(), embeddingBytes);
        }

        return Map.of(
                "message", "PDF uploaded successfully with embeddings",
                "pdfId", saved.getId(),
                "filename", saved.getFilename(),
                "numChunks", storedChunks.size()
        );
    }


    @GetMapping("/{pdfId}/chunks")
    public Map<String, Object> getPdfChunks(@PathVariable Long pdfId) {
        List<PdfTextChunk> storedChunks = chunkStorageService.getChunks(pdfId);
        return Map.of(
                "pdfId", pdfId,
                "numChunks", storedChunks.size(),
                "chunks", storedChunks.stream().map(PdfTextChunk::getText).toList()
        );
    }
}
