package org.handson.ragllm.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // קשר של "אחד לרבים" - משתמש אחד יכול להחזיק הרבה קבצים
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PdfFile> pdfFiles = new ArrayList<>();

    protected User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<PdfFile> getPdfFiles() { return pdfFiles; }

    public void addPdfFile(PdfFile pdfFile) {
        pdfFiles.add(pdfFile);
        pdfFile.setUser(this);
    }
}