package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.codereview.CodeReview;
import com.devmentor.domain.codereview.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CodeReview entity
 */
@Repository
public interface JpaCodeReviewRepository extends JpaRepository<CodeReview, UUID> {

    List<CodeReview> findByReviewerId(UUID reviewerId);

    List<CodeReview> findByProjectId(UUID projectId);

    List<CodeReview> findByStatus(ReviewStatus status);

    List<CodeReview> findByReviewerIdAndStatus(UUID reviewerId, ReviewStatus status);

    @Query("SELECT cr FROM CodeReview cr WHERE cr.reviewer.id = :reviewerId ORDER BY cr.createdAt DESC LIMIT :limit")
    List<CodeReview> findRecentByReviewerId(@Param("reviewerId") UUID reviewerId, @Param("limit") int limit);

    List<CodeReview> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByReviewerId(UUID reviewerId);

    long countByStatus(ReviewStatus status);

    long countByCreatedAtAfter(LocalDateTime date);
}
