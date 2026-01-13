package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.QuestionRequest;
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

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfChunkService chunkService,
            PdfTextChunkStorageService chunkStorageService
    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;
        this.chunkStorageService = chunkStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {

        PdfFile saved = pdfFileService.save(file);
        String text = textExtractorService.extractText(saved.getId());
        List<String> chunks = chunkService.splitTextIntoChunks(text);

        // שלב אחד שחוסך את כל ה-loop ב-controller
        chunkStorageService.saveChunksWithEmbeddings(saved.getId(), chunks);

        return Map.of(
                "message", "PDF uploaded successfully with embeddings",
                "pdfId", saved.getId(),
                "numChunks", chunks.size()
        );
    }

    @PostMapping("/{pdfId}/ask")
    public Map<String, String> ask(
            @PathVariable Long pdfId,
            @RequestBody QuestionRequest request) { // שינוי כאן מ-Map ל-QuestionRequest

        String answer = chunkStorageService.askQuestion(pdfId, request.getQuestion());
        return Map.of("answer", answer);
    }
}
