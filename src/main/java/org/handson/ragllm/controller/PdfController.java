package org.handson.ragllm.controller;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.service.PdfFileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFileService pdfService;

    public PdfController(PdfFileService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {

        PdfFile saved = pdfService.save(file);

        return Map.of(
                "message", "PDF uploaded successfully",
                "pdfId", saved.getId(),
                "filename", saved.getFilename()
        );
    }
}
