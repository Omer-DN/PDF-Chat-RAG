package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    /**
     * מחפש את כל הקבצים המשוייכים למזהה משתמש ספציפי.
     * Spring Data JPA מבין אוטומטית שעליו לגשת לשדה 'user'
     * בתוך PdfFile ומשם לשדה 'id'.
     */
    List<PdfFile> findByUserId(Long userId);

    /**
     * ניתן גם להוסיף חיפוש קובץ ספציפי של משתמש כדי לוודא הרשאות
     */
    java.util.Optional<PdfFile> findByIdAndUserId(Long id, Long userId);
}