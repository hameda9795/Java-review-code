package com.devmentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * DevMentor AI - AI-powered code review and mentoring platform for Spring Boot developers
 *
 * This application follows Hexagonal Architecture (Ports & Adapters) principles:
 * - Domain: Core business logic, independent of frameworks
 * - Application: Use cases and application services
 * - Infrastructure: Technical implementations (database, AI, GitHub)
 * - Interfaces: API controllers and external integrations
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableAsync
public class DevMentorAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevMentorAiApplication.class, args);
    }
}
