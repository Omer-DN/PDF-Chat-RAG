package org.handson.ragllm.service;

import org.handson.ragllm.model.PdfTextChunk;
import org.handson.ragllm.repository.PdfTextChunkRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfChunkStorageService {

    private final PdfTextChunkRepository repository;

    public PdfChunkStorageService(PdfTextChunkRepository repository) {
        this.repository = repository;
    }

    public void saveChunks(Long pdfId, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            PdfTextChunk chunk = new PdfTextChunk(pdfId, chunks.get(i), i);
            repository.save(chunk);
        }
    }

    public List<PdfTextChunk> getChunks(Long pdfId) {
        return repository.findByPdfIdOrderByChunkNumberAsc(pdfId);
    }
}
