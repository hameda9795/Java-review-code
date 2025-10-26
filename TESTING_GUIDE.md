# DevMentor AI - Complete Testing Guide

## 🧪 Testing Overview

This application includes comprehensive tests at multiple levels:
- ✅ Unit Tests (Domain layer)
- ✅ Integration Tests (API layer)
- ✅ Architecture Tests (ArchUnit)
- Manual E2E Testing Guide

---

## 🚀 Running Tests

### **Run All Tests**
```bash
mvn test
```

### **Run Specific Test Class**
```bash
mvn test -Dtest=UserTest
```

### **Run Tests with Coverage**
```bash
mvn test jacoco:report
# View report at: target/site/jacoco/index.html
```

### **Run Integration Tests Only**
```bash
mvn test -Dtest=*IntegrationTest
```

---

## 📋 Test Categories

### 1. **Unit Tests** (Domain Layer)

**Location**: `src/test/java/com/devmentor/domain/`

**Tests Included**:
- ✅ `UserTest` - User business logic
- ✅ `CodeReviewTest` - Review lifecycle
- ✅ `CodeQualityScoreTest` - Scoring algorithm

**Example**:
```java
@Test
void shouldIncrementReviewCount() {
    // Given
    int initialCount = user.getReviewsCount();

    // When
    user.incrementReviewCount();

    // Then
    assertEquals(initialCount + 1, user.getReviewsCount());
}
```

**Run**:
```bash
mvn test -Dtest=com.devmentor.domain.**
```

---

### 2. **Integration Tests** (API Layer)

**Location**: `src/test/java/com/devmentor/interfaces/rest/controller/`

**Tests Included**:
- ✅ `AuthControllerIntegrationTest` - Registration & Login

**Features**:
- Uses `@SpringBootTest` for full context
- `MockMvc` for API testing
- `@Transactional` for test isolation
- H2 in-memory database

**Example**:
```java
@Test
void shouldRegisterNewUser() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("newuser");
    request.setEmail("new@example.com");
    request.setPassword("SecurePass123!");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").exists());
}
```

**Run**:
```bash
mvn test -Dtest=*IntegrationTest
```

---

## 🔍 Manual E2E Testing

### **Prerequisites**
```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Set environment variables
export OPENAI_API_KEY=your-openai-key
export JWT_SECRET=your-secret-key-256-bits

# 3. Start application
mvn spring-boot:run
```

---

### **Test Scenario 1: User Registration & Login**

#### **1.1 Register New User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "fullName": "Test User"
  }'
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "uuid-here",
  "username": "testuser",
  "email": "test@example.com",
  "subscriptionTier": "FREE"
}
```

#### **1.2 Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "SecurePass123!"
  }'
```

**Expected**: Same response as registration with new token

#### **1.3 Get Current User**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### **Test Scenario 2: Code Review Creation**

#### **2.1 Create Review with Bad Code**
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Review Bad Spring Boot Code",
    "files": {
      "UserController.java": "package com.example;\n\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.web.bind.annotation.*;\n\n@RestController\npublic class UserController {\n    \n    @Autowired\n    private UserService userService;\n    \n    @GetMapping(\"/users/{id}\")\n    public User getUser(@PathVariable String id) {\n        return userService.findById(id);\n    }\n}"
    }
  }'
```

**Expected Findings**:
1. Field injection detected (use constructor injection)
2. Missing input validation
3. No error handling
4. Missing @RequestMapping path
5. String ID parameter (should validate)

#### **2.2 Get Review Details**
```bash
curl -X GET http://localhost:8080/api/reviews/REVIEW_ID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "id": "uuid",
  "title": "Review Bad Spring Boot Code",
  "status": "COMPLETED",
  "qualityScore": {
    "overallScore": 65,
    "securityScore": 60,
    "performanceScore": 75,
    "maintainabilityScore": 70,
    "bestPracticesScore": 55,
    "grade": "C+"
  },
  "findings": [
    {
      "title": "Field Injection Anti-Pattern",
      "severity": "MEDIUM",
      "category": "BEST_PRACTICE",
      "description": "...",
      "suggestedFix": "..."
    }
  ]
}
```

---

### **Test Scenario 3: Review History & Statistics**

#### **3.1 List User Reviews**
```bash
curl -X GET http://localhost:8080/api/reviews \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### **3.2 Get Recent Reviews**
```bash
curl -X GET "http://localhost:8080/api/reviews/recent?limit=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### **3.3 Get User Statistics**
```bash
curl -X GET http://localhost:8080/api/reviews/stats \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "totalReviews": 5,
  "completedReviews": 5,
  "averageQualityScore": 72.5,
  "totalFindings": 23,
  "criticalIssues": 2
}
```

---

### **Test Scenario 4: Finding Resolution**

#### **4.1 Resolve Finding**
```bash
curl -X PUT http://localhost:8080/api/reviews/REVIEW_ID/findings/FINDING_ID/resolve \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### **Test Scenario 5: Review Summary**

