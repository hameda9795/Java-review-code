package com.devmentor.domain.codereview;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Curated knowledge base of Spring Boot and Java best practices
 * This serves as educational content for code reviews
 */
public class BestPracticesKnowledgeBase {

    /**
     * Get all best practice guidelines
     */
    public static List<BestPracticeGuideline> getAllGuidelines() {
        List<BestPracticeGuideline> guidelines = new ArrayList<>();
        
        // Spring Boot Best Practices
        guidelines.add(constructorInjection());
        guidelines.add(useTransactional());
        guidelines.add(dtoPattern());
        guidelines.add(exceptionHandling());
        guidelines.add(validationBestPractices());
        
        // Architecture Best Practices
        guidelines.add(hexagonalArchitecture());
        guidelines.add(domainPurity());
        guidelines.add(dependencyInversion());
        
        // Security Best Practices
        guidelines.add(inputValidation());
        guidelines.add(sqlInjectionPrevention());
        guidelines.add(authenticationBestPractices());
        
        // Performance Best Practices
        guidelines.add(avoidNPlusOne());
        guidelines.add(properIndexing());
        guidelines.add(caching());
        
        // Code Quality Best Practices
        guidelines.add(solidPrinciples());
        guidelines.add(cleanCode());
        guidelines.add(testingBestPractices());
        
        return guidelines;
    }

    // === SPRING BOOT BEST PRACTICES ===
    
    private static BestPracticeGuideline constructorInjection() {
        return BestPracticeGuideline.builder()
                .id("SB001")
                .title("Use Constructor Injection Instead of Field Injection")
                .category(FindingCategory.BEST_PRACTICE)
                .severity(FindingSeverity.MEDIUM)
                .description("Constructor injection is preferred over @Autowired field injection")
                .reasoning("""
                        WHY CONSTRUCTOR INJECTION:
                        1. Immutability: Dependencies can be final
                        2. Testability: Easy to inject mocks in tests without Spring context
                        3. Null Safety: Required dependencies are enforced at compile-time
                        4. Circular Dependencies: Detected early (constructor calls fail)
                        5. Better Design: Forces you to think about dependencies
                        
                        FIELD INJECTION PROBLEMS:
                        - Mutable state (no final keyword)
                        - Hidden dependencies (class looks simple but has many dependencies)
                        - Cannot instantiate class without Spring
                        - NullPointerException risk if dependency not injected
                        """)
                .antiPattern("""
                        // ANTI-PATTERN: Field Injection
                        @Service
                        public class UserService {
                            @Autowired
                            private UserRepository userRepository; // Mutable, hard to test
                            
                            @Autowired
                            private EmailService emailService;
                            
                            // Hidden dependencies, can't enforce at compile time
                        }
                        """)
                .correctPattern("""
                        // BEST PRACTICE: Constructor Injection
                        @Service
                        public class UserService {
                            private final UserRepository userRepository; // Immutable
                            private final EmailService emailService;
                            
                            // Lombok makes this even cleaner:
                            @RequiredArgsConstructor
                            public class UserService {
                                private final UserRepository userRepository;
                                private final EmailService emailService;
                                // Constructor generated automatically
                            }
                            
                            // Dependencies are clear and enforced
                            // Easy to test: new UserService(mockRepo, mockEmail)
                        }
                        """)
                .resourceLinks(EducationalResource.ResourceLinks.builder()
                        .officialDocumentation("https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection")
                        .bookReference("Spring in Action 6th Edition - Chapter 2: Dependency Injection")
                        .blogPost("https://reflectoring.io/constructor-injection/")
                        .build())
                .difficultyLevel(EducationalResource.DifficultyLevel.BEGINNER)
                .build();
    }
    
