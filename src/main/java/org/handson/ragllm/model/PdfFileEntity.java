package org.handson.ragllm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_files")
public class PdfFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected PdfFileEntity() {}

    public PdfFileEntity(String filename, byte[] data, LocalDateTime uploadedAt) {
        this.filename = filename;
        this.data = data;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public String getFilename() { return filename; }
}
