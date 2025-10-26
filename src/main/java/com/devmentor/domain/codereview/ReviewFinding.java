package com.devmentor.domain.codereview;

import com.devmentor.domain.shared.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ReviewFinding represents a single issue or suggestion found during code review
 */
@Entity
@Table(name = "review_findings", indexes = {
        @Index(name = "idx_finding_review_id", columnList = "code_review_id"),
        @Index(name = "idx_finding_severity", columnList = "severity"),
        @Index(name = "idx_finding_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFinding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private FindingSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FindingCategory category;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "end_line_number")
    private Integer endLineNumber;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "resources_url", length = 500)
    private String resourcesUrl;

    @Column(name = "impact_score")
    private Integer impactScore; // 1-10

    @Column(name = "metrics_violated", columnDefinition = "TEXT")
    private String metricsViolated; // JSON array of violated metrics (e.g., ["Cyclomatic Complexity: 15", "Method Length: 85"])

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    // Business methods
    public void markAsResolved() {
        this.isResolved = true;
    }

    public void markAsUnresolved() {
        this.isResolved = false;
    }

    public boolean isCritical() {
        return severity == FindingSeverity.CRITICAL;
    }

    public boolean isSecurityRelated() {
        return category == FindingCategory.SECURITY_VULNERABILITY;
    }

    public boolean isPerformanceRelated() {
        return category == FindingCategory.PERFORMANCE;
    }

    public String getLocationDisplay() {
        if (filePath == null) {
            return "Unknown location";
        }
        if (lineNumber == null) {
            return filePath;
        }
        return String.format("%s:%d", filePath, lineNumber);
    }
}
