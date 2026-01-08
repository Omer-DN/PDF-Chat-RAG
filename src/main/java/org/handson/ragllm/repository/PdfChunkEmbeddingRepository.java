package org.handson.ragllm.repository;

import org.handson.ragllm.model.PdfChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PdfChunkEmbeddingRepository extends JpaRepository<PdfChunkEmbedding, Long> {
    List<PdfChunkEmbedding> findByChunkIdIn(List<Long> chunkIds);
}
