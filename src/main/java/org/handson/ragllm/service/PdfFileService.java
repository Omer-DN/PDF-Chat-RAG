package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfFileSummary;
import org.handson.ragllm.repository.PdfRepository;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.handson.ragllm.repository.QuestionAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PdfFileService {

    private final PdfRepository repository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final PdfTextChunkRepository chunkRepository;

    public PdfFileService(PdfRepository repository,
                          QuestionAnswerRepository questionAnswerRepository,
                          PdfTextChunkRepository chunkRepository) {
        this.repository = repository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public PdfFile save(Long userId, MultipartFile file) throws IOException {
        PdfFile pdf = new PdfFile(
                userId,
                file.getOriginalFilename(),
                file.getBytes(),
                LocalDateTime.now()
        );
        return repository.save(pdf);
    }

    /** רשימת קבצים לפי משתמש – בתוך טרנזקציה כדי לאפשר גישה ל-LOB ב-PostgreSQL */
    @Transactional(readOnly = true)
    public List<PdfFile> findByUserId(Long userId) {
        return repository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    /** רשימת מטא-דאטה בלבד (בלי תוכן PDF) – מונע "Unable to access lob stream". */
    @Transactional(readOnly = true)
    public List<PdfFileSummary> findSummariesByUserId(Long userId) {
        return repository.findSummariesByUserIdOrderByUploadedAtDesc(userId);
    }

    public java.util.Optional<PdfFile> findById(Long id) {
        return repository.findById(id);
    }

    /** מוחק קובץ PDF אחד וכל הצ'אט והמקטעים שלו. רק אם המשתמש בעלים. בלי טעינת LOB. */
    @Transactional
    public boolean deletePdfAndRelated(Long pdfId, Long userId) {
        if (!repository.existsByIdAndUserId(pdfId, userId)) {
            return false;
        }
        questionAnswerRepository.deleteByUserIdAndPdfId(userId, pdfId);
        chunkRepository.deleteByPdfId(pdfId);
        repository.deleteById(pdfId);
        return true;
    }

    /** מוחק את כל הקבצים וההיסטוריה של המשתמש. משתמש ב-Summary כדי לא לטעון LOB. */
    @Transactional
    public void deleteAllByUserId(Long userId) {
        questionAnswerRepository.deleteByUserId(userId);
        List<PdfFileSummary> summaries = repository.findSummariesByUserIdOrderByUploadedAtDesc(userId);
        for (PdfFileSummary s : summaries) {
            chunkRepository.deleteByPdfId(s.getId());
        }
        repository.deleteByUserId(userId);
    }
}
