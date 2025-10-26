package com.devmentor.interfaces.rest.dto;

import com.devmentor.domain.codereview.CodeQualityScore;
import com.devmentor.domain.codereview.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for code review response
 */
@Data
@Builder
public class ReviewResponse {

    private UUID id;
    private String title;
    private String description;
    private ReviewStatus status;
    private UUID reviewerId;
    private String reviewerName;
    private UUID projectId;
    private String projectName;
    private CodeQualityScoreDto qualityScore;
    private List<ReviewFindingDto> findings;
    private Integer totalFilesAnalyzed;
    private Integer totalLinesAnalyzed;
    private Long analysisDurationMs;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    public static class CodeQualityScoreDto {
        private Integer overallScore;
        private Integer securityScore;
        private Integer performanceScore;
        private Integer maintainabilityScore;
        private Integer bestPracticesScore;
        private Integer testCoverageScore;
        private String grade;

        public static CodeQualityScoreDto from(CodeQualityScore score) {
            if (score == null) return null;

            return CodeQualityScoreDto.builder()
                    .overallScore(score.getOverallScore())
                    .securityScore(score.getSecurityScore())
                    .performanceScore(score.getPerformanceScore())
                    .maintainabilityScore(score.getMaintainabilityScore())
                    .bestPracticesScore(score.getBestPracticesScore())
                    .testCoverageScore(score.getTestCoverageScore())
                    .grade(score.getGrade())
                    .build();
        }
    }
}
