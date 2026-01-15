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

    public PdfController(PdfFileService pdfFileService) {
        this.pdfFileService = pdfFileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) throws Exception {

        // PdfFileService.save() כבר עושה הכל:
        // 1. שמירת פרטי הקובץ ב-DB
        // 2. חילוץ טקסט מה-PDF
        // 3. חלוקה ל-Chunks
        // 4. יצירת embeddings ושמירה ב-pdf_text_chunks
        PdfFile saved = pdfFileService.save(file, userId);

        return Map.of(
                "message", "PDF uploaded and processed successfully",
                "pdfId", saved.getId()
        );
    }
}