package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfRepository extends JpaRepository<PdfFile, Long> {}
