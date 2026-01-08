package org.handson.ragllm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_chunk_embeddings")
public class PdfChunkEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chunk_id", nullable = false)
    private Long chunkId;

    @Lob
    @Column(name = "embedding", nullable = false)
    private byte[] embedding;

    protected PdfChunkEmbedding() {}

    public PdfChunkEmbedding(Long chunkId, byte[] embedding) {
        this.chunkId = chunkId;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public Long getChunkId() { return chunkId; }
    public byte[] getEmbedding() { return embedding; }
}
