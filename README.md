# Photo Upload Management System

A production-grade, distributed photo upload and management system built with Spring Boot and React Micro Frontends, implementing comprehensive SOLID principles, design patterns, and resilience mechanisms.

## 🎯 Features

- **Photo Upload & Processing**: Drag-and-drop upload with real-time progress tracking
- **Automatic Processing**: Thumbnail generation and metadata extraction
- **Event-Driven Architecture**: RabbitMQ/Kafka for async processing
- **Resilience Patterns**: Circuit breakers, retry mechanisms, fallback strategies
- **Cloud Storage**: Abstracted support for AWS S3, Google Cloud Storage, Azure Blob
- **Micro Frontend Architecture**: Module Federation for scalable frontend
- **Production-Ready**: Docker, Kubernetes, CI/CD, monitoring, and observability

## 🏗️ Architecture

### Backend Architecture

```
┌──────────────┐
│  photo-api   │ ← REST Controllers, Security, Exception Handling
├──────────────┤
│photo-service │ ← Business Logic, Saga Pattern, Async Processing
├──────────────┤
│  event-bus   │ ← RabbitMQ/Kafka Event Publishing & Consumption
├──────────────┤
│storage-provider│← S3/GCS/Azure Abstraction with Circuit Breakers
├──────────────┤
│photo-common  │ ← Shared Entities, DTOs, Events, Exceptions
└──────────────┘
```

### Frontend Architecture (Micro Frontends)

```
┌─────────────────────────────────────┐
│         Host Application            │
│   (Module Federation Orchestrator)  │
├─────────────────────────────────────┤
│  ┌─────────┐  ┌──────────┐  ┌──────┐
│  │Upload   │  │Gallery   │  │Events│
│  │  MFE    │  │  MFE     │  │  MFE │
│  └─────────┘  └──────────┘  └──────┘
└─────────────────────────────────────┘
```

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2, Spring Cloud
- **Database**: PostgreSQL with Flyway migrations
- **Caching**: Redis (24h photo URLs, 1h permissions, 12h thumbnails)
- **Message Queue**: RabbitMQ / Apache Kafka
- **Cloud Storage**: AWS S3 / GCS / Azure Blob (abstracted)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Bulkhead)
- **Observability**: Spring Actuator, Micrometer, Prometheus

### Frontend
- **Framework**: React 18 with TypeScript
- **State Management**: Redux Toolkit
- **Server State**: React Query
- **Micro Frontends**: Webpack Module Federation
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios with interceptors

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes with HPA
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus + Grafana

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+ (or Kafka 3.x)

## 🚀 Quick Start

### Using Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd CursorAiProject

# Set environment variables
cp .env.example .env
# Edit .env with your AWS/GCP/Azure credentials

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f photo-api

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# RabbitMQ Management: http://localhost:15672
```

### Manual Setup

#### Backend

```bash
cd backend

# Build all modules
mvn clean install

# Run database migrations
mvn flyway:migrate -pl photo-api

# Start the application
cd photo-api
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend/host

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

## 🔧 Configuration

### Backend Configuration

Edit `backend/photo-api/src/main/resources/application.yml`:

```yaml
# Database
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/photoupload
    username: postgres
    password: postgres

# Redis
  data:
    redis:
      host: localhost
      port: 6379

# Storage Provider (s3, gcs, azure)
storage:
  provider: s3

# AWS S3
aws:
  access-key-id: ${AWS_ACCESS_KEY_ID}
  secret-access-key: ${AWS_SECRET_ACCESS_KEY}
  s3:
    bucket-name: photo-upload-bucket

# Event Publisher (rabbitmq, kafka, database-fallback)
event:
  publisher: rabbitmq
```

### Frontend Configuration

Create `frontend/host/.env`:

```env
REACT_APP_API_URL=http://localhost:8080/api
```

## 🧪 Testing

### Backend Tests

```bash
cd backend

# Unit tests
mvn test

# Integration tests
mvn verify -P integration-tests

# Coverage report (target: 80%)
mvn jacoco:report
```

