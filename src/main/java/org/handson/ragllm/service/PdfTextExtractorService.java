package org.handson.ragllm.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.repository.PdfRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class PdfTextExtractorService {

    private final PdfRepository pdfRepository;

    public PdfTextExtractorService(PdfRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    public String extractText(Long pdfId) {
        PdfFile pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found: " + pdfId));

        try (PDDocument document =
                     PDDocument.load(new ByteArrayInputStream(pdf.getData()))) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}