    private static BestPracticeGuideline useTransactional() {
        return BestPracticeGuideline.builder()
                .id("SB002")
                .title("Use @Transactional on Service Methods, Not Repository")
                .category(FindingCategory.BEST_PRACTICE)
                .severity(FindingSeverity.HIGH)
                .description("Transaction boundaries should be at the service layer, not repository")
                .reasoning("""
                        TRANSACTION PLACEMENT PRINCIPLES:
                        
                        Service Layer = Use Case = Transaction Boundary
                        - Service methods orchestrate multiple repository calls
                        - All-or-nothing semantics for business operations
                        - Service layer owns the transaction lifecycle
                        
                        WRONG: @Transactional on Repository
                        - Too fine-grained (each query = separate transaction)
                        - Cannot maintain consistency across multiple operations
                        - Lazy loading fails (entities detached outside transaction)
                        
                        EXAMPLE SCENARIO:
                        Creating an order requires:
                        1. Save Order
                        2. Update Product stock
                        3. Send notification
                        
                        These must be in ONE transaction (service method),
                        not three separate transactions (repository methods).
                        """)
                .antiPattern("""
                        // WRONG: Transaction on repository
                        @Repository
                        public interface OrderRepository extends JpaRepository<Order, UUID> {
                            @Transactional // Too fine-grained!
                            List<Order> findByUserId(UUID userId);
                        }
                        
                        @Service
                        public class OrderService {
                            public void createOrder(OrderRequest request) {
                                // Multiple repo calls = multiple transactions = inconsistency risk
                                orderRepository.save(order);
                                productRepository.updateStock(productId, quantity); // Separate transaction!
                                // If this fails, order is already saved! Inconsistent state.
                            }
                        }
                        """)
                .correctPattern("""
                        // CORRECT: Transaction on service
                        @Repository
                        public interface OrderRepository extends JpaRepository<Order, UUID> {
                            // No @Transactional here
                        }
                        
                        @Service
                        public class OrderService {
                            
                            @Transactional // Service = Use Case = Transaction
                            public Order createOrder(OrderRequest request) {
                                // All operations in ONE transaction
                                Order order = orderRepository.save(order);
                                productRepository.updateStock(productId, -quantity);
                                notificationService.sendOrderConfirmation(order);
                                
                                // If any operation fails, ALL rollback
                                return order;
                            }
                            
                            @Transactional(readOnly = true) // Optimization for reads
                            public Order getOrder(UUID id) {
                                return orderRepository.findById(id)
                                        .orElseThrow(() -> new NotFoundException("Order not found"));
                            }
                        }
                        """)
                .resourceLinks(EducationalResource.ResourceLinks.builder()
                        .officialDocumentation("https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction")
                        .bookReference("Spring Data JPA Best Practices - Transaction Management")
                        .blogPost("https://vladmihalcea.com/spring-transaction-best-practices/")
                        .build())
                .difficultyLevel(EducationalResource.DifficultyLevel.INTERMEDIATE)
                .build();
    }
    
    private static BestPracticeGuideline dtoPattern() {
        return BestPracticeGuideline.builder()
                .id("SB003")
                .title("Use DTOs in REST Controllers, Never Expose Entities")
                .category(FindingCategory.BEST_PRACTICE)
                .severity(FindingSeverity.HIGH)
                .description("REST API should use Data Transfer Objects (DTOs), not domain entities")
                .reasoning("""
                        WHY NEVER EXPOSE ENTITIES DIRECTLY:
                        
                        1. SECURITY: Entities may contain sensitive fields
                           - User entity has passwordHash -> exposed in JSON!
                           - Over-posting attacks (mass assignment vulnerabilities)
                        
                        2. VERSIONING: API structure coupled to database schema
                           - Change DB column = break API contract
                           - Cannot evolve database independently
                        
                        3. PERFORMANCE: Lazy loading causes N+1 queries in JSON serialization
                           - @JsonIgnore everywhere = messy
                           - Serialization triggers proxy initialization
                        
                        4. DESIGN: Violates Separation of Concerns
                           - Entity = Domain Model (business logic)
                           - DTO = API Contract (external interface)
                           - These change for different reasons
                        
                        5. JACKSON ISSUES: Bidirectional relationships cause infinite loops
                           - Need @JsonManagedReference/@JsonBackReference everywhere
                        """)
                .antiPattern("""
                        // ANTI-PATTERN: Exposing Entity Directly
                        @Entity
                        public class User {
                            private UUID id;
                            private String email;
                            private String passwordHash; // SECURITY RISK!
                            
                            @OneToMany
                            private List<Order> orders; // N+1 query when serialized!
                        }
                        
                        @RestController
                        public class UserController {
                            @GetMapping("/users/{id}")
                            public User getUser(@PathVariable UUID id) {
                                return userRepository.findById(id); // Exposes password!
                            }
                        }
                        """)
                .correctPattern("""
                        // BEST PRACTICE: Use DTOs
                        
                        // Domain Entity (internal)
                        @Entity
                        class UserEntity {
                            private UUID id;
                            private String email;
                            private String passwordHash; // Internal only
                            @OneToMany
                            private List<Order> orders;
                        }
                        
                        // API DTO (external contract)
                        public record UserDto(
                            UUID id,
                            String email,
                            String displayName
                            // No password, controlled fields
                        ) {}
                        
                        @RestController
                        public class UserController {
                            
                            @GetMapping("/users/{id}")
                            public UserDto getUser(@PathVariable UUID id) {
                                User user = userService.findById(id);
                                return new UserDto(
                                    user.getId(),
                                    user.getEmail(),
                                    user.getDisplayName()
                                ); // Explicit mapping, full control
                            }
                            
                            // Use MapStruct or ModelMapper for complex mappings
                        }
                        """)
                .resourceLinks(EducationalResource.ResourceLinks.builder()
                        .officialDocumentation("https://martinfowler.com/eaaCatalog/dataTransferObject.html")
                        .blogPost("https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application")
                        .build())
                .difficultyLevel(EducationalResource.DifficultyLevel.INTERMEDIATE)
                .build();
    }

