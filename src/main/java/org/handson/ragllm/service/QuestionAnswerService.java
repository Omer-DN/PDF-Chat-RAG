package org.handson.ragllm.service;

import org.handson.ragllm.model.QuestionAnswer;
import org.handson.ragllm.repository.QuestionAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionAnswerService {

    private final QuestionAnswerRepository repository;

    public QuestionAnswerService(QuestionAnswerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public QuestionAnswer save(Long userId, Long pdfId, String question, String answer) {
        QuestionAnswer qa = new QuestionAnswer(userId, pdfId, question, answer);
        return repository.save(qa);
    }

    public List<QuestionAnswer> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<QuestionAnswer> findByUserIdAndPdfId(Long userId, Long pdfId) {
        return repository.findByUserIdAndPdfIdOrderByCreatedAtAsc(userId, pdfId);
    }
}
