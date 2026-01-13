package org.handson.ragllm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_history")
public class QuestionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // קישור למשתמש ששאל את השאלה
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // קישור לקובץ ה-PDF שעליו נשאלה השאלה
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_id", nullable = false)
    private PdfFile pdfFile;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected QuestionHistory() {}

    public QuestionHistory(User user, PdfFile pdfFile, String question, String answer) {
        this.user = user;
        this.pdfFile = pdfFile;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public PdfFile getPdfFile() { return pdfFile; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setUser(User user) { this.user = user; }
    public void setPdfFile(PdfFile pdfFile) { this.pdfFile = pdfFile; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
}