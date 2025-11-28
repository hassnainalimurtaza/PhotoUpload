# 🚀 Quick Start Guide

## Prerequisites Check

Before running the project, ensure you have:

- ✅ Docker Desktop installed and running
- ✅ Docker Compose (comes with Docker Desktop)
- ✅ At least 8GB RAM available for Docker
- ✅ Ports 3000, 5432, 5672, 6379, 8080, 15672 available

## Option 1: Docker Compose (Recommended - Easiest)

### Step 1: Setup Environment

```bash
# Navigate to project directory
cd /hdd/IdeaProjects/CursorAiProject

# Create .env file from example
cp .env.example .env

# Edit .env if you want to use real AWS S3 (optional for testing)
# For local testing, you can leave it as is
nano .env
```

### Step 2: Start All Services

```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Or view specific service logs
docker-compose logs -f photo-api
```

### Step 3: Wait for Services to Start

The backend needs about 60-90 seconds to fully initialize. Monitor the logs:

```bash
# Check if API is healthy
docker-compose logs photo-api | grep "Started PhotoUploadApplication"

# Check all service status
docker-compose ps
```

### Step 4: Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/photos
- **Health Check**: http://localhost:8080/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **API Documentation**: http://localhost:8080/actuator

### Step 5: Test the System

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Test photo upload (requires authentication)
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@/path/to/your/photo.jpg" \
  -F "userId=test-user-123"

# Get photos
curl -X GET "http://localhost:8080/api/photos?userId=test-user-123" \
  -u user:password
```

## Option 2: Manual Setup (Development)

### Backend Setup

```bash
cd /hdd/IdeaProjects/CursorAiProject/backend

# Start required services
docker-compose up -d postgres redis rabbitmq

# Wait for services to be ready (about 10 seconds)
sleep 10

# Build the project
mvn clean install -DskipTests

# Run the application
cd photo-api
mvn spring-boot:run
```

### Frontend Setup (Separate Terminal)

```bash
cd /hdd/IdeaProjects/CursorAiProject/frontend/host

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will be available at http://localhost:3000

## 🔍 Verify Everything is Working

### 1. Check Docker Services

```bash
docker-compose ps
```

Expected output: All services should be "Up" and healthy.

### 2. Check API Health

```bash
curl http://localhost:8080/actuator/health | jq
```

Expected: `{"status":"UP"}`

### 3. Check Database

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres -d photoupload

# Check tables
\dt

# Exit
\q
```

### 4. Check RabbitMQ

Visit http://localhost:15672
- Username: guest
- Password: guest

You should see the photo event queues.

### 5. Test Photo Upload via UI

1. Open http://localhost:3000
2. Drag and drop an image file
3. Watch the upload progress
4. Navigate to Gallery to see the uploaded photo
5. Check Events to see the processing workflow

## 🐛 Troubleshooting

### Services Won't Start

```bash
# Stop all services
docker-compose down -v

# Remove old volumes
docker volume prune

# Restart
docker-compose up -d
```

### Port Already in Use

```bash
# Find what's using the port (example: 8080)
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change the port in docker-compose.yml
```

### Backend Fails to Connect to Database

```bash
# Check if PostgreSQL is running
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# Wait a bit and restart the API
docker-compose restart photo-api
```

### "Failed to upload" Error

This is expected if you haven't configured AWS S3 credentials. The system will:
1. Try to upload to S3
2. Fail (circuit breaker opens)
3. Fall back to local storage simulation
4. Queue the item for retry

For local testing without S3, this is normal behavior demonstrating the resilience patterns.

### Frontend Can't Connect to Backend

```bash
# Check if API is accessible
curl http://localhost:8080/actuator/health

# Check CORS settings in application.yml
# Ensure http://localhost:3000 is in allowed origins
```

## 📊 Monitoring

### View Metrics

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Application info
curl http://localhost:8080/actuator/info

# Bean information
curl http://localhost:8080/actuator/beans
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f photo-api

# Last 100 lines
docker-compose logs --tail=100 photo-api
```

## 🧪 Testing the Resilience Patterns

### Test Circuit Breaker

```bash
# Upload multiple photos rapidly to trigger circuit breaker
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/photos/upload \
    -u user:password \
    -F "file=@test.jpg" \
    -F "userId=test-$i"
done
```

### Test Message Queue Fallback

```bash
# Stop RabbitMQ
docker-compose stop rabbitmq

# Try uploading - should fall back to database queue
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@test.jpg" \
  -F "userId=test-user"

# Restart RabbitMQ
docker-compose start rabbitmq
```

### Test Retry Mechanism

```bash
# Upload a photo (will fail to S3 if not configured)
# Watch the retry attempts in logs
docker-compose logs -f photo-api | grep -i retry
```

## 🛑 Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Stop but keep data
docker-compose stop
```

## 📝 Default Credentials

- **API Basic Auth**: user/password or admin/admin
- **RabbitMQ**: guest/guest
- **PostgreSQL**: postgres/postgres
- **Redis**: (no password by default)

## 🎯 Next Steps

1. Configure AWS S3 credentials in `.env` for real cloud storage
2. Explore the API endpoints at http://localhost:8080/actuator
3. Check the event log for a photo to see the complete workflow
4. Try the retry feature on a failed photo
5. Monitor the RabbitMQ queues
6. Check Redis cache with: `docker-compose exec redis redis-cli`

## ✅ Success Indicators

You'll know everything is working when:

1. ✅ All docker containers are "Up (healthy)"
2. ✅ API returns 200 on `/actuator/health`
3. ✅ Frontend loads at http://localhost:3000
4. ✅ You can upload a photo via the UI
5. ✅ Photo status changes from PENDING → UPLOADING → PROCESSING → COMPLETED
6. ✅ Events log shows all processing steps
7. ✅ RabbitMQ shows message flow in queues

---

**Need Help?** Check the logs: `docker-compose logs -f`

