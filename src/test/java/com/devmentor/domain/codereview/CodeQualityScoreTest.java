package com.devmentor.domain.codereview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeQualityScoreTest {

    @Test
    void shouldCalculateOverallScore() {
        // When
        CodeQualityScore score = CodeQualityScore.calculate(80, 75, 85, 80, 70);

        // Then
        assertNotNull(score);
        assertTrue(score.getOverallScore() >= 0 && score.getOverallScore() <= 100);
    }

    @Test
    void shouldCalculateGradeForExcellentScore() {
        // When
        CodeQualityScore score = CodeQualityScore.calculate(95, 95, 95, 95, 95);

        // Then
        assertEquals("A+", score.getGrade());
        assertTrue(score.isExcellent());
    }

    @Test
    void shouldCalculateGradeForGoodScore() {
        // When
        CodeQualityScore score = CodeQualityScore.calculate(80, 80, 80, 80, 80);

        // Then
        assertTrue(score.getGrade().startsWith("B"));
        assertTrue(score.isGood());
    }

    @Test
    void shouldCalculateGradeForPoorScore() {
        // When
        CodeQualityScore score = CodeQualityScore.calculate(50, 50, 50, 50, 50);

        // Then
        assertTrue(score.needsImprovement());
    }

    @Test
    void shouldCalculateWeightedAverage() {
        // Given - different scores for different categories
        int security = 100;
        int performance = 80;
        int maintainability = 90;
        int bestPractices = 85;
        int testCoverage = 70;

        // When
        CodeQualityScore score = CodeQualityScore.calculate(
                security, performance, maintainability, bestPractices, testCoverage
        );

        // Then
        // Security (25%) and Maintainability (25%) should have more weight
        assertTrue(score.getOverallScore() > 80);
    }
}
