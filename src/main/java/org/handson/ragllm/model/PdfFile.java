package org.handson.ragllm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_files")
public class PdfFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected PdfFile() {}

    public PdfFile(Long userId, String filename, byte[] data, LocalDateTime uploadedAt) {
        this.userId = userId;
        this.filename = filename;
        this.data = data;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFilename() { return filename; }
    public byte[] getData() { return data; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
