package org.handson.ragllm;

import org.handson.ragllm.client.GeminiClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagLlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagLlmApplication.class, args);
    }

    @Bean
    public CommandLineRunner listAvailableModels(GeminiClient geminiClient) {
        return args -> {
            System.out.println("🔍 Checking available Gemini models...");
            geminiClient.listAvailableModels();
        };
    }
}
