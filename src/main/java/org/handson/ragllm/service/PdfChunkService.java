package org.handson.ragllm.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PdfChunkService {

    private static final int CHUNK_SIZE = 500; // תווים בכל chunk

    public List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            chunks.add(text.substring(i, Math.min(length, i + CHUNK_SIZE)));
        }
        return chunks;
    }
}
