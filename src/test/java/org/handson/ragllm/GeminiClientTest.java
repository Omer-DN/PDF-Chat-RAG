package org.handson.ragllm;


import org.handson.ragllm.client.GeminiClient;
import org.handson.ragllm.config.GeminiConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GeminiClientTest {

    @Test
    public void testGetEmbedding_returnsCorrectLength() {
        // Mock Config
        GeminiConfig config = new GeminiConfig();
        config.setApiKey("dummy");
        config.setApiUrl("http://localhost/mock");

        GeminiClient client = new GeminiClient(config);

        String sampleText = "שלום עולם";
        float[] embedding = client.getEmbedding(sampleText);

        // בדיקה: המערך לא null ואורך נכון
        assertNotNull(embedding, "Embedding should not be null");
        assertEquals(1536, embedding.length, "Embedding length should be 1536");

        // אופציונלי: הדפסת הערך הראשון
        System.out.println("First value of embedding: " + embedding[0]);
    }
}