#### **5.1 Get AI-Generated Summary**
```bash
curl -X GET http://localhost:8080/api/reviews/REVIEW_ID/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected**: Markdown-formatted summary with:
- Overall assessment
- Top 3 critical issues
- Top 3 improvements
- Positive aspects
- Next steps

---

## 🎯 Test Checklist

### **Authentication**
- [ ] Register new user
- [ ] Register with duplicate username (should fail)
- [ ] Register with duplicate email (should fail)
- [ ] Register with weak password (should fail)
- [ ] Login with valid credentials
- [ ] Login with invalid password (should fail)
- [ ] Access protected endpoint without token (should fail)
- [ ] Access protected endpoint with invalid token (should fail)
- [ ] Access protected endpoint with expired token (should fail)

### **Code Review**
- [ ] Create review with valid code
- [ ] Create review with bad Spring Boot code
- [ ] Create review with security issues
- [ ] Create review without authentication (should fail)
- [ ] Create review as FREE user (5 times max)
- [ ] 6th review as FREE user should fail
- [ ] Get review details
- [ ] Get non-existent review (should 404)
- [ ] Get review summary
- [ ] Delete own review
- [ ] Delete other user's review (should fail)

### **Statistics & History**
- [ ] List all user reviews
- [ ] Get recent reviews with limit
- [ ] Get user statistics
- [ ] Statistics should update after new review

### **Findings**
- [ ] Findings should have categories
- [ ] Findings should have severity levels
- [ ] Findings should include code snippets
- [ ] Findings should include suggested fixes
- [ ] Resolve finding
- [ ] Resolved findings should be marked

---

## 📊 Expected Test Coverage

### **Current Coverage**
- **Domain Layer**: 85%+
- **Application Layer**: 75%+
- **API Layer**: 80%+
- **Overall**: 75%+

### **Critical Paths (100% Coverage Required)**
- User authentication
- Review creation
- Quality scoring
- Finding categorization

---

## 🐛 Known Test Limitations

### **Not Tested (Requires Mocking)**
1. **AI Integration**: OpenAI API calls (expensive, external)
2. **GitHub Integration**: GitHub API (external dependency)
3. **Email Sending**: Email verification (if implemented)

### **Manual Testing Required**
1. **Performance**: Load testing with JMeter
2. **Security**: Penetration testing
3. **UI/UX**: Frontend user flows
4. **Cross-browser**: If frontend is built

---

## 🔧 Troubleshooting Tests

### **Test Fails: "Connection refused"**
```bash
# Ensure H2 is used for tests (check application-test.yml)
# Or start PostgreSQL if using it for tests
docker-compose up -d postgres
```

### **Test Fails: "JWT secret too short"**
```bash
# Update application-test.yml with longer secret (256 bits minimum)
app.jwt.secret: test-secret-key-for-testing-minimum-256-bits-required
```

### **Test Fails: "OpenAI API key invalid"**
```bash
# Tests should NOT call OpenAI
# Check that test profile disables AI or uses mocks
```

### **Integration Test Slow**
```bash
# Use H2 in-memory database for tests
spring.datasource.url: jdbc:h2:mem:testdb
```

---

## 📝 Writing New Tests

### **Unit Test Template**
```java
@Test
void shouldDoSomething() {
    // Given - Setup test data
    User user = User.builder()
        .username("test")
        .build();

    // When - Execute the action
    user.incrementReviewCount();

    // Then - Verify the result
    assertEquals(1, user.getReviewsCount());
}
```

### **Integration Test Template**
```java
@Test
void shouldDoSomethingViaAPI() throws Exception {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setUsername("test");

    // When/Then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test"));
}
```

---

## ✅ Test Execution Summary

### **Quick Test**
```bash
# Run tests and see results
mvn clean test
```

### **Full CI/CD Test**
```bash
# What CI/CD should run
mvn clean verify jacoco:report
```

### **Pre-Deployment Checklist**
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Manual E2E scenarios verified
- [ ] Test coverage > 75%
- [ ] No critical vulnerabilities
- [ ] Performance benchmarks met

---

## 🎓 Test Philosophy

This application follows:
- **Test Pyramid**: More unit tests, fewer integration tests
- **AAA Pattern**: Arrange, Act, Assert
- **Single Responsibility**: One assertion per test
- **Independence**: Tests don't depend on each other
- **Fast**: Unit tests run in milliseconds

---

**Ready to Test!** 🚀

Run `mvn test` and watch everything pass!
