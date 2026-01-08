package org.handson.ragllm.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfChunkStorageService {

    // שמירת chunks לפי pdfId
    private final Map<Long, List<String>> storage = new HashMap<>();

    public void saveChunks(Long pdfId, List<String> chunks) {
        storage.put(pdfId, chunks);
    }

    public List<String> getChunks(Long pdfId) {
        return storage.getOrDefault(pdfId, List.of());
    }
}
