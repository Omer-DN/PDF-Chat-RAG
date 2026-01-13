package org.handson.ragllm.service;

import jakarta.transaction.Transactional;
import org.handson.ragllm.model.PdfFile;
import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.PdfRepository;
import org.handson.ragllm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PdfFileService {

    private final PdfRepository repository;
    private final UserRepository userRepository;

    public PdfFileService(PdfRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    /**
     * שמירת קובץ ושיוכו למשתמש ספציפי.
     * הערה: הסרנו את repository.deleteAll() כדי לאפשר שמירת היסטוריה.
     */
    @Transactional
    public PdfFile save(MultipartFile file, Long userId) throws IOException {

        // שליפת המשתמש מהדאטה-בייס
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // יצירת אובייקט הקובץ
        PdfFile pdf = new PdfFile(
                file.getOriginalFilename(),
                file.getBytes(),
                LocalDateTime.now()
        );

        // קישור הקובץ למשתמש
        pdf.setUser(user);

        // שמירה בדאטה-בייס
        return repository.save(pdf);
    }

    /**
     * שליפת כל הקבצים השייכים למשתמש מסוים
     */
    public java.util.List<PdfFile> getFilesByUser(Long userId) {
        return repository.findByUserId(userId);
    }
}