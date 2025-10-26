# DevMentor AI - Quick Start Guide

## 🚀 Get Up and Running in 5 Minutes

### Prerequisites

- Java 21 or higher
- Docker & Docker Compose (for database)
- OpenAI API key

**Note**: Maven is NOT required! The project includes Maven Wrapper (`mvnw`).

### Step 1: Clone and Navigate

```bash
cd devmentor-ai
```

### Step 2: Start PostgreSQL with pgvector

```bash
docker-compose up -d postgres
```

This starts PostgreSQL 16 with the pgvector extension enabled.

### Step 3: Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
export JWT_SECRET=your-secret-key-at-least-256-bits
export GITHUB_CLIENT_ID=your-github-oauth-client-id
export GITHUB_CLIENT_SECRET=your-github-oauth-client-secret
```

Or update `src/main/resources/application.yml` directly.

### Step 4: Build the Application

**Windows PowerShell:**
```powershell
.\mvnw.cmd clean install
```

**Windows CMD:**
```bash
mvnw.cmd clean install
```

**Linux/Mac:**
```bash
./mvnw clean install
```

### Step 5: Run the Application

**Windows PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Windows CMD:**
```bash
mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Step 6: Access the API

#### Swagger UI (API Documentation)
```
http://localhost:8080/swagger-ui.html
```

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### Metrics
```
http://localhost:8080/actuator/prometheus
```

---

## 📝 Quick API Examples

### Create a Code Review

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <your-user-uuid>" \
  -d '{
    "title": "Review My Service Class",
    "files": {
      "UserService.java": "public class UserService { ... }"
    }
  }'
```

### Get Review Results

```bash
curl http://localhost:8080/api/reviews/<review-id>
```

### Get User Statistics

```bash
curl http://localhost:8080/api/reviews/stats \
  -H "X-User-Id: <your-user-uuid>"
```

---

## 🧪 Test the AI Review

### Sample Java File to Review

Create a file `BadCode.java`:

```java
public class UserController {
    @Autowired
    private UserService userService; // Field injection - bad practice

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        // No input validation
        // SQL injection risk if used in raw query
        return userService.findById(id);
    }
}
```

### Submit for Review

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123e4567-e89b-12d3-a456-426614174000" \
  -d @review-request.json
```

Where `review-request.json`:
```json
{
  "title": "Review Bad Code Example",
  "files": {
    "UserController.java": "<paste code here>"
  }
}
```

### Expected Findings

The AI will identify:
1. **Field injection** (use constructor injection)
2. **Missing input validation**
3. **Potential SQL injection** risk
4. **Missing @RestController** annotation
5. **No error handling**
6. **Missing API documentation**

---

## 🐳 Docker Deployment (Full Stack)

### Build and Run Everything

```bash
# Build application
mvn clean package

# Start all services
docker-compose up -d
```

This starts:
- PostgreSQL with pgvector
- Spring Boot application
- (Optional) Prometheus & Grafana

---

## 📊 Monitoring

### Actuator Endpoints

```bash
# Health
curl http://localhost:8080/actuator/health

# Info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Grafana Dashboard (if enabled)

```
http://localhost:3000
Username: admin
Password: admin
```

---

## 🔧 Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs devmentor-postgres

# Connect to database
docker exec -it devmentor-postgres psql -U devmentor -d devmentor

# Verify pgvector extension
\dx
```

### Application Won't Start

```bash
# Check Java version
java -version  # Should be 21+

# Check Maven version
mvn -version

# Clean and rebuild
mvn clean install -U

# Check logs
tail -f logs/devmentor-ai.log
```

### OpenAI API Errors

- Verify API key is correct
- Check your OpenAI account has credits
- Review rate limits
- Check logs for detailed error messages

---

## 📚 Next Steps

1. **Add Authentication**: Implement JWT authentication (currently using X-User-Id header)
2. **Add GitHub Integration**: Enable GitHub OAuth and repository analysis
3. **Build Frontend**: Create Next.js dashboard
4. **Add Tests**: Write comprehensive unit and integration tests
5. **Configure RAG**: Seed the vector database with Spring Boot best practices

---

## 🆘 Getting Help

- **Documentation**: See `/docs` folder
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Logs**: `logs/devmentor-ai.log`
- **Issues**: File an issue on GitHub

---

## 🎯 What's Working Now

✅ **Core Functionality**:
- File upload and AI code review
- Quality scoring (0-100)
- Finding categorization (Security, Performance, Code Smell, etc.)
- Severity levels (CRITICAL to INFO)
- REST API with full CRUD operations
- PostgreSQL persistence
- Swagger API documentation
- Docker deployment

⏳ **In Development**:
- JWT Authentication
- GitHub OAuth integration
- RAG system with vector embeddings
- Frontend dashboard
- Comprehensive test suite

---

## 💡 Pro Tips

1. **Use Meaningful Titles**: Help organize your reviews
2. **Review Small Changes**: Better AI analysis on focused code
3. **Check Findings**: AI is good but not perfect - review suggestions carefully
4. **Monitor Costs**: Each review uses OpenAI API tokens
5. **Save API Keys**: Use environment variables, never commit secrets

---

**Happy Coding! 🚀**

For detailed documentation, see [README.md](README.md) and [STATUS.md](STATUS.md)
