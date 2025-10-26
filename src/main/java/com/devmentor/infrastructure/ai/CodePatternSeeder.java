package com.devmentor.infrastructure.ai;

import com.devmentor.application.ai.RAGService;
import com.devmentor.domain.codereview.CodePattern;
import com.devmentor.domain.codereview.FindingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with Spring Boot best practices and patterns
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CodePatternSeeder implements CommandLineRunner {

    private final RAGService ragService;
    
    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Override
    public void run(String... args) {
        // Skip seeding if API key is not configured
        if (apiKey == null || apiKey.startsWith("your-") || apiKey.equals("sk-test-dummy-key")) {
            log.warn("Skipping code pattern seeding - OpenAI API key not configured");
            return;
        }
        
        log.info("Seeding code patterns...");
        seedSecurityPatterns();
        seedPerformancePatterns();
        seedBestPractices();
        log.info("Code patterns seeded successfully");
    }

    private void seedSecurityPatterns() {
        // SQL Injection Prevention
        ragService.storePattern(CodePattern.builder()
                .title("Use Parameterized Queries to Prevent SQL Injection")
                .description("Always use JPA or parameterized queries instead of string concatenation")
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        // Good: Using JPA
                        @Query("SELECT u FROM User u WHERE u.email = :email")
                        Optional<User> findByEmail(@Param("email") String email);
                        """)
                .badExample("""
                        // Bad: String concatenation
                        String query = "SELECT * FROM users WHERE email = '" + email + "'";
                        """)
                .resourcesUrl("https://owasp.org/www-community/attacks/SQL_Injection")
                .build());

        // Password Storage
        ragService.storePattern(CodePattern.builder()
                .title("Use BCrypt for Password Hashing")
                .description("Never store passwords in plain text. Use BCrypt or Argon2")
                .category(FindingCategory.SECURITY_VULNERABILITY)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        @Bean
                        public PasswordEncoder passwordEncoder() {
                            return new BCryptPasswordEncoder();
                        }

                        String hashedPassword = passwordEncoder.encode(rawPassword);
                        """)
                .badExample("""
                        // Bad: Plain text password
                        user.setPassword(rawPassword);
                        """)
                .build());
    }

    private void seedPerformancePatterns() {
        // N+1 Query Problem
        ragService.storePattern(CodePattern.builder()
                .title("Avoid N+1 Query Problem with @EntityGraph")
                .description("Use @EntityGraph or fetch joins to avoid multiple database queries")
                .category(FindingCategory.PERFORMANCE)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        @EntityGraph(attributePaths = {"orders", "orders.items"})
                        @Query("SELECT u FROM User u WHERE u.id = :id")
                        Optional<User> findByIdWithOrders(@Param("id") Long id);
                        """)
                .badExample("""
                        // Bad: Lazy loading causes N+1
                        User user = userRepository.findById(id);
                        for (Order order : user.getOrders()) {
                            // Each iteration triggers a query
                            order.getItems();
                        }
                        """)
                .build());

        // Caching
        ragService.storePattern(CodePattern.builder()
                .title("Use @Cacheable for Frequently Accessed Data")
                .description("Cache frequently accessed, rarely changing data")
                .category(FindingCategory.PERFORMANCE)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        @Cacheable(value = "users", key = "#id")
                        public User findById(Long id) {
                            return userRepository.findById(id).orElseThrow();
                        }
                        """)
                .build());
    }

    private void seedBestPractices() {
        // Constructor Injection
        ragService.storePattern(CodePattern.builder()
                .title("Use Constructor Injection Instead of Field Injection")
                .description("Constructor injection makes dependencies explicit and enables immutability")
                .category(FindingCategory.BEST_PRACTICE)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        @Service
                        @RequiredArgsConstructor
                        public class UserService {
                            private final UserRepository userRepository;
                            // Constructor injection via Lombok
                        }
                        """)
                .badExample("""
                        @Service
                        public class UserService {
                            @Autowired
                            private UserRepository userRepository; // Field injection
                        }
                        """)
                .build());

        // Exception Handling
        ragService.storePattern(CodePattern.builder()
                .title("Use @ControllerAdvice for Global Exception Handling")
                .description("Centralize exception handling with @ControllerAdvice")
                .category(FindingCategory.ERROR_HANDLING)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        @RestControllerAdvice
                        public class GlobalExceptionHandler {
                            @ExceptionHandler(ResourceNotFoundException.class)
                            public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(new ErrorResponse(ex.getMessage()));
                            }
                        }
                        """)
                .build());

        // Validation
        ragService.storePattern(CodePattern.builder()
                .title("Use Bean Validation Annotations")
                .description("Use @Valid and Jakarta validation annotations for input validation")
                .category(FindingCategory.BEST_PRACTICE)
                .patternType(CodePattern.PatternType.BEST_PRACTICE)
                .goodExample("""
                        public class CreateUserRequest {
                            @NotBlank(message = "Username is required")
                            @Size(min = 3, max = 50)
                            private String username;

                            @Email
                            @NotBlank
                            private String email;
                        }

                        @PostMapping
                        public User create(@Valid @RequestBody CreateUserRequest request) {
                            // Validation happens automatically
                        }
                        """)
                .build());
    }
}
