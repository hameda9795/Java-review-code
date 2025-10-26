package com.devmentor.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom metrics configuration for application monitoring
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter reviewCreatedCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.reviews.created")
                .description("Total number of code reviews created")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter reviewCompletedCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.reviews.completed")
                .description("Total number of code reviews completed")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter reviewFailedCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.reviews.failed")
                .description("Total number of code reviews that failed")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Timer reviewDurationTimer(MeterRegistry registry) {
        return Timer.builder("devmentor.reviews.duration")
                .description("Duration of code review analysis")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.users.registered")
                .description("Total number of user registrations")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter githubAuthCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.github.authentications")
                .description("Total number of GitHub authentications")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter aiTokensCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.ai.tokens.used")
                .description("Total number of AI tokens consumed")
                .tag("application", "devmentor-ai")
                .register(registry);
    }

    @Bean
    public Counter findingsCounter(MeterRegistry registry) {
        return Counter.builder("devmentor.findings.total")
                .description("Total number of code findings detected")
                .tag("application", "devmentor-ai")
                .register(registry);
    }
}
