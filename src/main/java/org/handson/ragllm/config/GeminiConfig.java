package org.handson.ragllm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {
    private String apiKey;
    private String baseUrl;
    /** מודל ל-generateContent, למשל gemini-2.0-flash או gemini-1.5-flash */
    private String model = "gemini-2.0-flash";
    /** מודל ל-embedding (למשל gemini-embedding-001) */
    private String embeddingModel = "gemini-embedding-001";
    /** ממד יציאה – חייב להתאים ל-vector(768) ב-DB */
    private int embeddingDimensions = 768;
}
