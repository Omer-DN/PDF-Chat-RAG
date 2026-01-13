package org.handson.ragllm.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_files")
public class PdfFile {

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

    // הקישור למשתמש - כל קובץ שייך למשתמש אחד
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected PdfFile() {}

    public PdfFile(String filename, byte[] data, LocalDateTime uploadedAt) {
        this.filename = filename;
        this.data = data;
        this.uploadedAt = uploadedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getFilename() { return filename; }
    public byte[] getData() { return data; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public User getUser() { return user; }
}