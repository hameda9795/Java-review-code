package com.devmentor.interfaces.rest.controller;

import com.devmentor.application.codereview.PromptGenerationService;
import com.devmentor.application.codereview.ReportGeneratorService;
import com.devmentor.application.codereview.ReviewService;
import com.devmentor.domain.codereview.CodeReview;
import com.devmentor.interfaces.rest.dto.CreateReviewRequest;
import com.devmentor.interfaces.rest.dto.GeneratedPromptDTO;
import com.devmentor.interfaces.rest.dto.ReviewFindingDto;
import com.devmentor.interfaces.rest.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST API controller for code reviews
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Code Reviews", description = "API for managing code reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReportGeneratorService reportGeneratorService;
    private final PromptGenerationService promptGenerationService;

    /**
     * Create a new code review from uploaded files
     */
    @PostMapping
    @Operation(summary = "Create new code review", description = "Analyzes uploaded files and creates a code review with findings")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        log.info("Creating review for user: {}, title: {}", userId, request.getTitle());

        CodeReview review = reviewService.reviewFiles(
                userId,
                request.getTitle(),
                request.getFiles()
        );

        ReviewResponse response = mapToResponse(review);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Review an existing project
     */
    @PostMapping("/projects/{projectId}")
    @Operation(summary = "Review a project", description = "Analyzes all files in a project")
    public ResponseEntity<ReviewResponse> reviewProject(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID projectId
    ) {
        log.info("Reviewing project: {} for user: {}", projectId, userId);

        CodeReview review = reviewService.reviewProject(userId, projectId);
        ReviewResponse response = mapToResponse(review);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get review by ID
     */
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review details", description = "Returns a specific code review with all findings")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        log.info("Getting review: {}", reviewId);

        return reviewService.getReview(reviewId)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all reviews for current user
     */
    @GetMapping
    @Operation(summary = "Get user reviews", description = "Returns all reviews for the current user")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        log.info("Getting reviews for user: {}", userId);

        List<CodeReview> reviews = reviewService.getUserReviews(userId);
        List<ReviewResponse> responses = reviews.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get recent reviews
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent reviews", description = "Returns most recent reviews for the user")
    public ResponseEntity<List<ReviewResponse>> getRecentReviews(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Getting {} recent reviews for user: {}", limit, userId);

        List<CodeReview> reviews = reviewService.getRecentReviews(userId, limit);
        List<ReviewResponse> responses = reviews.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get review summary
     */
    @GetMapping("/{reviewId}/summary")
    @Operation(summary = "Get review summary", description = "Returns AI-generated summary of the review")
    public ResponseEntity<String> getReviewSummary(@PathVariable UUID reviewId) {
        log.info("Getting summary for review: {}", reviewId);

        String summary = reviewService.getReviewSummary(reviewId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Mark finding as resolved
     */
    @PutMapping("/{reviewId}/findings/{findingId}/resolve")
    @Operation(summary = "Resolve finding", description = "Marks a finding as resolved")
    public ResponseEntity<Void> resolveFinding(
            @PathVariable UUID reviewId,
            @PathVariable UUID findingId
    ) {
        log.info("Resolving finding: {} in review: {}", findingId, reviewId);

        reviewService.resolveFinding(reviewId, findingId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a review
     */
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Deletes a code review")
    public ResponseEntity<Void> deleteReview(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID reviewId
    ) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Returns review statistics for the user")
    public ResponseEntity<ReviewService.ReviewStatistics> getUserStatistics(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        log.info("Getting statistics for user: {}", userId);

        ReviewService.ReviewStatistics stats = reviewService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Download review report in Markdown format
     */
    @GetMapping("/{reviewId}/download/markdown")
    @Operation(summary = "Download Markdown report", description = "Downloads the code review as a Markdown file")
    public ResponseEntity<byte[]> downloadMarkdownReport(@PathVariable UUID reviewId) {
        log.info("Downloading Markdown report for review: {}", reviewId);

        return reviewService.getReview(reviewId)
                .map(review -> {
                    String markdown = reportGeneratorService.generateMarkdownReport(review);
                    byte[] content = markdown.getBytes(StandardCharsets.UTF_8);

                    String filename = sanitizeFilename(review.getTitle()) + "_review.md";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.TEXT_MARKDOWN);
                    headers.setContentDispositionFormData("attachment", filename);
                    headers.setContentLength(content.length);

                    return new ResponseEntity<>(content, headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Download review report in HTML format
     */
    @GetMapping("/{reviewId}/download/html")
    @Operation(summary = "Download HTML report", description = "Downloads the code review as an HTML file")
    public ResponseEntity<byte[]> downloadHtmlReport(@PathVariable UUID reviewId) {
        log.info("Downloading HTML report for review: {}", reviewId);

        return reviewService.getReview(reviewId)
                .map(review -> {
                    String html = reportGeneratorService.generateHtmlReport(review);
                    byte[] content = html.getBytes(StandardCharsets.UTF_8);

                    String filename = sanitizeFilename(review.getTitle()) + "_review.html";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.TEXT_HTML);
                    headers.setContentDispositionFormData("attachment", filename);
                    headers.setContentLength(content.length);

                    return new ResponseEntity<>(content, headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Download review report in CSV format
     */
    @GetMapping("/{reviewId}/download/csv")
    @Operation(summary = "Download CSV report", description = "Downloads the code review findings as a CSV file")
    public ResponseEntity<byte[]> downloadCsvReport(@PathVariable UUID reviewId) {
        log.info("Downloading CSV report for review: {}", reviewId);

        return reviewService.getReview(reviewId)
                .map(review -> {
                    String csv = reportGeneratorService.generateCsvReport(review);
                    byte[] content = csv.getBytes(StandardCharsets.UTF_8);

                    String filename = sanitizeFilename(review.getTitle()) + "_findings.csv";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(new MediaType("text", "csv"));
                    headers.setContentDispositionFormData("attachment", filename);
                    headers.setContentLength(content.length);

                    return new ResponseEntity<>(content, headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

        /**
     * Generate AI-ready prompt for fixing all issues
     */
    @GetMapping("/{reviewId}/generate-prompt")
    @Operation(summary = "Generate AI fixing prompt", description = "Generates a comprehensive prompt for AI to fix all review findings")
    public ResponseEntity<GeneratedPromptDTO> generatePrompt(
            @PathVariable UUID reviewId
    ) {
        log.info("Generating AI fixing prompt for review: {}", reviewId);

        CodeReview review = reviewService.getReview(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        String prompt = promptGenerationService.generateFixingPrompt(review);
        
        // Count findings by severity
        long criticalCount = review.getFindings().stream()
            .filter(f -> f.getSeverity() == com.devmentor.domain.codereview.FindingSeverity.CRITICAL)
            .count();
        long highCount = review.getFindings().stream()
            .filter(f -> f.getSeverity() == com.devmentor.domain.codereview.FindingSeverity.HIGH)
            .count();
        long mediumCount = review.getFindings().stream()
            .filter(f -> f.getSeverity() == com.devmentor.domain.codereview.FindingSeverity.MEDIUM)
            .count();
        long lowCount = review.getFindings().stream()
            .filter(f -> f.getSeverity() == com.devmentor.domain.codereview.FindingSeverity.LOW)
            .count();
        
        GeneratedPromptDTO response = new GeneratedPromptDTO(
            prompt,
            review.getFindings().size(),
            (int) criticalCount,
            (int) highCount,
            (int) mediumCount,
            (int) lowCount,
            reviewId.toString(),
            LocalDateTime.now(),
            "Copy this prompt and paste it into Claude AI or another AI assistant to fix all the issues in your code."
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Sanitize filename for download
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9-_\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
    }

    /**
     * Map domain entity to DTO
     */
    private ReviewResponse mapToResponse(CodeReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .title(review.getTitle())
                .description(review.getDescription())
                .status(review.getStatus())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getUsername())
                .projectId(review.getProject() != null ? review.getProject().getId() : null)
                .projectName(review.getProject() != null ? review.getProject().getName() : null)
                .qualityScore(ReviewResponse.CodeQualityScoreDto.from(review.getQualityScore()))
                .findings(review.getFindings().stream()
                        .map(ReviewFindingDto::from)
                        .toList())
                .totalFilesAnalyzed(review.getTotalFilesAnalyzed())
                .totalLinesAnalyzed(review.getTotalLinesAnalyzed())
                .analysisDurationMs(review.getAnalysisDurationMs())
                .createdAt(review.getCreatedAt())
                .completedAt(review.getCompletedAt())
                .build();
    }
}
