package com.devmentor.infrastructure.github;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for GitHub integration
 */
@Configuration
public class GitHubConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
