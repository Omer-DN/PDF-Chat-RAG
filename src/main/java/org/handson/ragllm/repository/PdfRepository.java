package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    List<PdfFile> findByUserIdOrderByUploadedAtDesc(Long userId);
}
