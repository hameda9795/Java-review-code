package com.devmentor.domain.codereview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for CodeReview domain
 */
public interface CodeReviewRepository {

    CodeReview save(CodeReview codeReview);

    Optional<CodeReview> findById(UUID id);

    List<CodeReview> findByReviewerId(UUID reviewerId);

    List<CodeReview> findByProjectId(UUID projectId);

    List<CodeReview> findByStatus(ReviewStatus status);

    List<CodeReview> findByReviewerIdAndStatus(UUID reviewerId, ReviewStatus status);

    List<CodeReview> findRecentByReviewerId(UUID reviewerId, int limit);

    List<CodeReview> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    void delete(CodeReview codeReview);

    long countByReviewerId(UUID reviewerId);

    long countByStatus(ReviewStatus status);

    long countAll();

    long countByCreatedAtAfter(LocalDateTime date);
}
