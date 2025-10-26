package com.devmentor.domain.codereview;

/**
 * Status of a code review
 */
public enum ReviewStatus {
    PENDING,      // Review requested but not started
    IN_PROGRESS,  // AI is analyzing the code
    COMPLETED,    // Review finished successfully
    FAILED        // Review failed due to error
}
