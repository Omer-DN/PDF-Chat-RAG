package org.handson.ragllm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(GeminiConfig.class)
public class AppConfig {

    /**
     * הגדרת RestTemplate כ-Bean.
     * זה יאפשר ל-Spring להזריק אותו ל-GeminiClient ולכל שירות אחר שיצטרך אותו.
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // הגדרת timeout כדי למנוע המתנה ארוכה מדי
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(30000); // 30 שניות
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(60000); // 60 שניות
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}