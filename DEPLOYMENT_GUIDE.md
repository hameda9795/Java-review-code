# DevMentor AI - Deployment Guide

Complete guide for deploying DevMentor AI to production.

---

## 🎯 Deployment Options

Choose the deployment strategy that best fits your needs:

1. **Docker Compose** (Simplest - Single Server)
2. **AWS ECS/Fargate** (Managed Containers)
3. **Kubernetes** (Enterprise Grade)
4. **Heroku** (Quick Deploy)

---

## 🐳 Option 1: Docker Compose Deployment (Recommended for Start)

### Prerequisites
- Server with Docker and Docker Compose installed
- Domain name (optional but recommended)
- SSL certificate (Let's Encrypt recommended)

### Step 1: Prepare Environment

```bash
# Create deployment directory
mkdir -p /opt/devmentor-ai
cd /opt/devmentor-ai

# Clone repository
git clone <your-repo-url> .

# Create environment file
cp .env.example .env
```

### Step 2: Configure Environment Variables

Edit `.env`:

```env
# Database
POSTGRES_USER=devmentor
POSTGRES_PASSWORD=<strong-password>
POSTGRES_DB=devmentor

# Application
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/devmentor
SPRING_DATASOURCE_USERNAME=devmentor
SPRING_DATASOURCE_PASSWORD=<strong-password>

# AI
OPENAI_API_KEY=<your-openai-api-key>

# Security
JWT_SECRET=<generate-256-bit-secret>

# GitHub OAuth
GITHUB_CLIENT_ID=<your-github-client-id>
GITHUB_CLIENT_SECRET=<your-github-client-secret>

# Server
SERVER_PORT=8080
ALLOWED_ORIGINS=https://yourdomain.com
```

### Step 3: Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: devmentor-postgres
    restart: always
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    networks:
      - devmentor-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: devmentor-backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - JWT_SECRET=${JWT_SECRET}
      - GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}
      - GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - devmentor-network
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  nginx:
    image: nginx:alpine
    container_name: devmentor-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - backend
    networks:
      - devmentor-network

volumes:
  postgres-data:

networks:
  devmentor-network:
    driver: bridge
```

### Step 4: Nginx Configuration

Create `nginx/nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server backend:8080;
    }

    server {
        listen 80;
        server_name yourdomain.com;

        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name yourdomain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

        # API proxy
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Frontend (if serving from same domain)
        location / {
            root /usr/share/nginx/html;
            try_files $uri $uri/ /index.html;
        }
    }
}
```

### Step 5: Deploy

```bash
# Build and start services
docker-compose -f docker-compose.prod.yml up -d --build

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Check status
docker-compose -f docker-compose.prod.yml ps
```

### Step 6: SSL Certificate (Let's Encrypt)

```bash
# Install certbot
sudo apt-get install certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renewal (already set up by certbot)
sudo certbot renew --dry-run
```

---

## ☁️ Option 2: AWS ECS Deployment

### Prerequisites
- AWS Account
- AWS CLI configured
- ECR repository created

### Step 1: Build and Push Docker Image

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build image
docker build -t devmentor-ai .

# Tag image
docker tag devmentor-ai:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/devmentor-ai:latest

# Push image
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/devmentor-ai:latest
```

### Step 2: Create RDS PostgreSQL Instance

1. Go to AWS RDS Console
2. Create PostgreSQL 16 database
3. Enable pgvector extension (via Parameter Groups)
4. Note down connection details

### Step 3: Create ECS Task Definition

Create `task-definition.json`:

```json
{
  "family": "devmentor-ai",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "devmentor-backend",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/devmentor-ai:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "valueFrom": "arn:aws:secretsmanager:region:account-id:secret:db-url"
        },
        {
          "name": "OPENAI_API_KEY",
          "valueFrom": "arn:aws:secretsmanager:region:account-id:secret:openai-key"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account-id:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/devmentor-ai",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### Step 4: Create ECS Service

```bash
# Register task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service \
  --cluster devmentor-cluster \
  --service-name devmentor-service \
  --task-definition devmentor-ai \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:...,containerName=devmentor-backend,containerPort=8080"
```

---

## ⚓ Option 3: Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (GKE, EKS, or AKS)
- kubectl configured
- Helm installed (optional)

### Step 1: Create Namespace

```bash
kubectl create namespace devmentor
```

### Step 2: Create Secrets

```bash
# Database credentials
kubectl create secret generic db-credentials \
  --from-literal=username=devmentor \
  --from-literal=password=<password> \
  -n devmentor

