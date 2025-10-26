package com.devmentor.application.ai;

import com.devmentor.domain.codereview.CodePattern;
import com.devmentor.domain.codereview.FindingCategory;
import com.devmentor.infrastructure.ai.EmbeddingService;
import com.devmentor.infrastructure.persistence.vector.CodePatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * RAG (Retrieval-Augmented Generation) service
 * Retrieves relevant code patterns to enhance AI review quality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {

    private final CodePatternRepository codePatternRepository;
    private final EmbeddingService embeddingService;

    /**
     * Find relevant patterns for code snippet
     */
    @Transactional(readOnly = true)
    public List<CodePattern> findRelevantPatterns(String codeSnippet, int limit) {
        log.debug("Finding relevant patterns for code snippet");

        // Generate embedding for code
        float[] embedding = embeddingService.generateEmbedding(codeSnippet);

        // Convert to pgvector format
        String embeddingStr = Arrays.toString(embedding);

        // Search similar patterns
        return codePatternRepository.findSimilarPatterns(embeddingStr, limit);
    }

    /**
     * Find relevant patterns by category
     */
    @Transactional(readOnly = true)
    public List<CodePattern> findRelevantPatternsByCategory(
            String codeSnippet,
            FindingCategory category,
            int limit
    ) {
        log.debug("Finding relevant patterns for category: {}", category);

        float[] embedding = embeddingService.generateEmbedding(codeSnippet);
        String embeddingStr = Arrays.toString(embedding);

        return codePatternRepository.findSimilarPatternsByCategory(
                embeddingStr,
                category.name(),
                limit
        );
    }

    /**
     * Store new code pattern
     */
    @Transactional
    public CodePattern storePattern(CodePattern pattern) {
        log.info("Storing new code pattern: {}", pattern.getTitle());

        // Generate embedding for pattern
        String textToEmbed = pattern.getTitle() + " " +
                pattern.getDescription() + " " +
                (pattern.getCodeExample() != null ? pattern.getCodeExample() : "");

        float[] embedding = embeddingService.generateEmbedding(textToEmbed);
        pattern.setEmbedding(embedding);

        return codePatternRepository.save(pattern);
    }

    /**
     * Get context for AI prompt
     */
    public String getContextForPrompt(String codeSnippet, int maxPatterns) {
        List<CodePattern> patterns = findRelevantPatterns(codeSnippet, maxPatterns);

        if (patterns.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n\nRelevant Best Practices:\n");
        for (CodePattern pattern : patterns) {
            context.append(String.format("""

                    Pattern: %s
                    Category: %s
                    Description: %s
                    %s
                    """,
                    pattern.getTitle(),
                    pattern.getCategory(),
                    pattern.getDescription(),
                    pattern.getGoodExample() != null ?
                            "Good Example:\n" + pattern.getGoodExample() : ""
            ));
        }

        return context.toString();
    }
}
