package com.devmentor.infrastructure.seed;

import com.devmentor.domain.codereview.*;
import com.devmentor.domain.user.SubscriptionTier;
import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import com.devmentor.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Database seeder for development and testing
 * Only runs in 'dev' or 'test' profiles
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting database seeding...");

        try {
            seedUsers();
            seedCodeReviews();
            log.info("Database seeding completed successfully!");
        } catch (Exception e) {
            log.error("Error during database seeding", e);
        }
    }

    private void seedUsers() {
        log.info("Seeding users...");

        // Check if users already exist
        if (userRepository.findByUsername("demo").isPresent()) {
            log.info("Users already exist, skipping user seeding");
            return;
        }

        // Demo user (FREE tier)
        User demoUser = User.builder()
                .username("demo")
                .email("demo@devmentor.ai")
                .passwordHash(passwordEncoder.encode("Demo123!"))
                .fullName("Demo User")
                .role(UserRole.USER)
                .subscriptionTier(SubscriptionTier.FREE)
                .isActive(true)
                .emailVerified(true)
                .reviewsCount(2)
                .totalFilesReviewed(15)
                .build();

        userRepository.save(demoUser);
        log.info("Created demo user: {}", demoUser.getUsername());

        // Premium user
        User premiumUser = User.builder()
                .username("premium")
                .email("premium@devmentor.ai")
                .passwordHash(passwordEncoder.encode("Premium123!"))
                .fullName("Premium User")
                .role(UserRole.USER)
                .subscriptionTier(SubscriptionTier.PREMIUM)
                .isActive(true)
                .emailVerified(true)
                .reviewsCount(25)
                .totalFilesReviewed(150)
                .build();

        userRepository.save(premiumUser);
        log.info("Created premium user: {}", premiumUser.getUsername());

        // Admin user
        User adminUser = User.builder()
                .username("admin")
                .email("admin@devmentor.ai")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .fullName("Admin User")
                .role(UserRole.ADMIN)
                .subscriptionTier(SubscriptionTier.PREMIUM)
                .isActive(true)
                .emailVerified(true)
                .reviewsCount(100)
                .totalFilesReviewed(500)
                .build();

        userRepository.save(adminUser);
        log.info("Created admin user: {}", adminUser.getUsername());
    }

    private void seedCodeReviews() {
        log.info("Seeding code reviews...");

        User demoUser = userRepository.findByUsername("demo").orElse(null);
        if (demoUser == null) {
            log.warn("Demo user not found, skipping code review seeding");
            return;
        }

        // Check if reviews already exist
        if (!codeReviewRepository.findByReviewerId(demoUser.getId()).isEmpty()) {
            log.info("Code reviews already exist, skipping review seeding");
            return;
        }

        // Create sample review 1 - Completed with good score
        CodeReview goodReview = CodeReview.builder()
                .reviewer(demoUser)
                .title("Spring Boot REST API Review")
                .description("Review of a clean Spring Boot REST API implementation")
                .status(ReviewStatus.COMPLETED)
                .totalFilesAnalyzed(5)
                .totalLinesAnalyzed(450)
                .tokensUsed(2500)
                .costUsd(0.05)
                .build();

        goodReview.setStartedAt(LocalDateTime.now().minusHours(2));
        goodReview.setCompletedAt(LocalDateTime.now().minusHours(1));

        CodeQualityScore goodScore = CodeQualityScore.calculate(85, 80, 90, 85, 75);
        goodReview.complete(goodScore);

        // Add findings
        goodReview.addFinding(ReviewFinding.builder()
                .title("Consider using constructor injection")
                .description("Field injection is used in UserController. Constructor injection is preferred for better testability.")
                .severity(FindingSeverity.LOW)
                .category(FindingCategory.BEST_PRACTICE)
                .filePath("src/main/java/com/example/UserController.java")
                .lineNumber(15)
                .codeSnippet("@Autowired\\nprivate UserService userService;")
                .suggestedFix("Use constructor injection instead:\\n\\nprivate final UserService userService;\\n\\npublic UserController(UserService userService) {\\n    this.userService = userService;\\n}")
                .build());

        goodReview.addFinding(ReviewFinding.builder()
                .title("Add input validation")
                .description("Request DTOs should have validation annotations")
                .severity(FindingSeverity.MEDIUM)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .filePath("src/main/java/com/example/dto/CreateUserRequest.java")
                .lineNumber(10)
                .codeSnippet("private String email;")
                .suggestedFix("Add validation: @Email @NotBlank private String email;")
                .build());

        codeReviewRepository.save(goodReview);
        log.info("Created sample review: {}", goodReview.getTitle());

        // Create sample review 2 - Completed with issues
        CodeReview issueReview = CodeReview.builder()
                .reviewer(demoUser)
                .title("Legacy Code Refactoring Review")
                .description("Review of legacy code with multiple issues")
                .status(ReviewStatus.COMPLETED)
                .totalFilesAnalyzed(8)
                .totalLinesAnalyzed(1200)
                .tokensUsed(5000)
                .costUsd(0.10)
                .build();

        issueReview.setStartedAt(LocalDateTime.now().minusDays(1));
        issueReview.setCompletedAt(LocalDateTime.now().minusDays(1).plusHours(2));

        CodeQualityScore issueScore = CodeQualityScore.calculate(50, 55, 60, 45, 40);
        issueReview.complete(issueScore);

        // Add critical finding
        issueReview.addFinding(ReviewFinding.builder()
                .title("SQL Injection Vulnerability")
                .description("Direct string concatenation in SQL query allows SQL injection")
                .severity(FindingSeverity.CRITICAL)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .filePath("src/main/java/com/example/UserRepository.java")
                .lineNumber(45)
                .codeSnippet("String query = \\\"SELECT * FROM users WHERE id = \\\" + userId;")
                .suggestedFix("Use parameterized queries or JPA methods")
                .build());

        issueReview.addFinding(ReviewFinding.builder()
                .title("Hardcoded credentials detected")
                .description("Database password is hardcoded in the source code")
                .severity(FindingSeverity.HIGH)
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .filePath("src/main/java/com/example/DatabaseConfig.java")
                .lineNumber(20)
                .codeSnippet("String password = \\\"admin123\\\";")
                .suggestedFix("Move credentials to environment variables or secure vault")
                .build());

        issueReview.addFinding(ReviewFinding.builder()
                .title("N+1 query problem")
                .description("Multiple database queries in a loop causing performance issues")
                .severity(FindingSeverity.HIGH)
                .category(FindingCategory.PERFORMANCE)
                .filePath("src/main/java/com/example/OrderService.java")
                .lineNumber(78)
                .codeSnippet("for (Order order : orders) {\\n    Customer customer = customerRepo.findById(order.getCustomerId());\\n}")
                .suggestedFix("Use JOIN FETCH or batch loading to reduce queries")
                .build());

        codeReviewRepository.save(issueReview);
        log.info("Created sample review: {}", issueReview.getTitle());
    }
}
