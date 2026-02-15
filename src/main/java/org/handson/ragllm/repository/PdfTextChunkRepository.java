package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfTextChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PdfTextChunkRepository extends JpaRepository<PdfTextChunk, Long> {

    List<PdfTextChunk> findByPdfIdOrderByChunkNumberAsc(Long pdfId);

    /** מחיקה ישירה בלי טעינת entities (מונע טעינת וקטורים/LOB). */
    @Modifying
    @Query("DELETE FROM PdfTextChunk c WHERE c.pdfId = :pdfId")
    void deleteByPdfId(@Param("pdfId") Long pdfId);

    // השאילתה לחיפוש דמיון (RAG) - מחזירה טקסט כדי למנוע שגיאות וקטורים
    @Query(value = """
        SELECT text
        FROM pdf_chunks
        WHERE pdf_id = :pdfId
        ORDER BY embedding <-> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findTopKTextByEmbedding(
            @Param("pdfId") Long pdfId,
            @Param("embedding") float[] embedding,
            @Param("limit") int limit
    );
}