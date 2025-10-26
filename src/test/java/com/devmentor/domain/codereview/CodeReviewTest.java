package com.devmentor.domain.codereview;

import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeReviewTest {

    private CodeReview codeReview;
    private User reviewer;

    @BeforeEach
    void setUp() {
        reviewer = User.builder()
                .username("reviewer")
                .email("reviewer@test.com")
                .role(UserRole.USER)
                .build();

        codeReview = CodeReview.builder()
                .reviewer(reviewer)
                .title("Test Review")
                .status(ReviewStatus.PENDING)
                .totalFilesAnalyzed(0)
                .build();
    }

    @Test
    void shouldStartReview() {
        // When
        codeReview.start();

        // Then
        assertEquals(ReviewStatus.IN_PROGRESS, codeReview.getStatus());
        assertNotNull(codeReview.getStartedAt());
    }

    @Test
    void shouldCompleteReview() {
        // Given
        codeReview.start();
        CodeQualityScore score = CodeQualityScore.calculate(80, 75, 85, 80, 70);

        // When
        codeReview.complete(score);

        // Then
        assertEquals(ReviewStatus.COMPLETED, codeReview.getStatus());
        assertNotNull(codeReview.getCompletedAt());
        assertEquals(score, codeReview.getQualityScore());
        assertNotNull(codeReview.getAnalysisDurationMs());
    }

    @Test
    void shouldFailReview() {
        // Given
        String errorMessage = "Analysis failed";

        // When
        codeReview.fail(errorMessage);

        // Then
        assertEquals(ReviewStatus.FAILED, codeReview.getStatus());
        assertEquals(errorMessage, codeReview.getDescription());
    }

    @Test
    void shouldAddFinding() {
        // Given
        ReviewFinding finding = ReviewFinding.builder()
                .title("Test Finding")
                .severity(FindingSeverity.HIGH)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .build();

        // When
        codeReview.addFinding(finding);

        // Then
        assertEquals(1, codeReview.getFindings().size());
        assertEquals(codeReview, finding.getCodeReview());
    }

    @Test
    void shouldGetCriticalFindings() {
        // Given
        codeReview.addFinding(ReviewFinding.builder()
                .severity(FindingSeverity.CRITICAL)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .build());
        codeReview.addFinding(ReviewFinding.builder()
                .severity(FindingSeverity.LOW)
                .category(FindingCategory.CODE_SMELL)
                .build());

        // When
        var criticalFindings = codeReview.getCriticalFindings();

        // Then
        assertEquals(1, criticalFindings.size());
        assertEquals(FindingSeverity.CRITICAL, criticalFindings.get(0).getSeverity());
    }

    @Test
    void shouldDetectCriticalIssues() {
        // Given
        codeReview.addFinding(ReviewFinding.builder()
                .severity(FindingSeverity.CRITICAL)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .build());

        // When/Then
        assertTrue(codeReview.hasCriticalIssues());
    }

    @Test
    void shouldGetFindingsByCategory() {
        // Given
        codeReview.addFinding(ReviewFinding.builder()
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .severity(FindingSeverity.HIGH)
                .build());
        codeReview.addFinding(ReviewFinding.builder()
                .category(FindingCategory.PERFORMANCE)
                .severity(FindingSeverity.MEDIUM)
                .build());

        // When
        var securityFindings = codeReview.getFindingsByCategory(FindingCategory.SECURITY_VULNERABILITY);

        // Then
        assertEquals(1, securityFindings.size());
        assertEquals(FindingCategory.SECURITY_VULNERABILITY, securityFindings.get(0).getCategory());
    }

    @Test
    void shouldUpdateMetrics() {
        // Given
        int files = 10;
        int lines = 1000;
        int tokens = 5000;
        double cost = 0.50;

        // When
        codeReview.updateMetrics(files, lines, tokens, cost);

        // Then
        assertEquals(files, codeReview.getTotalFilesAnalyzed());
        assertEquals(lines, codeReview.getTotalLinesAnalyzed());
        assertEquals(tokens, codeReview.getTokensUsed());
        assertEquals(cost, codeReview.getCostUsd());
    }
}
