package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.QuestionHistory;
import org.handson.ragllm.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*") // מאפשר ל-HTML לגשת לשרת
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfFileService;

    public PdfController(PdfFileService pdfFileService) {
        this.pdfFileService = pdfFileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) throws Exception {

        // בדיקת גודל קובץ
        long fileSize = file.getSize();
        String storageType = fileSize > 10 * 1024 * 1024 ? "Elasticsearch" : "PostgreSQL";
        
        // PdfFileService.save() כבר עושה הכל:
        // 1. שמירת פרטי הקובץ ב-DB
        // 2. חילוץ טקסט מה-PDF
        // 3. חלוקה ל-Chunks
        // 4. יצירת embeddings ושמירה (PostgreSQL או Elasticsearch לפי גודל)
        PdfFile saved = pdfFileService.save(file, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "PDF uploaded and processed successfully");
        response.put("pdfId", saved.getId());
        response.put("fileSize", fileSize);
        response.put("storageType", storageType);
        return response;
    }

    /**
     * שליפת כל הקבצים של משתמש מסוים
     */
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserFiles(@PathVariable Long userId) {
        List<PdfFile> files = pdfFileService.getUserFiles(userId);
        return files.stream().map(file -> {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("id", file.getId());
            fileMap.put("filename", file.getFilename());
            fileMap.put("uploadedAt", file.getUploadedAt() != null ? file.getUploadedAt().toString() : null);
            return fileMap;
        }).collect(Collectors.toList());
    }

    /**
     * שליפת היסטוריית הצ'אט של משתמש עבור קובץ ספציפי
     */
    @GetMapping("/history/{userId}/{pdfId}")
    public List<Map<String, Object>> getChatHistory(
            @PathVariable Long userId,
            @PathVariable Long pdfId) {
        List<QuestionHistory> history = pdfFileService.getChatHistory(userId, pdfId);
        return history.stream().map(h -> {
            Map<String, Object> historyMap = new HashMap<>();
            historyMap.put("id", h.getId());
            historyMap.put("question", h.getQuestion());
            historyMap.put("answer", h.getAnswer());
            historyMap.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);
            return historyMap;
        }).collect(Collectors.toList());
    }

    /**
     * מחיקת היסטוריית צ'אט של משתמש עבור קובץ ספציפי
     */
    @DeleteMapping("/history/{userId}/{pdfId}")
    public Map<String, Object> deleteChatHistory(
            @PathVariable Long userId,
            @PathVariable Long pdfId) {
        pdfFileService.deleteChatHistory(userId, pdfId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Chat history deleted successfully");
        response.put("pdfId", pdfId);
        return response;
    }

    /**
     * מחיקת קובץ PDF וכל הנתונים הקשורים אליו
     */
    @DeleteMapping("/{userId}/{pdfId}")
    public Map<String, Object> deletePdfFile(
            @PathVariable Long userId,
            @PathVariable Long pdfId) {
        try {
            pdfFileService.deletePdfFile(userId, pdfId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "PDF file and all related data deleted successfully");
            response.put("pdfId", pdfId);
            return response;
        } catch (RuntimeException e) {
            // החזר שגיאה ברורה ל-Frontend
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("pdfId", pdfId);
            // במקום לזרוק exception, נחזיר response עם status code
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, 
                    e.getMessage() != null ? e.getMessage() : "Error deleting PDF file");
        }
    }
}