    // === ARCHITECTURE BEST PRACTICES ===
    
    private static BestPracticeGuideline hexagonalArchitecture() {
        return BestPracticeGuideline.builder()
                .id("ARCH001")
                .title("Hexagonal Architecture: Dependencies Point Inward")
                .category(FindingCategory.ARCHITECTURE)
                .severity(FindingSeverity.CRITICAL)
                .description("Domain layer should have no dependencies on outer layers")
                .reasoning("""
                        HEXAGONAL ARCHITECTURE (Ports & Adapters):
                        
                        LAYERS (Inner to Outer):
                        1. Domain (Core Business Logic) - No dependencies
                        2. Application (Use Cases) - Depends on Domain
                        3. Infrastructure (Technical Details) - Depends on Application & Domain
                        4. Interfaces (REST, CLI, etc.) - Depends on Application
                        
                        DEPENDENCY RULE:
                        Source code dependencies must point INWARD only.
                        Inner layers must not know about outer layers.
                        
                        BENEFITS:
                        - Business logic isolated from frameworks
                        - Easy to swap infrastructure (JPA -> MongoDB)
                        - Testable without Spring/HTTP/Database
                        - Framework upgrades don't touch domain
                        - Domain is database-agnostic
                        """)
                .correctPattern("""
                        // Domain Layer (Pure Java, no imports)
                        package com.example.domain;
                        
                        public class Order {
                            private Money total;
                            
                            public void apply(Discount discount) {
                                // Pure business logic
                            }
                        }
                        
                        // Port (interface in domain)
                        public interface OrderRepository {
                            Order save(Order order);
                        }
                        
                        // Application Layer (orchestration)
                        package com.example.application;
                        
                        public class OrderService {
                            private final OrderRepository repository; // Depends on domain port
                            
                            public Order placeOrder(OrderRequest request) {
                                Order order = Order.create(request);
                                return repository.save(order);
                            }
                        }
                        
                        // Infrastructure Layer (adapter implementation)
                        package com.example.infrastructure;
                        
                        @Repository
                        public class JpaOrderRepository implements OrderRepository {
                            // JPA implementation details
                        }
                        """)
                .resourceLinks(EducationalResource.ResourceLinks.builder()
                        .bookReference("Clean Architecture by Robert Martin - Chapter 22")
                        .blogPost("https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html")
                        .build())
                .difficultyLevel(EducationalResource.DifficultyLevel.ADVANCED)
                .build();
    }

    // === PERFORMANCE BEST PRACTICES ===
    
