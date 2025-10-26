package com.devmentor.domain.user;

import com.devmentor.domain.shared.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User domain entity representing a developer using the platform
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_github_id", columnList = "github_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "github_id")
    private Long githubId;

    @Column(name = "github_username", length = 50)
    private String githubUsername;

    @Column(name = "github_access_token", length = 500)
    private String githubAccessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private Integer reviewsCount = 0;

    @Column(name = "total_files_reviewed", nullable = false)
    @Builder.Default
    private Integer totalFilesReviewed = 0;

    // Business methods
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void incrementReviewCount() {
        this.reviewsCount++;
    }

    public void addFilesReviewed(int filesCount) {
        this.totalFilesReviewed += filesCount;
    }

    public boolean canCreateReview() {
        if (!isActive) {
            return false;
        }

        // Free tier: check subscription tier limit
        // Premium tier: unlimited
        return subscriptionTier == SubscriptionTier.PREMIUM ||
               reviewsCount < subscriptionTier.getMaxReviewsPerMonth();
    }

    public boolean hasGithubConnected() {
        return githubId != null && githubAccessToken != null;
    }

    public void connectGithub(Long githubId, String githubUsername, String accessToken) {
        this.githubId = githubId;
        this.githubUsername = githubUsername;
        this.githubAccessToken = accessToken;
    }

    public void updateGithubInfo(String githubUsername, String accessToken) {
        this.githubUsername = githubUsername;
        this.githubAccessToken = accessToken;
    }

    public void disconnectGithub() {
        this.githubId = null;
        this.githubUsername = null;
        this.githubAccessToken = null;
    }
}
