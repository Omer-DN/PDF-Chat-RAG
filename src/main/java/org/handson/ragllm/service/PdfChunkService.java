package org.handson.ragllm.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfChunkService {   // <-- כאן שינינו את השם

    private static final int CHUNK_SIZE = 500; // תווים לכל Chunk

    public List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }
}
