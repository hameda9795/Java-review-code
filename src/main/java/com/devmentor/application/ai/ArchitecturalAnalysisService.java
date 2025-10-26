package com.devmentor.application.ai;

import com.devmentor.domain.codereview.FindingCategory;
import com.devmentor.domain.codereview.FindingSeverity;
import com.devmentor.domain.codereview.ReviewFinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for analyzing project architecture and design patterns
 * Goes beyond individual file analysis to evaluate overall system design
 */
@Service
@Slf4j
public class ArchitecturalAnalysisService {

    /**
     * Analyze project structure for architectural violations
     */
    public List<ReviewFinding> analyzeArchitecture(Map<String, String> files) {
        log.info("Starting architectural analysis of {} files", files.size());
        
        List<ReviewFinding> findings = new ArrayList<>();

        findings.addAll(checkHexagonalArchitecture(files));
        findings.addAll(checkLayerDependencies(files));
        findings.addAll(checkDomainPurity(files));
        findings.addAll(checkSOLIDPrinciples(files));
        findings.addAll(checkDesignPatterns(files));
        
        log.info("Architectural analysis complete: {} findings", findings.size());
        return findings;
    }

    /**
     * Verify hexagonal architecture boundaries
     */
    private List<ReviewFinding> checkHexagonalArchitecture(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Check if domain layer has any infrastructure dependencies
        Map<String, String> domainFiles = files.entrySet().stream()
                .filter(e -> e.getKey().contains("/domain/"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, String> entry : domainFiles.entrySet()) {
            String content = entry.getValue();
            String filePath = entry.getKey();

            // Check for Spring annotations in domain (violation)
            if (content.contains("@Entity") || content.contains("@Table") || 
                content.contains("@Column") || content.contains("@ManyToOne")) {
                
                findings.add(ReviewFinding.builder()
                        .title("JPA Annotations in Domain Layer Violate Clean Architecture")
                        .description("Domain entities should not depend on JPA (@Entity, @Table). This creates tight coupling to infrastructure.")
                        .severity(FindingSeverity.HIGH)
                        .category(FindingCategory.ARCHITECTURE)
                        .filePath(filePath)
                        .codeSnippet("@Entity, @Table, @Column annotations found in domain")
                        .suggestedFix("""
                                // PROBLEM: Domain entity tightly coupled to JPA
                                @Entity
                                @Table(name = "users")
                                public class User {
                                    @Column(name = "email")
                                    private String email;
                                }
                                
                                // SOLUTION: Pure domain entity + separate JPA entity
                                // Domain layer (pure):
                                public class User {
                                    private final Email email; // Value object
                                    // Business logic only
                                }
                                
                                // Infrastructure layer (JPA mapping):
                                @Entity
                                @Table(name = "users")
                                class UserJpaEntity {
                                    @Column(name = "email")
                                    private String email;
                                    
                                    public User toDomain() {
                                        return new User(new Email(email));
                                    }
                                }
                                """)
                        .explanation("""
                                This violates the Dependency Inversion Principle (SOLID) and Hexagonal Architecture.
                                
                                WHY IT MATTERS:
                                - Domain should express business rules, not database concerns
                                - Cannot swap persistence (e.g., JPA -> MongoDB) without changing domain
                                - Harder to test domain logic (requires JPA setup)
                                - Business logic and technical details are mixed
                                
                                SOLUTION APPROACH:
                                1. Keep domain entities pure POJOs with business logic
                                2. Create separate JPA entities in infrastructure layer
                                3. Use mapper/adapter to convert between them
                                4. Repository interface in domain, implementation in infrastructure
                                
                                TRADE-OFF: More boilerplate code, but much better separation
                                WHEN TO IGNORE: Very simple CRUD apps with no complex business logic
                                
                                RESOURCES:
                                - Clean Architecture by Robert Martin - Chapter 22
                                - DDD: Separate persistence model from domain model
                                """)
                        .resourcesUrl("https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html")
                        .build());
            }

            // Check for @Service, @Component in domain
            if (content.contains("import org.springframework")) {
                findings.add(ReviewFinding.builder()
                        .title("Spring Framework Dependency in Domain Layer")
                        .description("Domain layer should not import Spring framework classes. This violates the dependency rule.")
                        .severity(FindingSeverity.CRITICAL)
                        .category(FindingCategory.ARCHITECTURE)
                        .filePath(filePath)
                        .suggestedFix("""
                                // WRONG: Domain depending on framework
                                import org.springframework.stereotype.Service;
                                
                                @Service
                                public class OrderService { }
                                
                                // CORRECT: Pure domain service
                                public class OrderService {
                                    // Pure business logic, no framework dependencies
                                }
                                
                                // Application layer wires it up with Spring
                                @Service
                                public class OrderApplicationService {
                                    private final OrderService orderService;
                                    // Spring manages this, not domain
                                }
                                """)
                        .explanation("""
                                PRINCIPLE: The domain is the heart of the application and should be framework-agnostic.
                                
                                This is a CRITICAL violation because:
                                1. Cannot use domain logic outside Spring context
                                2. Framework coupling makes testing harder
                                3. Framework upgrade = potential domain changes
                                4. Violates Dependency Inversion (high-level modules shouldn't depend on low-level)
                                
                                The domain should be pure Java that could run in any context.
                                """)
                        .build());
            }
        }

        return findings;
    }

    /**
     * Check for layer dependency violations
     */
    private List<ReviewFinding> checkLayerDependencies(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Domain should never import from application or infrastructure
        Map<String, String> domainFiles = files.entrySet().stream()
                .filter(e -> e.getKey().contains("/domain/"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, String> entry : domainFiles.entrySet()) {
            String content = entry.getValue();
            
            if (content.contains("import com.devmentor.application") || 
                content.contains("import com.devmentor.infrastructure") ||
                content.contains("import com.devmentor.interfaces")) {
                
                findings.add(ReviewFinding.builder()
                        .title("Layer Dependency Violation: Domain Depends on Outer Layers")
                        .description("Domain layer imports from application/infrastructure/interfaces. Dependencies must point INWARD.")
                        .severity(FindingSeverity.CRITICAL)
                        .category(FindingCategory.ARCHITECTURE)
                        .filePath(entry.getKey())
                        .explanation("""
                                HEXAGONAL ARCHITECTURE RULE: Dependencies must always point toward the domain (center).
                                
                                Allowed dependency directions:
                                Interfaces -> Application -> Domain
                                Infrastructure -> Application -> Domain
                                
                                NEVER: Domain -> Application/Infrastructure
                                
                                WHY: Domain contains business rules and should be the most stable part.
                                If domain depends on outer layers, changes in infrastructure break business logic.
                                
                                FIX: Use Dependency Inversion (define interfaces in domain, implement in outer layers)
                                """)
                        .build());
            }
        }

        return findings;
    }

    /**
     * Ensure domain layer contains only business logic
     */
    private List<ReviewFinding> checkDomainPurity(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();

        Map<String, String> domainFiles = files.entrySet().stream()
                .filter(e -> e.getKey().contains("/domain/"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, String> entry : domainFiles.entrySet()) {
            String content = entry.getValue();
            String filePath = entry.getKey();

            // Check for HTTP-specific code in domain
            if (content.contains("import org.springframework.http") ||
                content.contains("HttpStatus") ||
                content.contains("ResponseEntity")) {
                
                findings.add(ReviewFinding.builder()
                        .title("HTTP Concerns Leak into Domain Layer")
                        .description("Domain entities/services should not deal with HTTP concepts (HttpStatus, ResponseEntity, etc.)")
                        .severity(FindingSeverity.HIGH)
                        .category(FindingCategory.ARCHITECTURE)
                        .filePath(filePath)
                        .suggestedFix("""
                                // WRONG: Domain aware of HTTP
                                public class OrderService {
                                    public ResponseEntity<Order> createOrder() {
                                        return ResponseEntity.ok(order);
                                    }
                                }
                                
                                // CORRECT: Domain returns domain objects
                                public class OrderService {
                                    public Order createOrder() {
                                        // Pure business logic
                                        return order;
                                    }
                                }
                                
                                // Controller handles HTTP concerns
                                @RestController
                                public class OrderController {
                                    public ResponseEntity<OrderDto> createOrder() {
                                        Order order = orderService.createOrder();
                                        return ResponseEntity.ok(toDto(order));
                                    }
                                }
                                """)
                        .explanation("""
                                Domain should express business concepts, not technical delivery mechanisms.
                                
                                BENEFIT: Can reuse domain logic in:
                                - REST API
                                - GraphQL API  
                                - Message queue consumers
                                - Batch jobs
                                - CLI tools
                                
                                HTTP is just ONE way to deliver domain functionality.
                                """)
                        .build());
            }
        }

        return findings;
    }

    /**
     * Check SOLID principles adherence
     */
    private List<ReviewFinding> checkSOLIDPrinciples(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String content = entry.getValue();
            String filePath = entry.getKey();

            // Single Responsibility: Check for "Manager", "Handler", "Util" classes (code smell)
            if (filePath.contains("Manager.java") || filePath.contains("Util.java") || 
                filePath.contains("Helper.java")) {
                
                findings.add(ReviewFinding.builder()
                        .title("Potential Single Responsibility Violation: Generic Class Name")
                        .description("Classes named 'Manager', 'Util', 'Helper' often violate SRP by doing too many things.")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.BEST_PRACTICE)
                        .filePath(filePath)
                        .suggestedFix("""
                                // SMELL: Vague name suggests multiple responsibilities
                                public class UserManager {
                                    void createUser() {}
                                    void sendEmail() {}
                                    void generateReport() {}
                                    void validateInput() {}
                                }
                                
                                // BETTER: Split into focused classes
                                public class UserCreationService {
                                    User createUser(UserData data) { }
                                }
                                
                                public class UserNotificationService {
                                    void notifyUserCreated(User user) { }
                                }
                                
                                public class UserReportGenerator {
                                    Report generateUserReport() { }
                                }
                                
                                // Each class has ONE reason to change
                                """)
                        .explanation("""
                                Single Responsibility Principle: A class should have only ONE reason to change.
                                
                                SYMPTOMS of SRP violation:
                                - Generic names: Manager, Handler, Util, Helper
                                - Many methods with unrelated functionality
                                - Hard to name the class clearly
                                
                                BENEFITS of SRP:
                                - Easier testing (smaller scope)
                                - Easier to understand
                                - Less coupling (fewer dependencies)
                                - Changes in one feature don't risk breaking others
                                
                                REFACTORING STRATEGY:
                                1. Group methods by responsibility
                                2. Extract each group into a focused class
                                3. Name classes after what they DO, not what they are
                                
                                RESOURCE: Clean Code by Robert Martin - Chapter 10
                                """)
                        .resourcesUrl("https://en.wikipedia.org/wiki/Single-responsibility_principle")
                        .build());
            }

            // Dependency Inversion: Check for "new" keyword in services (should use DI)
            if (filePath.contains("Service.java") && content.contains(" new ") &&
                !content.contains("new ArrayList") && !content.contains("new HashMap")) {
                
                findings.add(ReviewFinding.builder()
                        .title("Possible Dependency Inversion Violation: Using 'new' in Service")
                        .description("Services should depend on abstractions (interfaces) via dependency injection, not create dependencies with 'new'.")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.ARCHITECTURE)
                        .filePath(filePath)
                        .suggestedFix("""
                                // WRONG: Hard-coded dependency
                                public class OrderService {
                                    private EmailSender emailSender = new SmtpEmailSender(); // Tight coupling
                                    
                                    public void processOrder() {
                                        emailSender.send(); // Cannot mock for testing
                                    }
                                }
                                
                                // CORRECT: Dependency Injection with interface
                                public class OrderService {
                                    private final EmailSender emailSender; // Depend on abstraction
                                    
                                    public OrderService(EmailSender emailSender) { // Injected
                                        this.emailSender = emailSender;
                                    }
                                    
                                    public void processOrder() {
                                        emailSender.send(); // Can inject mock/stub
                                    }
                                }
                                
                                // Spring wires it up:
                                @Service
                                public class OrderService {
                                    private final EmailSender emailSender;
                                    // Constructor injection (recommended)
                                }
                                """)
                        .explanation("""
                                Dependency Inversion Principle (SOLID):
                                - High-level modules should not depend on low-level modules
                                - Both should depend on abstractions (interfaces)
                                
                                BENEFITS:
                                - Easy to swap implementations (SMTP -> SendGrid)
                                - Testable (inject mocks)
                                - Loose coupling
                                - Follows Hollywood Principle: "Don't call us, we'll call you"
                                
                                SPRING BOOT BEST PRACTICE:
                                - Use constructor injection (not field injection)
                                - Depend on interfaces, not concrete classes
                                - Let Spring manage object lifecycle
                                
                                EXCEPTION: OK to use 'new' for:
                                - Value objects (new Money(100))
                                - DTOs (new UserDto())
                                - Collections (new ArrayList<>())
                                """)
                        .build());
            }
        }

        return findings;
    }

