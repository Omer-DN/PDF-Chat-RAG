package org.handson.ragllm.repository;

import org.handson.ragllm.model.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuestionAnswer> findByUserIdAndPdfIdOrderByCreatedAtAsc(Long userId, Long pdfId);

    /** מחיקה ישירה בלי טעינת entities. */
    @Modifying
    @Query("DELETE FROM QuestionAnswer q WHERE q.userId = :userId AND q.pdfId = :pdfId")
    void deleteByUserIdAndPdfId(@Param("userId") Long userId, @Param("pdfId") Long pdfId);

    /** מחיקה ישירה בלי טעינת entities. */
    @Modifying
    @Query("DELETE FROM QuestionAnswer q WHERE q.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
