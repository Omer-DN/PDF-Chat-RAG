package org.handson.ragllm.repository;

import org.handson.ragllm.model.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuestionAnswer> findByUserIdAndPdfIdOrderByCreatedAtAsc(Long userId, Long pdfId);

    void deleteByUserIdAndPdfId(Long userId, Long pdfId);

    void deleteByUserId(Long userId);
}