    /**
     * Detect missing or incorrect design patterns
     */
    private List<ReviewFinding> checkDesignPatterns(Map<String, String> files) {
        List<ReviewFinding> findings = new ArrayList<>();

        // Check for missing Builder pattern on complex constructors
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String content = entry.getValue();
            String filePath = entry.getKey();

            // Simple heuristic: Constructor with 5+ parameters should use Builder
            Pattern constructorPattern = Pattern.compile("public\\s+\\w+\\s*\\([^)]*,[^)]*,[^)]*,[^)]*,[^)]*");
            if (constructorPattern.matcher(content).find() && !content.contains("@Builder")) {
                
                findings.add(ReviewFinding.builder()
                        .title("Missing Builder Pattern for Complex Constructor")
                        .description("Constructor with 5+ parameters should use Builder pattern for readability and flexibility.")
                        .severity(FindingSeverity.LOW)
                        .category(FindingCategory.DESIGN_PATTERN)
                        .filePath(filePath)
                        .suggestedFix("""
                                // BEFORE: Hard to read and maintain
                                public User(String name, String email, int age, String address, String phone) {
                                    // Which parameter is which?
                                }
                                User user = new User("John", "john@example.com", 30, "123 Main St", "555-1234");
                                
                                // AFTER: Builder pattern (using Lombok)
                                @Builder
                                public class User {
                                    private String name;
                                    private String email;
                                    private int age;
                                    private String address;
                                    private String phone;
                                }
                                
                                // Clear and readable
                                User user = User.builder()
                                        .name("John")
                                        .email("john@example.com")
                                        .age(30)
                                        .address("123 Main St")
                                        .phone("555-1234")
                                        .build();
                                
                                // BENEFITS:
                                // - Named parameters (clear what each value means)
                                // - Optional parameters (omit what you don't need)
                                // - Immutable objects (no setters)
                                // - Compile-time safety
                                """)
                        .explanation("""
                                Builder Pattern (GoF Creational Pattern):
                                
                                WHEN TO USE:
                                - 4+ constructor parameters
                                - Many optional parameters
                                - Need immutable objects
                                - Telescoping constructor problem
                                
                                IMPLEMENTATION OPTIONS:
                                1. Lombok @Builder (easiest)
                                2. Manual builder class
                                3. Records with custom builders (Java 14+)
                                
                                RESOURCE: Effective Java Item 2: Consider a builder when faced with many constructor parameters
                                """)
                        .resourcesUrl("https://projectlombok.org/features/Builder")
                        .build());
            }
        }

        return findings;
    }
}
