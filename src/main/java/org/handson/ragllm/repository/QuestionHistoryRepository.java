package org.handson.ragllm.repository;

import org.handson.ragllm.model.QuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionHistoryRepository extends JpaRepository<QuestionHistory, Long> {

    /**
     * שליפת כל היסטוריית השאלות של משתמש מסוים עבור קובץ ספציפי.
     * התוצאות יוחזרו בסדר כרונולוגי (מהישן לחדש).
     */
    List<QuestionHistory> findByUserIdAndPdfFileIdOrderByCreatedAtAsc(Long userId, Long pdfId);

    /**
     * שליפת כל השאלות שמשתמש שאל אי פעם (בכל הקבצים).
     */
    List<QuestionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}