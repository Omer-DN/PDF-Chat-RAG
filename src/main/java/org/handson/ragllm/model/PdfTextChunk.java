package org.handson.ragllm.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "pdf_text_chunks")
public class PdfTextChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdf_id")
    private PdfFile pdfFile;

    @Column(columnDefinition = "TEXT")
    private String chunkText;

    private int chunkIndex;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    protected PdfTextChunk() {}

    public PdfTextChunk(PdfFile pdfFile, String chunkText, int chunkIndex, float[] embedding) {
        this.pdfFile = pdfFile;
        this.chunkText = chunkText;
        this.chunkIndex = chunkIndex;
        this.embedding = embedding;
    }

    // Getters
    public Long getId() { return id; }
    public String getChunkText() { return chunkText; }
    public float[] getEmbedding() { return embedding; }
}