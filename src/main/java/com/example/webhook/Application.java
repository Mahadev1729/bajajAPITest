package com.example.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

import com.example.webhook.service.WebhookService;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // This runs on startup
    @Bean
    ApplicationRunner runner(WebhookService webhookService) {
        return args -> {
            webhookService.executeFlow();
        };
    }
}
