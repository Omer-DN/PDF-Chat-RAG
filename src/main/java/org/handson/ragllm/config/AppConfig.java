package org.handson.ragllm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * הגדרת RestTemplate כ-Bean.
     * זה יאפשר ל-Spring להזריק אותו ל-GeminiClient ולכל שירות אחר שיצטרך אותו.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}