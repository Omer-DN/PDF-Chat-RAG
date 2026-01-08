package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PdfTextChunkStorageService {

    private final PdfTextChunkRepository repository;

    public PdfTextChunkStorageService(PdfTextChunkRepository repository) {
        this.repository = repository;
    }

    // =========================
    // Save chunks to DB
    // =========================
    @Transactional
    public void saveChunks(Long pdfId, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            PdfTextChunk chunk = new PdfTextChunk(pdfId, chunks.get(i), i);
            repository.save(chunk);
        }
    }

    // =========================
    // Retrieve chunks from DB
    // =========================
    @Transactional(readOnly = true)
    public List<PdfTextChunk> getChunks(Long pdfId) {
        return repository.findByPdfIdOrderByChunkNumberAsc(pdfId);
    }
}
