package com.devmentor.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.USER)
                .subscriptionTier(SubscriptionTier.FREE)
                .isActive(true)
                .reviewsCount(0)
                .totalFilesReviewed(0)
                .build();
    }

    @Test
    void shouldRecordLogin() {
        // When
        user.recordLogin();

        // Then
        assertNotNull(user.getLastLoginAt());
    }

    @Test
    void shouldIncrementReviewCount() {
        // Given
        int initialCount = user.getReviewsCount();

        // When
        user.incrementReviewCount();

        // Then
        assertEquals(initialCount + 1, user.getReviewsCount());
    }

    @Test
    void shouldAddFilesReviewed() {
        // Given
        int filesToAdd = 5;

        // When
        user.addFilesReviewed(filesToAdd);

        // Then
        assertEquals(filesToAdd, user.getTotalFilesReviewed());
    }

    @Test
    void shouldAllowReviewForFreeUserUnderLimit() {
        // Given
        user.setReviewsCount(3); // Under limit of 5

        // When/Then
        assertTrue(user.canCreateReview());
    }

    @Test
    void shouldNotAllowReviewForFreeUserOverLimit() {
        // Given
        user.setReviewsCount(5); // At limit

        // When/Then
        assertFalse(user.canCreateReview());
    }

    @Test
    void shouldAllowReviewForPremiumUser() {
        // Given
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setReviewsCount(100);

        // When/Then
        assertTrue(user.canCreateReview());
    }

    @Test
    void shouldConnectGithub() {
        // Given
        Long githubId = 12345L;
        String githubUsername = "testuser";
        String accessToken = "token123";

        // When
        user.connectGithub(githubId, githubUsername, accessToken);

        // Then
        assertEquals(githubId, user.getGithubId());
        assertEquals(githubUsername, user.getGithubUsername());
        assertEquals(accessToken, user.getGithubAccessToken());
        assertTrue(user.hasGithubConnected());
    }

    @Test
    void shouldDisconnectGithub() {
        // Given
        user.connectGithub(12345L, "testuser", "token123");

        // When
        user.disconnectGithub();

        // Then
        assertNull(user.getGithubId());
        assertNull(user.getGithubUsername());
        assertNull(user.getGithubAccessToken());
        assertFalse(user.hasGithubConnected());
    }

    @Test
    void shouldNotAllowReviewForInactiveUser() {
        // Given
        user.setIsActive(false);

        // When/Then
        assertFalse(user.canCreateReview());
    }
}
