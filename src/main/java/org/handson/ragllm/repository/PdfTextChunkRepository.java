package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfTextChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PdfTextChunkRepository extends JpaRepository<PdfTextChunk, Long> {

    @Query(value = "SELECT c.chunk_text FROM pdf_text_chunks c " +
            "WHERE c.pdf_id = :pdfId " +
            "ORDER BY c.embedding <=> cast(:vector as vector) " + // האופרטור של pgvector
            "LIMIT :topK", nativeQuery = true)
    List<String> findTopKTextByEmbedding(@Param("pdfId") Long pdfId,
                                         @Param("vector") float[] vector,
                                         @Param("topK") int topK);
}