    private static BestPracticeGuideline avoidNPlusOne() {
        return BestPracticeGuideline.builder()
                .id("PERF001")
                .title("Avoid N+1 Query Problem with @EntityGraph or Fetch Joins")
                .category(FindingCategory.PERFORMANCE)
                .severity(FindingSeverity.CRITICAL)
                .description("Loading entities with lazy relationships causes N+1 queries")
                .reasoning("""
                        N+1 QUERY PROBLEM:
                        
                        SCENARIO: Fetch 10 Orders, each has a User
                        - 1 query to get 10 orders
                        - 10 queries to get each order's user (one per order)
                        - Total: 11 queries instead of 1!
                        
                        IMPACT:
                        - 100 orders = 101 queries (devastating performance)
                        - Database connection pool exhaustion
                        - Response time: 50ms -> 5 seconds
                        - Cannot scale
                        
                        DETECTION:
                        - Enable Hibernate SQL logging
                        - Look for: "select ... from users where id = ?" repeated many times
                        - Use p6spy to see actual query count
                        """)
                .antiPattern("""
                        // PROBLEM: Lazy loading causes N+1
                        @Entity
                        public class Order {
                            @ManyToOne(fetch = FetchType.LAZY) // Default is LAZY
                            private User user;
                        }
                        
                        // Controller
                        List<Order> orders = orderRepository.findAll(); // 1 query
                        for (Order order : orders) {
                            String name = order.getUser().getName(); // N queries!
                        }
                        """)
                .correctPattern("""
                        // SOLUTION 1: @EntityGraph (recommended)
                        @Repository
                        public interface OrderRepository extends JpaRepository<Order, UUID> {
                            
                            @EntityGraph(attributePaths = {"user", "items"})
                            List<Order> findAll();
                            
                            // Single query with LEFT JOIN!
                        }
                        
                        // SOLUTION 2: JPQL Fetch Join
                        @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.status = :status")
                        List<Order> findByStatus(@Param("status") OrderStatus status);
                        
                        // SOLUTION 3: Custom @EntityGraph for specific queries
                        @Entity
                        @NamedEntityGraph(
                            name = "Order.withUserAndItems",
                            attributeNodes = {
                                @NamedAttributeNode("user"),
                                @NamedAttributeNode("items")
                            }
                        )
                        public class Order {
                            @ManyToOne(fetch = FetchType.LAZY)
                            private User user;
                            
                            @OneToMany
                            private List<OrderItem> items;
                        }
                        
                        // Usage
                        @EntityGraph("Order.withUserAndItems")
                        List<Order> findByStatus(OrderStatus status);
                        """)
                .resourceLinks(EducationalResource.ResourceLinks.builder()
                        .officialDocumentation("https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph")
                        .blogPost("https://vladmihalcea.com/n-plus-1-query-problem/")
                        .build())
                .difficultyLevel(EducationalResource.DifficultyLevel.INTERMEDIATE)
                .build();
    }

    // Placeholder implementations for other guidelines
    private static BestPracticeGuideline exceptionHandling() { return BestPracticeGuideline.builder().id("SB004").title("Global Exception Handling").build(); }
    private static BestPracticeGuideline validationBestPractices() { return BestPracticeGuideline.builder().id("SB005").title("Bean Validation").build(); }
    private static BestPracticeGuideline domainPurity() { return BestPracticeGuideline.builder().id("ARCH002").title("Domain Purity").build(); }
    private static BestPracticeGuideline dependencyInversion() { return BestPracticeGuideline.builder().id("ARCH003").title("Dependency Inversion").build(); }
    private static BestPracticeGuideline inputValidation() { return BestPracticeGuideline.builder().id("SEC001").title("Input Validation").build(); }
    private static BestPracticeGuideline sqlInjectionPrevention() { return BestPracticeGuideline.builder().id("SEC002").title("SQL Injection Prevention").build(); }
    private static BestPracticeGuideline authenticationBestPractices() { return BestPracticeGuideline.builder().id("SEC003").title("Authentication").build(); }
    private static BestPracticeGuideline properIndexing() { return BestPracticeGuideline.builder().id("PERF002").title("Database Indexing").build(); }
    private static BestPracticeGuideline caching() { return BestPracticeGuideline.builder().id("PERF003").title("Caching Strategy").build(); }
    private static BestPracticeGuideline solidPrinciples() { return BestPracticeGuideline.builder().id("CODE001").title("SOLID Principles").build(); }
    private static BestPracticeGuideline cleanCode() { return BestPracticeGuideline.builder().id("CODE002").title("Clean Code").build(); }
    private static BestPracticeGuideline testingBestPractices() { return BestPracticeGuideline.builder().id("TEST001").title("Testing Best Practices").build(); }

    @Getter
    @AllArgsConstructor
    @lombok.Builder
    public static class BestPracticeGuideline {
        private String id;
        private String title;
        private FindingCategory category;
        private FindingSeverity severity;
        private String description;
        private String reasoning;
        private String antiPattern;
        private String correctPattern;
        private EducationalResource.ResourceLinks resourceLinks;
        private EducationalResource.DifficultyLevel difficultyLevel;
    }
}
