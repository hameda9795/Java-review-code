package com.devmentor.domain.project;

import com.devmentor.domain.shared.BaseEntity;
import com.devmentor.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Project domain entity representing a code project to be reviewed
 */
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_user_id", columnList = "user_id"),
        @Index(name = "idx_project_github_repo", columnList = "github_repository_url")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @NotBlank(message = "Project name is required")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 50)
    @Builder.Default
    private ProjectType projectType = ProjectType.SPRING_BOOT;

    @Column(name = "github_repository_url", length = 500)
    private String githubRepositoryUrl;

    @Column(name = "github_branch", length = 100)
    @Builder.Default
    private String githubBranch = "main";

    @Column(name = "local_path", length = 500)
    private String localPath;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SourceFile> sourceFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "total_files", nullable = false)
    @Builder.Default
    private Integer totalFiles = 0;

    @Column(name = "total_lines_of_code", nullable = false)
    @Builder.Default
    private Integer totalLinesOfCode = 0;

    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private Integer reviewsCount = 0;

    @Column(name = "average_quality_score")
    private Double averageQualityScore;

    // Business methods
    public void addSourceFile(SourceFile sourceFile) {
        sourceFiles.add(sourceFile);
        sourceFile.setProject(this);
        updateMetrics();
    }

    public void removeSourceFile(SourceFile sourceFile) {
        sourceFiles.remove(sourceFile);
        sourceFile.setProject(null);
        updateMetrics();
    }

    public void updateMetrics() {
        this.totalFiles = sourceFiles.size();
        this.totalLinesOfCode = sourceFiles.stream()
                .mapToInt(SourceFile::getLinesOfCode)
                .sum();
    }

    public void incrementReviewCount() {
        this.reviewsCount++;
    }

    public void updateAverageQualityScore(double newScore) {
        if (this.averageQualityScore == null) {
            this.averageQualityScore = newScore;
        } else {
            // Weighted average: new score has more weight
            this.averageQualityScore = (this.averageQualityScore * 0.7) + (newScore * 0.3);
        }
    }

    public boolean isFromGithub() {
        return githubRepositoryUrl != null && !githubRepositoryUrl.isEmpty();
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    public void activate() {
        this.status = ProjectStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == ProjectStatus.ACTIVE;
    }
}
