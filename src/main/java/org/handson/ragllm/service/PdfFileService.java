package org.handson.ragllm.service;

import jakarta.transaction.Transactional;
import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.repository.PdfRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PdfFileService {

    private final PdfRepository repository;

    public PdfFileService(PdfRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PdfFile save(Long userId, MultipartFile file) throws IOException {
        PdfFile pdf = new PdfFile(
                userId,
                file.getOriginalFilename(),
                file.getBytes(),
                LocalDateTime.now()
        );
        return repository.save(pdf);
    }

    public List<PdfFile> findByUserId(Long userId) {
        return repository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    public java.util.Optional<PdfFile> findById(Long id) {
        return repository.findById(id);
    }
}
