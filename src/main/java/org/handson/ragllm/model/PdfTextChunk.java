package org.handson.ragllm.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pdf_chunks")
public class PdfTextChunk {

    // Getters
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ב-Production עדיף לקשר לישות עצמה ולא רק ל-ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_id", nullable = false)
    private PdfFile pdfFile;

    @Getter
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false)
    private int chunkNumber;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    protected PdfTextChunk() {}

    // Constructor מלא שמתאים ל-Service שלנו
    public PdfTextChunk(PdfFile pdfFile, String text, int chunkNumber, float[] embedding) {
        this.pdfFile = pdfFile;
        this.text = text;
        this.chunkNumber = chunkNumber;
        this.embedding = embedding;
    }

    public int getChunkNumber() { return chunkNumber; }
    public float[] getEmbedding() { return embedding; }
}