# API keys
kubectl create secret generic api-keys \
  --from-literal=openai-key=<key> \
  --from-literal=jwt-secret=<secret> \
  --from-literal=github-client-id=<id> \
  --from-literal=github-client-secret=<secret> \
  -n devmentor
```

### Step 3: Deploy PostgreSQL

Create `k8s/postgres.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: devmentor
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: devmentor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: pgvector/pgvector:pg16
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: POSTGRES_DB
          value: devmentor
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: devmentor
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
```

### Step 4: Deploy Application

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: devmentor-backend
  namespace: devmentor
spec:
  replicas: 3
  selector:
    matchLabels:
      app: devmentor-backend
  template:
    metadata:
      labels:
        app: devmentor-backend
    spec:
      containers:
      - name: backend
        image: <your-registry>/devmentor-ai:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres:5432/devmentor"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: openai-key
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: jwt-secret
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: devmentor-backend
  namespace: devmentor
spec:
  selector:
    app: devmentor-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Step 5: Deploy

```bash
# Apply configurations
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/deployment.yaml

# Check status
kubectl get pods -n devmentor
kubectl get svc -n devmentor

# View logs
kubectl logs -f deployment/devmentor-backend -n devmentor
```

---

## 🚀 Option 4: Heroku Deployment

### Step 1: Install Heroku CLI

```bash
# Install Heroku CLI
curl https://cli-assets.heroku.com/install.sh | sh

# Login
heroku login
```

### Step 2: Create Heroku App

```bash
# Create app
heroku create devmentor-ai

# Add PostgreSQL addon
heroku addons:create heroku-postgresql:standard-0

# Set buildpack
heroku buildpacks:set heroku/java
```

### Step 3: Configure Environment

```bash
# Set environment variables
heroku config:set OPENAI_API_KEY=<your-key>
heroku config:set JWT_SECRET=<your-secret>
heroku config:set GITHUB_CLIENT_ID=<your-id>
heroku config:set GITHUB_CLIENT_SECRET=<your-secret>
heroku config:set SPRING_PROFILES_ACTIVE=prod
```

### Step 4: Create `Procfile`

```
web: java -Dserver.port=$PORT -jar target/*.jar
```

### Step 5: Deploy

```bash
# Deploy
git push heroku main

# View logs
heroku logs --tail

# Scale
heroku ps:scale web=2
```

---

## 📊 Post-Deployment Checklist

### Security
- [ ] SSL/TLS certificate installed and valid
- [ ] Environment variables properly secured
- [ ] Database credentials rotated
- [ ] Firewall rules configured
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Security headers set in nginx

### Monitoring
- [ ] Application logs centralized
- [ ] Metrics dashboard set up (Grafana/Prometheus)
- [ ] Alerts configured
- [ ] Health checks passing
- [ ] Backup strategy in place

### Performance
- [ ] Database indexes created
- [ ] Connection pooling configured
- [ ] CDN configured for static assets
- [ ] Caching strategy implemented
- [ ] Load testing performed

### Documentation
- [ ] API documentation accessible
- [ ] Runbooks created
- [ ] Incident response plan documented
- [ ] Deployment rollback procedure tested

---

## 🔧 Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Database connection - verify SPRING_DATASOURCE_URL
# 2. Missing environment variables - check .env file
# 3. Port already in use - change SERVER_PORT
```

### Database Connection Issues

```bash
# Test database connection
docker exec -it devmentor-postgres psql -U devmentor -d devmentor

# Check if pgvector extension is enabled
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### High Memory Usage

```bash
# Adjust JVM heap size in Dockerfile
ENV JAVA_OPTS="-Xmx1g -Xms512m"
```

---

## 📈 Scaling Recommendations

### Small Scale (< 100 users)
- Single server with Docker Compose
- 2GB RAM, 2 CPU cores
- Small RDS/Managed PostgreSQL instance

### Medium Scale (100-1000 users)
- 2-3 application instances
- Load balancer
- Medium RDS instance with read replicas
- Redis for caching

### Large Scale (1000+ users)
- Kubernetes cluster with auto-scaling
- Multi-region deployment
- CDN for static assets
- Managed PostgreSQL cluster
- ElastiCache/Redis cluster
- Message queue (RabbitMQ/Kafka) for async tasks

---

**Deployment Complete!** Your DevMentor AI application is now running in production.

For support, create an issue on GitHub.
