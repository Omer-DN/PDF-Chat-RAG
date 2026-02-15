package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.PdfFileSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    /** בדיקת בעלות בלי לטעון תוכן PDF (LOB). */
    boolean existsByIdAndUserId(Long id, Long userId);

    List<PdfFile> findByUserIdOrderByUploadedAtDesc(Long userId);

    /** רשימה בלי טעינת data – מונע LOB/auto-commit error */
    List<PdfFileSummary> findSummariesByUserIdOrderByUploadedAtDesc(Long userId);

    /** מחיקה ישירה בלי טעינת entities (מונע LOB). */
    @Modifying
    @Query("DELETE FROM PdfFile f WHERE f.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
