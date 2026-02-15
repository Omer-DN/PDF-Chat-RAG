package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfFileSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    List<PdfFile> findByUserIdOrderByUploadedAtDesc(Long userId);

    /** רשימה בלי טעינת data – מונע LOB/auto-commit error */
    List<PdfFileSummary> findSummariesByUserIdOrderByUploadedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
