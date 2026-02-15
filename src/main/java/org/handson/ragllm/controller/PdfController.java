package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.QuestionRequest;
import org.handson.ragllm.security.UserPrincipal;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfFileService;
    private final PdfTextExtractorService textExtractorService;
    private final PdfChunkService chunkService;
    private final PdfTextChunkStorageService chunkStorageService;
    private final QuestionAnswerService questionAnswerService;

    public PdfController(
            PdfFileService pdfFileService,
            PdfTextExtractorService textExtractorService,
            PdfChunkService chunkService,
            PdfTextChunkStorageService chunkStorageService,
            QuestionAnswerService questionAnswerService
    ) {
        this.pdfFileService = pdfFileService;
        this.textExtractorService = textExtractorService;
        this.chunkService = chunkService;
        this.chunkStorageService = chunkStorageService;
        this.questionAnswerService = questionAnswerService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam("file") MultipartFile file) throws Exception {

        PdfFile saved = pdfFileService.save(user.getId(), file);
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
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long pdfId,
            @RequestBody QuestionRequest request) {

        String answer = chunkStorageService.askQuestion(pdfId, request.getQuestion());
        questionAnswerService.save(user.getId(), pdfId, request.getQuestion(), answer);
        return Map.of("answer", answer);
    }

    /** רשימת הקבצים של המשתמש המחובר (בלי טעינת תוכן PDF – מונע LOB error) */
    @GetMapping("/list")
    public List<Map<String, Object>> listMyFiles(@AuthenticationPrincipal UserPrincipal user) {
        return pdfFileService.findSummariesByUserId(user.getId()).stream()
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(),
                        "filename", f.getFilename(),
                        "uploadedAt", f.getUploadedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    /** מרענן embeddings למסמך (מתקן מסמכים עם וקטורי אפס). רק לבעלים. */
    @PostMapping("/{pdfId}/reembed")
    public Map<String, Object> reembedPdf(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long pdfId) {
        if (pdfFileService.findById(pdfId).filter(f -> f.getUserId().equals(user.getId())).isEmpty()) {
            return Map.of("ok", false, "message", "מסמך לא נמצא או אין הרשאה.");
        }
        int updated = chunkStorageService.reEmbedChunks(pdfId);
        return Map.of("ok", true, "message", "עודכנו " + updated + " מקטעים.", "chunksUpdated", updated);
    }

    /** מחיקת כל ההיסטוריה של המשתמש – כל הקבצים וכל השאלות/תשובות. (לפני /{pdfId} כדי ש־/all ייתפס.) */
    @DeleteMapping("/all")
    public Map<String, Object> deleteAllMyHistory(@AuthenticationPrincipal UserPrincipal user) {
        pdfFileService.deleteAllByUserId(user.getId());
        return Map.of("ok", true, "message", "כל ההיסטוריה נמחקה.");
    }

    /** מחיקת קובץ PDF מסוים וכל הצ'אט והמקטעים שלו. רק לבעלים. */
    @DeleteMapping("/{pdfId}")
    public Map<String, Object> deletePdf(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long pdfId) {
        boolean deleted = pdfFileService.deletePdfAndRelated(pdfId, user.getId());
        if (!deleted) {
            return Map.of("ok", false, "message", "מסמך לא נמצא או אין הרשאה.");
        }
        return Map.of("ok", true, "message", "הקובץ והצ'אט נמחקו.");
    }

    /** שאלות ותשובות למסמך (רק של המשתמש המחובר; המסמך חייב להיות שלו) */
    @GetMapping("/{pdfId}/history")
    public List<Map<String, String>> getHistory(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long pdfId) {
        if (pdfFileService.findById(pdfId).filter(f -> f.getUserId().equals(user.getId())).isEmpty()) {
            return List.of();
        }
        return questionAnswerService.findByUserIdAndPdfId(user.getId(), pdfId).stream()
                .map(qa -> Map.<String, String>of(
                        "question", qa.getQuestion(),
                        "answer", qa.getAnswer(),
                        "createdAt", qa.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }
}
