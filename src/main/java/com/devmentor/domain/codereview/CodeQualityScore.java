package com.devmentor.domain.codereview;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Value object representing overall code quality score
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeQualityScore {

    @Column(name = "overall_score")
    private Integer overallScore; // 0-100

    @Column(name = "security_score")
    private Integer securityScore; // 0-100

    @Column(name = "performance_score")
    private Integer performanceScore; // 0-100

    @Column(name = "maintainability_score")
    private Integer maintainabilityScore; // 0-100

    @Column(name = "best_practices_score")
    private Integer bestPracticesScore; // 0-100

    @Column(name = "test_coverage_score")
    private Integer testCoverageScore; // 0-100

    @Column(name = "grade", length = 2)
    private String grade; // A+, A, B, C, D, F

    /**
     * Calculate overall score from individual scores
     */
    public static CodeQualityScore calculate(
            int securityScore,
            int performanceScore,
            int maintainabilityScore,
            int bestPracticesScore,
            int testCoverageScore
    ) {
        // Weighted average
        int overall = (int) (
                securityScore * 0.25 +
                        performanceScore * 0.20 +
                        maintainabilityScore * 0.25 +
                        bestPracticesScore * 0.20 +
                        testCoverageScore * 0.10
        );

        String grade = calculateGrade(overall);

        return CodeQualityScore.builder()
                .overallScore(overall)
                .securityScore(securityScore)
                .performanceScore(performanceScore)
                .maintainabilityScore(maintainabilityScore)
                .bestPracticesScore(bestPracticesScore)
                .testCoverageScore(testCoverageScore)
                .grade(grade)
                .build();
    }

    private static String calculateGrade(int score) {
        if (score >= 95) return "A+";
        if (score >= 90) return "A";
        if (score >= 85) return "A-";
        if (score >= 80) return "B+";
        if (score >= 75) return "B";
        if (score >= 70) return "B-";
        if (score >= 65) return "C+";
        if (score >= 60) return "C";
        if (score >= 55) return "C-";
        if (score >= 50) return "D";
        return "F";
    }

    public boolean isExcellent() {
        return overallScore >= 90;
    }

    public boolean isGood() {
        return overallScore >= 75;
    }

    public boolean needsImprovement() {
        return overallScore < 60;
    }
}
