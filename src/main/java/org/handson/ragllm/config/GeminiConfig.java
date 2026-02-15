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
}
