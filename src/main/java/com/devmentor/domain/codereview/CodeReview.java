package com.devmentor.domain.codereview;

import com.devmentor.domain.project.Project;
import com.devmentor.domain.shared.BaseEntity;
import com.devmentor.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeReview aggregate root - represents a complete code review session
 */
@Entity
@Table(name = "code_reviews", indexes = {
        @Index(name = "idx_review_user_id", columnList = "user_id"),
        @Index(name = "idx_review_project_id", columnList = "project_id"),
        @Index(name = "idx_review_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewFinding> findings = new ArrayList<>();

    @Embedded
    private CodeQualityScore qualityScore;

    @Column(name = "total_files_analyzed", nullable = false)
    @Builder.Default
    private Integer totalFilesAnalyzed = 0;

    @Column(name = "total_lines_analyzed", nullable = false)
    @Builder.Default
    private Integer totalLinesAnalyzed = 0;

    @Column(name = "analysis_duration_ms")
    private Long analysisDurationMs;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ai_model_used", length = 50)
    private String aiModelUsed;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "cost_usd")
    private Double costUsd;

    // Business methods
    public void addFinding(ReviewFinding finding) {
        findings.add(finding);
        finding.setCodeReview(this);
    }

    public void removeFinding(ReviewFinding finding) {
        findings.remove(finding);
        finding.setCodeReview(null);
    }

    public void start() {
        this.status = ReviewStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(CodeQualityScore score) {
        this.status = ReviewStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.qualityScore = score;

        if (startedAt != null && completedAt != null) {
            this.analysisDurationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    public void fail(String reason) {
        this.status = ReviewStatus.FAILED;
        this.description = reason;
        this.completedAt = LocalDateTime.now();
    }

    public List<ReviewFinding> getCriticalFindings() {
        return findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.CRITICAL)
                .toList();
    }

    public List<ReviewFinding> getHighFindings() {
        return findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.HIGH)
                .toList();
    }

    public List<ReviewFinding> getMediumFindings() {
        return findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.MEDIUM)
                .toList();
    }

    public List<ReviewFinding> getLowFindings() {
        return findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.LOW)
                .toList();
    }

    public List<ReviewFinding> getFindingsByCategory(FindingCategory category) {
        return findings.stream()
                .filter(f -> f.getCategory() == category)
                .toList();
    }

    public boolean isCompleted() {
        return status == ReviewStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == ReviewStatus.IN_PROGRESS;
    }

    public boolean hasCriticalIssues() {
        return findings.stream()
                .anyMatch(f -> f.getSeverity() == FindingSeverity.CRITICAL);
    }

    public int getTotalIssuesCount() {
        return findings.size();
    }

    public void updateMetrics(int filesAnalyzed, int linesAnalyzed, int tokensUsed, double cost) {
        this.totalFilesAnalyzed = filesAnalyzed;
        this.totalLinesAnalyzed = linesAnalyzed;
        this.tokensUsed = tokensUsed;
        this.costUsd = cost;
    }
}
