package org.handson.ragllm.model;

public class QuestionRequest {

    private String question;
    private Long userId;

    // קונסטרקטור ריק נדרש עבור Jackson (דה-סריאליזציה של JSON)
    public QuestionRequest() {}

    public QuestionRequest(String question, Long userId) {
        this.question = question;
        this.userId = userId;
    }

    // Getters and Setters
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}