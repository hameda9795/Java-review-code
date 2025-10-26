package com.devmentor.application.codereview;

import com.devmentor.application.ai.AIReviewService;
import com.devmentor.application.ai.ArchitecturalAnalysisService;
import com.devmentor.domain.codereview.*;
import com.devmentor.domain.project.Project;
import com.devmentor.domain.project.ProjectRepository;
import com.devmentor.domain.project.SourceFile;
import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import com.devmentor.infrastructure.parser.JavaParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Main application service for orchestrating code reviews
 * This is the USE CASE layer in hexagonal architecture
 * 
 * Enhanced with:
 * - Architectural analysis (Hexagonal Architecture, DDD patterns)
 * - Static code analysis (JavaParser for metrics)
 * - Educational feedback (Best practices knowledge base)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final CodeReviewRepository codeReviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AIReviewService aiReviewService;
    private final ArchitecturalAnalysisService architecturalAnalysisService;
    private final JavaParserService javaParserService;

    /**
     * Create and execute a COMPREHENSIVE code review for uploaded files
     * 
     * Analysis includes:
     * 1. AI-powered deep code review (security, performance, best practices)
     * 2. Architectural analysis (hexagonal architecture, layer violations)
     * 3. Static code analysis (complexity metrics, code smells, SOLID violations)
     */
    @Transactional
    public CodeReview reviewFiles(UUID userId, String title, Map<String, String> files) {
        log.info("Starting comprehensive code review for user: {}, files: {}", userId, files.size());

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.canCreateReview()) {
            throw new IllegalStateException("User has reached review limit for current subscription tier");
        }

        // Create review
        CodeReview review = CodeReview.builder()
                .reviewer(user)
                .title(title)
                .description("Comprehensive educational code review of " + files.size() + " files")
                .status(ReviewStatus.PENDING)
                .totalFilesAnalyzed(files.size())
                .aiModelUsed("gpt-4o")
                .build();

        review.start();
        review = codeReviewRepository.save(review);

        try {
            List<ReviewFinding> allFindings = new ArrayList<>();
            
            // 1. AI-POWERED ANALYSIS (Deep semantic understanding)
            log.info("Phase 1: AI-powered analysis");
            List<ReviewFinding> aiFindings = aiReviewService.analyzeProject(files);
            allFindings.addAll(aiFindings);
            log.info("AI analysis found {} issues", aiFindings.size());
            
            // 2. ARCHITECTURAL ANALYSIS (Project structure, design patterns)
            log.info("Phase 2: Architectural analysis");
            List<ReviewFinding> architecturalFindings = architecturalAnalysisService.analyzeArchitecture(files);
            allFindings.addAll(architecturalFindings);
            log.info("Architectural analysis found {} issues", architecturalFindings.size());
            
            // 3. STATIC CODE ANALYSIS (Java-specific metrics)
            log.info("Phase 3: Static code analysis");
            List<ReviewFinding> staticFindings = performStaticAnalysis(files);
            allFindings.addAll(staticFindings);
            log.info("Static analysis found {} issues", staticFindings.size());

            // Add all findings to review
            for (ReviewFinding finding : allFindings) {
                review.addFinding(finding);
            }

            // Calculate comprehensive quality score
            CodeQualityScore score = aiReviewService.calculateQualityScore(allFindings);

            // Complete review
            review.complete(score);

            // Update metrics
            int totalLines = files.values().stream()
                    .mapToInt(content -> content.split("\n").length)
                    .sum();

            review.updateMetrics(
                    files.size(),
                    totalLines,
                    4000, // Mock token count
                    0.50  // Mock cost
            );

            // Update user stats
            user.incrementReviewCount();
            user.addFilesReviewed(files.size());
            userRepository.save(user);

            review = codeReviewRepository.save(review);

            log.info("Comprehensive code review completed: {} total findings, score: {}",
                    allFindings.size(), score.getOverallScore());
            log.info("Breakdown - AI: {}, Architectural: {}, Static: {}",
                    aiFindings.size(), architecturalFindings.size(), staticFindings.size());

            return review;

        } catch (Exception e) {
            log.error("Code review failed", e);
            review.fail("Analysis failed: " + e.getMessage());
            codeReviewRepository.save(review);
            throw new RuntimeException("Code review failed", e);
        }
    }

    /**
     * Perform static code analysis using JavaParser
     */
    private List<ReviewFinding> performStaticAnalysis(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            
            // Only analyze Java files
            if (!filePath.endsWith(".java")) {
                continue;
            }
            
            try {
                // Calculate code metrics
                Map<String, Object> metrics = javaParserService.calculateMetrics(content);
                
                // Check for high complexity
                if (metrics.containsKey("cyclomaticComplexity")) {
                    int complexity = (int) metrics.get("cyclomaticComplexity");
                    if (complexity > 20) {
                        findings.add(createComplexityFinding(filePath, complexity));
                    }
                }
                
                // Check for code smells
                if (metrics.containsKey("codeSmells")) {
                    @SuppressWarnings("unchecked")
                    List<String> smells = (List<String>) metrics.get("codeSmells");
                    for (String smell : smells) {
                        findings.add(createCodeSmellFinding(filePath, smell));
                    }
                }
                
                // Detect SOLID violations
                List<String> solidViolations = javaParserService.detectSOLIDViolations(content);
                for (String violation : solidViolations) {
                    findings.add(createSOLIDViolationFinding(filePath, violation));
                }
                
                // Suggest design patterns
                List<String> patternSuggestions = javaParserService.suggestDesignPatterns(content);
                for (String suggestion : patternSuggestions) {
                    findings.add(createDesignPatternSuggestion(filePath, suggestion));
                }
                
            } catch (Exception e) {
                log.warn("Static analysis failed for file: {}", filePath, e);
            }
        }
        
        return findings;
    }
    
    private ReviewFinding createComplexityFinding(String filePath, int complexity) {
        return ReviewFinding.builder()
                .title("High Cyclomatic Complexity (" + complexity + ")")
                .description("This file has very high cyclomatic complexity, making it difficult to understand and test")
                .severity(complexity > 30 ? FindingSeverity.HIGH : FindingSeverity.MEDIUM)
                .category(FindingCategory.CODE_SMELL)
                .filePath(filePath)
                .suggestedFix("""
                        Refactor to reduce complexity:
                        1. Extract complex conditions into named methods
                        2. Apply Strategy pattern for multiple conditional branches
                        3. Use polymorphism instead of if-else chains
                        4. Break down large methods into smaller, focused ones
                        
                        Target: Complexity < 10 per method
                        """)
                .explanation("""
                        Cyclomatic complexity measures the number of independent paths through code.
                        High complexity (> 20) indicates:
                        - Hard to understand
                        - Difficult to test thoroughly
                        - More bugs likely
                        - Expensive to maintain
                        
                        RESOURCES:
                        - Clean Code by Robert Martin: Chapter 3 - Functions
                        - Refactoring by Martin Fowler: Extract Method pattern
                        """)
                .build();
    }
    
    private ReviewFinding createCodeSmellFinding(String filePath, String smell) {
        return ReviewFinding.builder()
                .title("Code Smell: " + smell)
                .description(smell)
                .severity(smell.contains("GOD_CLASS") ? FindingSeverity.HIGH : FindingSeverity.MEDIUM)
                .category(FindingCategory.CODE_SMELL)
                .filePath(filePath)
                .explanation("See best practices documentation for remediation strategies")
                .build();
    }
    
    private ReviewFinding createSOLIDViolationFinding(String filePath, String violation) {
        return ReviewFinding.builder()
                .title("SOLID Principle Violation")
                .description(violation)
                .severity(FindingSeverity.MEDIUM)
                .category(FindingCategory.ARCHITECTURE)
                .filePath(filePath)
                .explanation("""
                        SOLID principles are fundamental to maintainable OOP design.
                        Violations lead to:
                        - Tight coupling
                        - Reduced testability
                        - Difficult refactoring
                        - Fragile code
                        """)
                .resourcesUrl("https://en.wikipedia.org/wiki/SOLID")
                .build();
    }
    
    private ReviewFinding createDesignPatternSuggestion(String filePath, String suggestion) {
        return ReviewFinding.builder()
                .title("Design Pattern Opportunity")
                .description(suggestion)
                .severity(FindingSeverity.INFO)
                .category(FindingCategory.DESIGN_PATTERN)
                .filePath(filePath)
                .explanation("Applying this design pattern would improve code structure and maintainability")
                .resourcesUrl("https://refactoring.guru/design-patterns")
                .build();
    }

    /**
     * Create and execute a code review for a project
     */
    @Transactional
    public CodeReview reviewProject(UUID userId, UUID projectId) {
        log.info("Starting code review for project: {}", projectId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this project");
        }

        if (!user.canCreateReview()) {
            throw new IllegalStateException("User has reached review limit");
        }

        // Extract files from project
        Map<String, String> files = new HashMap<>();
        for (SourceFile sourceFile : project.getSourceFiles()) {
            files.put(sourceFile.getFilePath(), sourceFile.getContent());
        }

        // Create review
        CodeReview review = reviewFiles(userId, "Review of " + project.getName(), files);
        review.setProject(project);

        // Update project metrics
        project.incrementReviewCount();
        if (review.getQualityScore() != null) {
            project.updateAverageQualityScore(review.getQualityScore().getOverallScore());
        }
        projectRepository.save(project);

        return codeReviewRepository.save(review);
    }

    /**
     * Get review by ID
     */
    @Transactional(readOnly = true)
    public Optional<CodeReview> getReview(UUID reviewId) {
        return codeReviewRepository.findById(reviewId);
    }

    /**
     * Get all reviews for a user
     */
    @Transactional(readOnly = true)
    public List<CodeReview> getUserReviews(UUID userId) {
        return codeReviewRepository.findByReviewerId(userId);
    }

    /**
     * Get recent reviews for a user
     */
    @Transactional(readOnly = true)
    public List<CodeReview> getRecentReviews(UUID userId, int limit) {
        return codeReviewRepository.findRecentByReviewerId(userId, limit);
    }

    /**
     * Get reviews for a project
     */
    @Transactional(readOnly = true)
    public List<CodeReview> getProjectReviews(UUID projectId) {
        return codeReviewRepository.findByProjectId(projectId);
    }

    /**
     * Get review summary
     */
    @Transactional(readOnly = true)
    public String getReviewSummary(UUID reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        return aiReviewService.generateSummary(review.getFindings());
    }

    /**
     * Mark finding as resolved
     */
    @Transactional
    public void resolveFinding(UUID reviewId, UUID findingId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.getFindings().stream()
                .filter(f -> f.getId().equals(findingId))
                .findFirst()
                .ifPresent(ReviewFinding::markAsResolved);

        codeReviewRepository.save(review);
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getReviewer().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this review");
        }

        codeReviewRepository.delete(review);
        log.info("Deleted review: {}", reviewId);
    }

    /**
     * Get review statistics for a user
     */
    @Transactional(readOnly = true)
    public ReviewStatistics getUserStatistics(UUID userId) {
        List<CodeReview> reviews = codeReviewRepository.findByReviewerId(userId);

        long totalReviews = reviews.size();
        long completedReviews = reviews.stream()
                .filter(CodeReview::isCompleted)
                .count();

        double averageScore = reviews.stream()
                .filter(CodeReview::isCompleted)
                .map(CodeReview::getQualityScore)
                .filter(Objects::nonNull)
                .mapToDouble(CodeQualityScore::getOverallScore)
                .average()
                .orElse(0.0);

        long totalFindings = reviews.stream()
                .mapToLong(CodeReview::getTotalIssuesCount)
                .sum();

        long criticalIssues = reviews.stream()
                .mapToLong(r -> r.getCriticalFindings().size())
                .sum();

        return new ReviewStatistics(
                totalReviews,
                completedReviews,
                averageScore,
                totalFindings,
                criticalIssues
        );
    }

    /**
     * Record class for review statistics
     */
    public record ReviewStatistics(
            long totalReviews,
            long completedReviews,
            double averageQualityScore,
            long totalFindings,
            long criticalIssues
    ) {}
}