### Frontend Tests

```bash
cd frontend/host

# Unit tests
npm test

# Coverage report
npm test -- --coverage

# Linting
npm run lint

# Type checking
npm run type-check
```

## 📊 Design Patterns Implemented

### SOLID Principles

- **Single Responsibility**: Each service has one well-defined purpose
- **Open/Closed**: Storage providers extensible without modifying existing code
- **Liskov Substitution**: All storage providers interchangeable
- **Interface Segregation**: Small, focused interfaces (PhotoProcessor, EventPublisher)
- **Dependency Inversion**: Depend on abstractions, not implementations

### Design Patterns

**Creational**
- Builder: Photo entity construction
- Factory: CloudStorageProviderFactory
- Singleton: Configuration beans

**Structural**
- Facade: PhotoService unified API
- Adapter: Cloud provider abstraction
- Decorator: Resilient wrappers for services

**Behavioral**
- Observer: Event listeners
- State: Photo status state machine
- Command: Processing queue commands
- Saga: Distributed photo processing workflow

## 🛡️ Resilience & Error Handling

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      storage-s3:
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        wait-duration-in-open-state: 30s
```

### Fallback Scenarios

| Scenario | Response | Fallback Strategy |
|----------|----------|------------------|
| Cloud Storage Down | 503 | Store locally, retry every 5min |
| Message Queue Down | 202 | Write to DB queue, poll every 30s |
| Database Unavailable | 500 | Graceful degradation, show cached data |
| Metadata Service Fails | 202 | Process without metadata, retry later |
| Duplicate Upload | 409 | Link to existing or offer choice |

## 📈 Monitoring & Observability

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Metrics Tracked

- Upload success/failure rates
- Processing latency (P50, P95, P99)
- Circuit breaker states
- Cache hit/miss ratios
- Queue depth and message processing rates

## 🚢 Deployment

### Kubernetes Deployment

```bash
# Apply configurations
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/deployment.yml

# Check deployment status
kubectl rollout status deployment/photo-api

# View pods
kubectl get pods -l app=photo-api

# Scale deployment
kubectl scale deployment photo-api --replicas=5
```

### Helm Chart (Optional)

```bash
helm install photo-upload ./helm/photo-upload \
  --set image.tag=latest \
  --set aws.accessKey=$AWS_ACCESS_KEY_ID \
  --set aws.secretKey=$AWS_SECRET_ACCESS_KEY
```

## 📝 API Documentation

### Photo Upload

```bash
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@photo.jpg" \
  -F "userId=user-123"
```

### Get Photos

```bash
curl -X GET "http://localhost:8080/api/photos?userId=user-123&status=COMPLETED&page=0&size=20" \
  -u user:password
```

### Get Photo Events

```bash
curl -X GET http://localhost:8080/api/photos/1/events \
  -u user:password
```

### Retry Failed Processing

```bash
curl -X POST http://localhost:8080/api/photos/1/retry \
  -u user:password
```

## 🔐 Security

- Spring Security with HTTP Basic Auth (demo) / JWT (production)
- CORS configuration for frontend origins
- Role-based access control (@PreAuthorize)
- Input validation with @Valid
- SQL injection prevention via JPA
- XSS protection via Content Security Policy

## 🏆 Performance

- **Concurrency**: ThreadPool (core: 10, max: 50)
- **Database**: Connection pooling (min: 5, max: 20)
- **Caching**: Redis multi-layer caching
- **Load Balancing**: Round-robin across 3+ instances
- **Horizontal Scaling**: Kubernetes HPA (3-10 pods)

## 📚 Additional Resources

- [Architecture Decision Records](docs/adr/)
- [API Documentation](docs/api/)
- [Runbook](docs/runbook.md)
- [Contributing Guidelines](CONTRIBUTING.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Your Name** - *Initial work*

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- React team for powerful UI library
- Module Federation for micro frontend capabilities
- Resilience4j for resilience patterns

---

**Built with ❤️ using Spring Boot, React, and Modern Cloud Technologies**

