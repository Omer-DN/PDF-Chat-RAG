package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfTextChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdfTextChunkRepository extends JpaRepository<PdfTextChunk, Long> {
    List<PdfTextChunk> findByPdfIdOrderByChunkNumberAsc(Long pdfId);
}
