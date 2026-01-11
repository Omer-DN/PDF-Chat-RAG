package org.handson.ragllm;

import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.model.PdfChunkEmbedding;
import org.handson.ragllm.repository.PdfChunkEmbeddingRepository;
import org.handson.ragllm.service.PdfChunkEmbeddingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PdfChunkEmbeddingServiceTest {

    @Test
    public void testCreateAndSaveEmbedding_savesCorrectly() {
        // 1️⃣ יוצרים Mocks
        GeminiClient mockClient = mock(GeminiClient.class);
        PdfChunkEmbeddingRepository mockRepo = mock(PdfChunkEmbeddingRepository.class);

        // 2️⃣ מגדירים מה ה-Mock של ה-GeminiClient יחזיר
        float[] dummyEmbedding = new float[1536];
        dummyEmbedding[0] = 0.5f;
        when(mockClient.getEmbedding("שלום עולם")).thenReturn(dummyEmbedding);

        // 3️⃣ יוצרים את ה-Service עם ה-Mocks
        PdfChunkEmbeddingService service = new PdfChunkEmbeddingService(mockRepo, mockClient);

        // 4️⃣ קוראים לפונקציה
        service.createAndSaveEmbedding(1L, "שלום עולם");

        // 5️⃣ תופסים את הארגומנט שנשלח ל-repository.save
        ArgumentCaptor<PdfChunkEmbedding> captor = ArgumentCaptor.forClass(PdfChunkEmbedding.class);
        verify(mockRepo, times(1)).save(captor.capture());

        PdfChunkEmbedding savedEntity = captor.getValue();

        // 6️⃣ בדיקות
        assertEquals(1L, savedEntity.getChunkId(), "ChunkId צריך להיות 1L");
        assertNotNull(savedEntity.getEmbedding(), "Embedding לא אמור להיות null");
        assertEquals(1536 * Float.BYTES, savedEntity.getEmbedding().length, "האורך של byte[] צריך להיות נכון");

        // 7️⃣ בדיקה שהערך הראשון הומר נכון
        ByteBuffer buffer = ByteBuffer.wrap(savedEntity.getEmbedding());
        float firstFloat = buffer.getFloat();
        assertEquals(0.5f, firstFloat, 0.0001f, "הערך הראשון צריך להיות 0.5");

        // 8️⃣ בדיקה שה־GeminiClient נקרא
        verify(mockClient, times(1)).getEmbedding("שלום עולם");
    }
}
