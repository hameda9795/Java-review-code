package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.codereview.CodeReview;
import com.devmentor.domain.codereview.CodeReviewRepository;
import com.devmentor.domain.codereview.ReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for CodeReview repository
 */
@Component
@RequiredArgsConstructor
public class CodeReviewRepositoryAdapter implements CodeReviewRepository {

    private final JpaCodeReviewRepository jpaCodeReviewRepository;

    @Override
    public CodeReview save(CodeReview codeReview) {
        return jpaCodeReviewRepository.save(codeReview);
    }

    @Override
    public Optional<CodeReview> findById(UUID id) {
        return jpaCodeReviewRepository.findById(id);
    }

    @Override
    public List<CodeReview> findByReviewerId(UUID reviewerId) {
        return jpaCodeReviewRepository.findByReviewerId(reviewerId);
    }

    @Override
    public List<CodeReview> findByProjectId(UUID projectId) {
        return jpaCodeReviewRepository.findByProjectId(projectId);
    }

    @Override
    public List<CodeReview> findByStatus(ReviewStatus status) {
        return jpaCodeReviewRepository.findByStatus(status);
    }

    @Override
    public List<CodeReview> findByReviewerIdAndStatus(UUID reviewerId, ReviewStatus status) {
        return jpaCodeReviewRepository.findByReviewerIdAndStatus(reviewerId, status);
    }

    @Override
    public List<CodeReview> findRecentByReviewerId(UUID reviewerId, int limit) {
        return jpaCodeReviewRepository.findRecentByReviewerId(reviewerId, limit);
    }

    @Override
    public List<CodeReview> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaCodeReviewRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public void delete(CodeReview codeReview) {
        jpaCodeReviewRepository.delete(codeReview);
    }

    @Override
    public long countByReviewerId(UUID reviewerId) {
        return jpaCodeReviewRepository.countByReviewerId(reviewerId);
    }

    @Override
    public long countByStatus(ReviewStatus status) {
        return jpaCodeReviewRepository.countByStatus(status);
    }

    @Override
    public long countAll() {
        return jpaCodeReviewRepository.count();
    }

    @Override
    public long countByCreatedAtAfter(LocalDateTime date) {
        return jpaCodeReviewRepository.countByCreatedAtAfter(date);
    }
}
