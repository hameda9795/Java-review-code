package com.devmentor.infrastructure.ai;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j configuration for AI models
 * Using Claude Sonnet 4.5 (Anthropic) for chat, OpenAI for embeddings
 */
@Configuration
public class LangChain4jConfig {

    @Value("${ai.anthropic.api-key}")
    private String anthropicApiKey;

    @Value("${ai.anthropic.model:claude-sonnet-4-20250514}")
    private String chatModel;

    @Value("${ai.anthropic.temperature:0.3}")
    private Double temperature;

    @Value("${ai.anthropic.max-tokens:8192}")
    private Integer maxTokens;

    @Value("${ai.openai.api-key:#{null}}")
    private String openAiApiKey;

    @Value("${ai.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return AnthropicChatModel.builder()
                .apiKey(anthropicApiKey)
                .modelName(chatModel)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofMinutes(5))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        // Keep using OpenAI for embeddings (Claude doesn't provide embedding models)
        if (openAiApiKey != null && !openAiApiKey.isEmpty()) {
            return OpenAiEmbeddingModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(embeddingModel)
                    .timeout(Duration.ofMinutes(2))
                    .build();
        }
        throw new IllegalStateException(
                "OpenAI API key is required for embeddings. " +
                "Set ai.openai.api-key in application.properties"
        );
    }
}
