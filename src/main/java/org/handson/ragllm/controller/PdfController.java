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

    /**
     * העלאת קובץ ושיוכו למשתמש.
     * שינוי: הוספת @RequestParam Long userId
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) throws Exception {

        // שמירת הקובץ מקושר למשתמש
        PdfFile saved = pdfFileService.save(file, userId);

        String text = textExtractorService.extractText(saved.getId());
        List<String> chunks = chunkService.splitTextIntoChunks(text);

        // שמירת ה-chunks עם embeddings
        chunkStorageService .saveChunksWithEmbeddings(saved.getId(), chunks);

        return Map.of(
                "message", "PDF uploaded successfully with embeddings for user: " + userId,
                "pdfId", saved.getId(),
                "numChunks", chunks.size()
        );
    }

    /**
     * שאילת שאלה ושמירה בהיסטוריה.
     * שינוי: ה-request יכיל כעת גם את ה-userId
     */
    @PostMapping("/{pdfId}/ask")
    public Map<String, String> ask(
            @PathVariable Long pdfId,
            @RequestBody QuestionRequest request) {

        // אנחנו מעבירים את ה-userId ל-service כדי שיוכל לשמור את ההיסטוריה
        String answer = chunkStorageService.askQuestion(pdfId, request.getQuestion(), request.getUserId());

        return Map.of("answer", answer);
    }
}