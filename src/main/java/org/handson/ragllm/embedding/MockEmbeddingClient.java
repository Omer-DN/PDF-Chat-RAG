package org.handson.ragllm.embedding;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Profile({"default", "mock"})
public class MockEmbeddingClient implements EmbeddingClient {

    private static final int DIMENSION = 768;

    @Override
    public float[] embed(String text) {
        float[] vector = new float[DIMENSION];
        Random random = new Random(text.hashCode());

        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = random.nextFloat();
        }
        return vector;
    }
}
