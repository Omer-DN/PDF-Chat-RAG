package org.handson.ragllm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Operation(
            summary = "Upload PDF file",
            description = "Upload a single PDF to the RAG system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "PDF uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "No file uploaded")
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(
            @Parameter(description = "PDF file to upload", required = true)
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded");
        }

        // כאן נוכל להוסיף שמירה זמנית או שירות
        return ResponseEntity.ok("PDF uploaded successfully: " + file.getOriginalFilename());
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello PDF RAG!";
    }
}
