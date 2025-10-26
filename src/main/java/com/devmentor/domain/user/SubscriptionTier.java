package com.devmentor.domain.user;

/**
 * Subscription tiers for the platform
 */
public enum SubscriptionTier {
    FREE(10, 10, 1),           // 10 reviews/month, 10 files per review, 1 GitHub repo
    PREMIUM(100, 100, 20);     // 100 reviews/month, 100 files per review, 20 GitHub repos

    private final int maxReviewsPerMonth;
    private final int maxFilesPerReview;
    private final int maxGithubRepos;

    SubscriptionTier(int maxReviewsPerMonth, int maxFilesPerReview, int maxGithubRepos) {
        this.maxReviewsPerMonth = maxReviewsPerMonth;
        this.maxFilesPerReview = maxFilesPerReview;
        this.maxGithubRepos = maxGithubRepos;
    }

    public int getMaxReviewsPerMonth() {
        return maxReviewsPerMonth;
    }

    public int getMaxFilesPerReview() {
        return maxFilesPerReview;
    }

    public int getMaxGithubRepos() {
        return maxGithubRepos;
    }
}
