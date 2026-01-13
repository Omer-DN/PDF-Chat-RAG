package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfTextChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PdfTextChunkRepository extends JpaRepository<PdfTextChunk, Long> {

    /**
     * התיקון: אנחנו מחפשים את ה-Id בתוך שדה ה-pdfFile
     * Spring יתרגם את זה ל: SELECT * FROM pdf_chunks WHERE pdf_id = ...
     */
    List<PdfTextChunk> findByPdfFile_IdOrderByChunkNumberAsc(Long pdfId);

    /**
     * השאילתה הווקטורית ל-RAG
     */
    @Query(value = """
        SELECT text
        FROM pdf_chunks
        WHERE pdf_id = :pdfId
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findTopKTextByEmbedding(
            @Param("pdfId") Long pdfId,
            @Param("embedding") float[] embedding,
            @Param("limit") int limit
    );
}