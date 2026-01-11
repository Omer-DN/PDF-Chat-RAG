package org.handson.ragllm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_chunks")
public class PdfTextChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pdfId;

    @Column(columnDefinition = "TEXT", nullable = false) // עדיף TEXT פשוט ב-Postgres על פני @Lob
    private String text;

    @Column(nullable = false)
    private int chunkNumber;

    // הוספת עמודת הווקטור
    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    protected PdfTextChunk() {}

    public PdfTextChunk(Long pdfId, String text, int chunkNumber) {
        this.pdfId = pdfId;
        this.text = text;
        this.chunkNumber = chunkNumber;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public Long getPdfId() { return pdfId; }
    public String getText() { return text; }
    public int getChunkNumber() { return chunkNumber; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}