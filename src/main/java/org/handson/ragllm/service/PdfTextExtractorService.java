package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.repository.PdfRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class PdfTextExtractorService {

    private final PdfRepository pdfRepository;

    public PdfTextExtractorService(PdfRepository pdfFileRepository) {
        this.pdfRepository = pdfFileRepository;
    }

    public String extractText(Long pdfId) {
        PdfFile pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found with id: " + pdfId));

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf.getData()))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // ניקוי תווים מיותרים
            return text.replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // במקרה של שגיאה נחזיר מחרוזת ריקה
        }
    }
}
