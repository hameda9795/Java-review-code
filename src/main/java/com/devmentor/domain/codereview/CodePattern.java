package com.devmentor.domain.codereview;

import com.devmentor.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Code pattern entity for RAG system
 * Stores best practices and anti-patterns with embeddings
 */
@Entity
@Table(name = "code_patterns", indexes = {
        @Index(name = "idx_pattern_category", columnList = "category"),
        @Index(name = "idx_pattern_type", columnList = "pattern_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodePattern extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "code_example", columnDefinition = "TEXT")
    private String codeExample;

    @Column(name = "good_example", columnDefinition = "TEXT")
    private String goodExample;

    @Column(name = "bad_example", columnDefinition = "TEXT")
    private String badExample;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FindingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false, length = 20)
    private PatternType patternType;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "resources_url", length = 500)
    private String resourcesUrl;

    public enum PatternType {
        BEST_PRACTICE,
        ANTI_PATTERN,
        SECURITY_ISSUE,
        PERFORMANCE_TIP
    }
}
