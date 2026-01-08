package org.handson.ragllm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_chunks")
public class PdfTextChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pdfId; // מזהה PDF מקור

    @Lob
    @Column(nullable = false)
    private String text; // הטקסט של החלק

    @Column(nullable = false)
    private int chunkNumber; // מספר Chunk

    protected PdfTextChunk() {}

    public PdfTextChunk(Long pdfId, String text, int chunkNumber) {
        this.pdfId = pdfId;
        this.text = text;
        this.chunkNumber = chunkNumber;
    }

    public Long getId() { return id; }
    public Long getPdfId() { return pdfId; }
    public String getText() { return text; }
    public int getChunkNumber() { return chunkNumber; }
}
