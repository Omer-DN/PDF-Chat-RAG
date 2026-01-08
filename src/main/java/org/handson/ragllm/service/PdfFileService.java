package org.handson.ragllm.service;

import jakarta.transaction.Transactional;
import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.repository.PdfRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PdfFileService {

    private final PdfRepository repository;

    public PdfFileService(PdfRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PdfFile save(MultipartFile file) throws IOException {

        // מוחק הכל – חוק מערכת
        repository.deleteAll();

        PdfFile pdf = new PdfFile(
                file.getOriginalFilename(),
                file.getBytes(),
                LocalDateTime.now()
        );

        return repository.save(pdf);
    }

}
