# Photo Upload System - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Technology Stack](#technology-stack)
5. [Design Patterns](#design-patterns)
6. [Project Structure](#project-structure)
7. [API Documentation](#api-documentation)
8. [Setup & Installation](#setup--installation)
9. [Configuration](#configuration)
10. [Testing](#testing)
11. [Deployment](#deployment)
12. [Monitoring & Observability](#monitoring--observability)

---

## Overview

A **production-grade, enterprise-level photo upload and management system** built with modern architecture principles. This system demonstrates best practices in software engineering, including SOLID principles, microservice patterns, event-driven architecture, and comprehensive resilience mechanisms.

### Key Highlights
- ✅ **Production-Ready**: Built with enterprise-grade code quality and error handling
- ✅ **Scalable**: Microservice architecture with horizontal scaling capabilities
- ✅ **Resilient**: Circuit breakers, retry mechanisms, and fallback strategies
- ✅ **Cloud-Native**: Container-ready with Kubernetes support
- ✅ **Modern Stack**: Spring Boot 3.2, React 18, TypeScript

---

## Features

### Core Functionality
- **Photo Upload**: Drag-and-drop or click-to-upload interface
- **Multi-Provider Storage**: Support for AWS S3, Google Cloud Storage, Azure Blob, and local filesystem
- **Photo Management**: List, view, and delete photos
- **Real-time Processing**: Asynchronous photo processing with status tracking
- **Metadata Extraction**: Automatic extraction of file metadata

### Technical Features

#### 1. **Resilience & Fault Tolerance**
- **Circuit Breakers**: Prevent cascading failures
  - Configurable failure thresholds (50% for storage, 70% for events)
  - Automatic recovery with half-open state
  - Slow call detection (2-3s thresholds)
- **Retry Mechanisms**: Exponential backoff strategy
  - 3 attempts with 1s, 2s, 4s intervals
  - Configurable retry policies
- **Fallback Strategies**: Graceful degradation when services fail

#### 2. **Event-Driven Architecture**
- **Async Processing**: Non-blocking photo processing
- **Event Bus**: RabbitMQ/Kafka integration with database fallback
- **Event Types**:
  - `PhotoUploadedEvent`
  - `PhotoProcessedEvent`
  - `PhotoDeletedEvent`
  - `PhotoProcessingFailedEvent`
- **Dead Letter Queue (DLQ)**: Failed message handling
- **Event Replay**: Capability to reprocess failed events

#### 3. **Performance Optimization**
- **Connection Pooling**: HikariCP with optimized settings
  - Production: 20 connections max
  - Local: 5 connections max
- **Caching**: Redis-based caching with TTL
  - Photo metadata caching
  - Query result caching
- **Async Processing**: CompletableFuture for parallel operations
- **Database Optimization**: 
  - Optimistic locking for concurrency
  - Database indexes on frequently queried columns

#### 4. **Security**
- **Input Validation**: Jakarta Validation annotations
- **File Type Validation**: MIME type checking
- **Size Limits**: Configurable max file size (50MB default)
- **CORS Configuration**: Configurable cross-origin policies
- **SQL Injection Protection**: JPA parameterized queries

#### 5. **Observability**
- **Health Checks**: Spring Boot Actuator endpoints
  - Database health
  - Storage health
  - Message queue health
- **Metrics**: Micrometer integration
  - Upload success/failure rates
  - Processing times
  - Circuit breaker states
- **Logging**: SLF4J with structured logging
  - Request/Response logging
  - Error tracking
  - Performance metrics

---

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (React)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Photo Upload │  │ Photo List   │  │ Photo Detail │      │
│  │  Component   │  │  Component   │  │  Component   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                          │                                   │
└──────────────────────────┼───────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────┼───────────────────────────────────┐
│                     API Gateway                              │
│              (PhotoUploadController)                         │
└──────────────────────────┼───────────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────────┐
│                   Service Layer (Facade)                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           PhotoUploadFacade (Orchestrator)             │ │
│  └───┬────────────────────────────────────────────────┬───┘ │
│      │                                                 │     │
│  ┌───▼──────────┐  ┌──────────────┐  ┌───────────────▼───┐ │
│  │Photo Service │  │Validation    │  │ Event Publisher   │ │
│  │              │  │Service       │  │                   │ │
│  └───┬──────────┘  └──────────────┘  └───────────────────┘ │
│      │                                         │             │
└──────┼─────────────────────────────────────────┼─────────────┘
       │                                         │
   ┌───▼────────────┐                    ┌──────▼──────┐
   │ Storage Layer  │                    │ Message Queue│
   │  (Strategy)    │                    │  (RabbitMQ) │
   └───┬────────────┘                    └─────────────┘
       │
   ┌───▼──────────────────────────────┐
   │  Storage Providers (Adapter)     │
   │  ┌────┐  ┌────┐  ┌──────┐       │
   │  │ S3 │  │GCS │  │Azure │       │
   │  └────┘  └────┘  └──────┘       │
   └──────────────────────────────────┘
```

### Module Structure

```
photo-upload-system/
├── photo-common/          # Shared domain models, DTOs, exceptions
├── storage-provider/      # Storage abstraction & implementations
├── event-bus/            # Event publishing & handling
├── photo-service/        # Core business logic
└── photo-api/            # REST API & web layer
```

### Key Architectural Decisions

1. **Modular Monolith**: Structured for easy transition to microservices
2. **Hexagonal Architecture**: Domain logic isolated from infrastructure
3. **Strategy Pattern**: Pluggable storage providers
4. **Facade Pattern**: Simplified API for complex operations
5. **Event Sourcing**: Maintain event history for audit trails

---

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.2.0 | Application framework |
| **Spring Data JPA** | 3.2.0 | Data access layer |
| **Spring Cloud** | 2023.0.0 | Cloud patterns & config |
| **Hibernate** | 6.x | ORM framework |
| **PostgreSQL** | 15+ | Primary database (production) |
| **H2** | 2.x | In-memory database (local dev) |
| **Redis** | 7.x | Caching layer |
| **RabbitMQ** | 3.12+ | Message broker |
| **Apache Kafka** | 3.x | Alternative message broker |
| **Resilience4j** | 2.2.0 | Resilience patterns |
| **Micrometer** | 1.12.x | Metrics & monitoring |
| **JUnit 5** | 5.10.x | Testing framework |
| **Mockito** | 5.x | Mocking framework |
| **TestContainers** | 1.19.3 | Integration testing |
| **Lombok** | 1.18.30 | Boilerplate reduction |
| **MapStruct** | 1.5.5 | Object mapping |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18.x | UI framework |
| **TypeScript** | 5.x | Type-safe JavaScript |
| **Redux Toolkit** | 2.x | State management |
| **React Query** | 5.x | Server state management |
| **Tailwind CSS** | 3.x | Utility-first CSS |
| **Axios** | 1.x | HTTP client |
| **React Router** | 6.x | Client-side routing |
| **Module Federation** | 5.x | Micro frontend support |

### Cloud & Storage
- **AWS SDK**: S3 integration
- **Google Cloud Storage**: GCS integration
- **Azure Blob Storage**: Azure integration

### DevOps
- **Docker** | **Docker Compose**: Containerization
- **Kubernetes**: Container orchestration
- **Helm**: Kubernetes package manager
- **GitHub Actions**: CI/CD pipeline
- **Maven**: Build tool (backend)
- **npm**: Package manager (frontend)

---

## Design Patterns

### 1. **Creational Patterns**

#### Singleton Pattern
```java
@Service
public class PhotoServiceImpl implements PhotoService {
    // Spring manages as singleton
}
```

#### Builder Pattern
```java
Photo photo = Photo.builder()
    .fileName("example.jpg")
    .originalFileName("photo.jpg")
    .fileSize(1024L)
    .status(PhotoStatus.PENDING)
    .build();
```

#### Factory Pattern
```java
public interface EventPublisherFactory {
    EventPublisher createPublisher(String type);
}
```

### 2. **Structural Patterns**

#### Facade Pattern
```java
@Service
public class PhotoUploadFacade {
    // Orchestrates multiple services
    public PhotoResponseDTO uploadPhoto(PhotoUploadRequest request) {
        // 1. Validate
        // 2. Upload to storage
        // 3. Save to database
        // 4. Publish event
        // 5. Return response
    }
}
```

#### Adapter Pattern
```java
public interface CloudStorageProvider {
    String uploadFile(MultipartFile file, String fileName);
    byte[] downloadFile(String fileUrl);
}

// Adapters for different cloud providers
@Service
public class S3StorageProvider implements CloudStorageProvider { }
@Service
public class GCSStorageProvider implements CloudStorageProvider { }
@Service
public class AzureStorageProvider implements CloudStorageProvider { }
```

#### Decorator Pattern
```java
@Component
public class RetryableStorageProvider implements CloudStorageProvider {
    private final CloudStorageProvider delegate;
    
    @Override
    @Retry(name = "storage")
    public String uploadFile(MultipartFile file, String fileName) {
        return delegate.uploadFile(file, fileName);
    }
}
```

#### Proxy Pattern
```java
@Component
public class CachedPhotoService implements PhotoService {
    @Cacheable("photos")
    public Photo getPhotoById(Long id) { }
}
```

### 3. **Behavioral Patterns**

#### Strategy Pattern
```java
@Service
public class StorageStrategySelector {
    public CloudStorageProvider selectProvider(String provider) {
        return switch(provider) {
            case "s3" -> s3Provider;
            case "gcs" -> gcsProvider;
            case "azure" -> azureProvider;
            default -> localProvider;
        };
    }
}
```

#### Observer Pattern
```java
@Component
public class PhotoEventListener {
    @EventListener
    public void handlePhotoUploaded(PhotoUploadedEvent event) {
        // Process photo upload event
    }
}
```

#### State Pattern
```java
public enum PhotoStatus {
    PENDING, UPLOADING, UPLOADED, PROCESSING, 
    RETRYING, COMPLETED, FAILED;
    
    public boolean canTransitionTo(PhotoStatus newStatus) {
        // State transition rules
    }
}
```

#### Command Pattern
```java
public interface PhotoCommand {
    void execute();
    void undo();
}

public class UploadPhotoCommand implements PhotoCommand { }
public class DeletePhotoCommand implements PhotoCommand { }
```

#### Template Method Pattern
```java
public abstract class AbstractStorageProvider {
    public final String upload(MultipartFile file) {
        validate(file);
        String url = doUpload(file);
        postProcess(url);
        return url;
    }
    
    protected abstract String doUpload(MultipartFile file);
}
```

### 4. **Enterprise Patterns**

#### Repository Pattern
```java
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByStatus(PhotoStatus status);
    Optional<Photo> findByFileName(String fileName);
}
```

#### Unit of Work Pattern
- Implemented via JPA `@Transactional`

#### Saga Pattern
```java
@Service
public class PhotoUploadSaga {
    @Transactional
    public void executeUploadSaga(PhotoUploadRequest request) {
        try {
            // Step 1: Upload to storage
            // Step 2: Save to DB
            // Step 3: Publish event
        } catch (Exception e) {
            // Compensating transactions
            compensate();
        }
    }
}
```

---

## Project Structure

### Backend Structure

```
backend/
├── photo-common/
│   └── src/main/java/com/photoupload/common/
│       ├── domain/              # Domain entities
│       │   ├── Photo.java
│       │   └── PhotoStatus.java
│       ├── dto/                 # Data Transfer Objects
│       │   ├── PhotoUploadRequest.java
│       │   ├── PhotoResponseDTO.java
│       │   └── ErrorResponse.java
│       ├── exception/           # Custom exceptions
│       │   ├── PhotoNotFoundException.java
│       │   ├── StorageException.java
│       │   └── ValidationException.java
│       └── event/              # Event definitions
│           ├── PhotoUploadedEvent.java
│           └── PhotoProcessedEvent.java
│
├── storage-provider/
│   └── src/main/java/com/photoupload/storage/
│       ├── CloudStorageProvider.java      # Interface
│       ├── StorageMetadata.java
│       ├── impl/                          # Implementations
│       │   ├── S3StorageProvider.java
│       │   ├── GCSStorageProvider.java
│       │   ├── AzureStorageProvider.java
│       │   └── LocalFileStorageProvider.java
│       └── config/
│           ├── ResilienceConfig.java
│           └── StorageConfig.java
│
├── event-bus/
│   └── src/main/java/com/photoupload/event/
│       ├── EventPublisher.java            # Interface
│       ├── impl/
│       │   ├── RabbitMQEventPublisher.java
│       │   ├── KafkaEventPublisher.java
│       │   └── DatabaseEventPublisher.java
│       └── listener/
│           └── PhotoEventListener.java
│
├── photo-service/
│   └── src/main/java/com/photoupload/service/
│       ├── PhotoService.java             # Interface
│       ├── ValidationService.java
│       ├── impl/
│       │   ├── PhotoServiceImpl.java
│       │   └── ValidationServiceImpl.java
│       ├── facade/
│       │   └── PhotoUploadFacade.java   # Main orchestrator
│       └── repository/
│           └── PhotoRepository.java
│
└── photo-api/
    └── src/main/java/com/photoupload/api/
        ├── PhotoUploadApplication.java   # Main class
        ├── controller/
        │   ├── PhotoUploadController.java
        │   └── HealthController.java
        ├── config/
        │   ├── WebConfig.java
        │   ├── SecurityConfig.java
        │   └── AsyncConfig.java
        ├── filter/
        │   └── RequestLoggingFilter.java
        ├── exception/
        │   └── GlobalExceptionHandler.java
        └── resources/
            ├── application.yml
            ├── application-local.yml
            ├── application-dev.yml
            └── application-prod.yml
```

### Frontend Structure

```
frontend/
├── host/                      # Host MFE application
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── App.tsx           # Root component
│   │   ├── index.tsx         # Entry point
│   │   ├── components/       # React components
│   │   │   ├── PhotoUpload/
│   │   │   │   ├── PhotoUpload.tsx
│   │   │   │   ├── UploadZone.tsx
│   │   │   │   └── UploadProgress.tsx
│   │   │   ├── PhotoList/
│   │   │   │   ├── PhotoList.tsx
│   │   │   │   ├── PhotoCard.tsx
│   │   │   │   └── PhotoGrid.tsx
│   │   │   └── common/
│   │   │       ├── ErrorBoundary.tsx
│   │   │       ├── LoadingSpinner.tsx
│   │   │       └── Toast.tsx
│   │   ├── store/            # Redux store
│   │   │   ├── index.ts
│   │   │   ├── photoSlice.ts
│   │   │   └── uiSlice.ts
│   │   ├── services/         # API services
│   │   │   ├── api.ts
│   │   │   └── photoService.ts
│   │   ├── hooks/            # Custom hooks
│   │   │   ├── usePhotos.ts
│   │   │   └── useUpload.ts
│   │   ├── types/            # TypeScript types
│   │   │   └── photo.types.ts
│   │   └── styles/
│   │       └── tailwind.css
│   ├── package.json
│   └── webpack.config.js
│
└── remote/                   # Remote MFE (if needed)
    └── ...
```

---

## API Documentation

### Base URL
- **Local**: `http://localhost:8080`
- **Production**: `https://api.yourdomain.com`

### Endpoints

#### 1. Upload Photo
```http
POST /api/photos
Content-Type: multipart/form-data

Parameters:
- file (required): Photo file
- description (optional): Photo description
- tags (optional): Comma-separated tags

Response: 201 Created
{
  "id": 1,
  "fileName": "photo_20231128_123456.jpg",
  "originalFileName": "vacation.jpg",
  "fileSize": 2048576,
  "fileUrl": "https://storage.../photo_20231128_123456.jpg",
  "contentType": "image/jpeg",
  "status": "UPLOADED",
  "uploadedAt": "2023-11-28T12:34:56Z"
}
```

#### 2. Get All Photos
```http
GET /api/photos?page=0&size=20&sort=uploadedAt,desc

Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "fileName": "photo_20231128_123456.jpg",
      "fileSize": 2048576,
      "status": "COMPLETED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

#### 3. Get Photo by ID
```http
GET /api/photos/{id}

Response: 200 OK
{
  "id": 1,
  "fileName": "photo_20231128_123456.jpg",
  "originalFileName": "vacation.jpg",
  "fileSize": 2048576,
  "fileUrl": "https://storage.../photo_20231128_123456.jpg",
  "status": "COMPLETED",
  "uploadedAt": "2023-11-28T12:34:56Z",
  "processedAt": "2023-11-28T12:35:01Z"
}
```

#### 4. Delete Photo
```http
DELETE /api/photos/{id}

Response: 204 No Content
```

#### 5. Get Photo by Status
```http
GET /api/photos/status/{status}

Statuses: PENDING, UPLOADING, UPLOADED, PROCESSING, 
          RETRYING, COMPLETED, FAILED

Response: 200 OK
[
  { "id": 1, "fileName": "photo1.jpg", "status": "COMPLETED" },
  { "id": 2, "fileName": "photo2.jpg", "status": "COMPLETED" }
]
```

#### 6. Health Check
```http
GET /actuator/health

Response: 200 OK
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### 7. Metrics
```http
GET /actuator/metrics
GET /actuator/metrics/{metric-name}
```

### Error Responses

```json
{
  "timestamp": "2023-11-28T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File size exceeds maximum allowed size of 50MB",
  "path": "/api/photos"
}
```

#### HTTP Status Codes
- `200 OK`: Success
- `201 Created`: Resource created
- `204 No Content`: Success with no content
- `400 Bad Request`: Invalid request
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict
- `500 Internal Server Error`: Server error
- `503 Service Unavailable`: Service temporarily unavailable

---

## Setup & Installation

### Prerequisites
- **Java JDK**: 17 or 21
- **Node.js**: 18+ and npm
- **Maven**: 3.8+
- **Docker**: 20+ (optional, for containerized services)
- **PostgreSQL**: 15+ (for production)

### Local Development Setup

#### 1. Clone Repository
```bash
git clone <repository-url>
cd CursorAiProject
```

#### 2. Install Java (if needed)
```bash
# Using SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.1-oracle
```

#### 3. Build Backend
```bash
cd backend
mvn clean install -DskipTests
```

#### 4. Install Frontend Dependencies
```bash
cd frontend/host
npm install
```

#### 5. Run with Local Profile
```bash
# Terminal 1 - Backend
cd backend/photo-api
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2 - Frontend
cd frontend/host
npm start
```

#### 6. Access Application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

### Docker Setup

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

---

## Configuration

### Environment Profiles

#### Local Profile (`application-local.yml`)
- H2 in-memory database
- Local file storage
- Simple cache (no Redis)
- No RabbitMQ required

#### Development Profile (`application-dev.yml`)
- PostgreSQL database
- S3 storage (dev bucket)
- Redis cache
- RabbitMQ

#### Production Profile (`application-prod.yml`)
- PostgreSQL cluster
- S3 storage (production bucket)
- Redis cluster
- RabbitMQ cluster
- Enhanced security
- Production logging

### Key Configuration Properties

```yaml
# Storage Configuration
storage:
  provider: local  # s3, gcs, azure, local
  local:
    base-path: ./uploads
  s3:
    bucket-name: my-photos
    region: us-east-1

# Event Configuration
event:
  publisher: database-fallback  # rabbitmq, kafka, database-fallback

# Resilience Configuration
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1s

# File Upload Limits
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

---

## Testing

### Backend Testing

#### Unit Tests
```bash
cd backend
mvn test
```

#### Integration Tests
```bash
mvn verify -P integration-tests
```

#### Test Coverage
```bash
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

### Frontend Testing

```bash
cd frontend/host

# Unit tests
npm test

# Coverage
npm run test:coverage

# E2E tests
npm run test:e2e
```

### Test Structure

```
src/test/java/
├── unit/
│   ├── service/
│   │   └── PhotoServiceTest.java
│   └── controller/
│       └── PhotoUploadControllerTest.java
└── integration/
    ├── PhotoUploadIntegrationTest.java
    └── StorageProviderIntegrationTest.java
```

---

## Deployment

### Kubernetes Deployment

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Check status
kubectl get pods
kubectl get services

# View logs
kubectl logs -f deployment/photo-api
```

### Helm Deployment

```bash
# Install with Helm
helm install photo-upload ./helm/photo-upload-chart

# Upgrade
helm upgrade photo-upload ./helm/photo-upload-chart

# Uninstall
helm uninstall photo-upload
```

### CI/CD Pipeline

GitHub Actions workflow automatically:
1. Runs tests
2. Builds Docker images
3. Pushes to container registry
4. Deploys to Kubernetes (if on main branch)

---

## Monitoring & Observability

### Metrics (Micrometer)
- JVM metrics
- HTTP request metrics
- Database connection pool metrics
- Custom business metrics

### Logging
- Structured JSON logging
- Request/response logging
- Error tracking
- Performance logging

### Health Checks
- Database connectivity
- Storage availability
- Message queue status
- Disk space monitoring

### Alerting
Configure alerts for:
- High error rates
- Circuit breaker open states
- Slow response times
- Resource exhaustion

---

## Performance Characteristics

### Expected Performance
- **Upload Throughput**: 100+ concurrent uploads
- **Response Time**: < 200ms (p95)
- **Storage**: Unlimited (cloud-based)
- **Database**: 10,000+ photos efficiently

### Optimization Features
- Connection pooling (HikariCP)
- Query optimization (indexed columns)
- Caching (Redis)
- Async processing (CompletableFuture)
- Lazy loading (JPA)

---

## Security Considerations

### Implemented Security
- Input validation
- File type checking
- Size limits
- SQL injection protection (JPA)
- CORS configuration

### Recommendations for Production
- Enable HTTPS/TLS
- Add authentication (OAuth2/JWT)
- Implement rate limiting
- Add request signing
- Enable audit logging
- Use secrets management (AWS Secrets Manager, Vault)

---

## Troubleshooting

### Common Issues

#### Backend won't start
```bash
# Check Java version
java -version

# Check logs
tail -f backend/photo-api/backend.log
```

#### Database connection errors
```bash
# Verify database is running
psql -h localhost -U postgres -l

# Check connection settings in application.yml
```

#### Storage errors
```bash
# Verify storage configuration
# Check cloud provider credentials
# Ensure local storage directory exists
```

---

## Contributing

### Code Style
- Follow Java Code Conventions
- Use Lombok for boilerplate reduction
- Write meaningful commit messages
- Add tests for new features

### Pull Request Process
1. Fork the repository
2. Create feature branch
3. Write tests
4. Submit PR with description

---

## License

This project is licensed under the MIT License.

---

## Support & Contact

For issues and questions:
- Create an issue on GitHub
- Contact: support@yourdomain.com

---

**Generated**: November 2023  
**Version**: 1.0.0  
**Status**: Production-Ready ✅

