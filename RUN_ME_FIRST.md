# 🚀 How to Run the Photo Upload System

## Current Status: ⚠️ Docker Not Running

Docker is required to run this application easily. Please follow these steps:

## Step 1: Start Docker Desktop

1. **Open Docker Desktop application** on your system
2. Wait for it to fully start (you'll see the whale icon in your system tray)
3. Verify Docker is running:
   ```bash
   docker --version
   docker info
   ```

## Step 2: Run the Application

Once Docker is running, simply execute:

```bash
cd /hdd/IdeaProjects/CursorAiProject
./start.sh
```

That's it! The script will:
- ✅ Check if Docker is running
- ✅ Create necessary configuration files
- ✅ Start PostgreSQL, Redis, RabbitMQ
- ✅ Build and start the backend API
- ✅ Start the frontend application

## Step 3: Access the Application

After 60-90 seconds, you can access:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | - |
| **Backend API** | http://localhost:8080 | user/password |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **RabbitMQ Console** | http://localhost:15672 | guest/guest |
| **API Metrics** | http://localhost:8080/actuator/prometheus | - |

## Step 4: Test the System

### Using the Web UI:
1. Open http://localhost:3000
2. Drag and drop a photo
3. Watch the upload progress
4. Navigate to Gallery to see your photos
5. Check Events to see the processing workflow

### Using cURL:
```bash
# Upload a photo
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@/path/to/your/photo.jpg" \
  -F "userId=test-user-123"

# List photos
curl -X GET "http://localhost:8080/api/photos?userId=test-user-123" \
  -u user:password | jq

# Check health
curl http://localhost:8080/actuator/health | jq
```

## Monitoring & Logs

```bash
# View all logs
docker compose logs -f

# View backend logs only
docker compose logs -f photo-api

# View service status
docker compose ps

# Check if everything is healthy
docker compose ps | grep healthy
```

## Stopping the Application

```bash
# Stop all services
./stop.sh

# Or manually
docker compose down

# Stop and remove all data (fresh start)
docker compose down -v
```

## Troubleshooting

### Problem: Docker command not found
**Solution:** Install Docker Desktop from https://www.docker.com/products/docker-desktop

### Problem: Port already in use
**Solution:** 
```bash
# Find what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Problem: Services won't start
**Solution:**
```bash
# Clean restart
docker compose down -v
docker compose up -d
```

### Problem: Backend fails to connect
**Solution:**
```bash
# Check logs
docker compose logs postgres
docker compose logs redis
docker compose logs rabbitmq

# Restart the backend
docker compose restart photo-api
```

## What's Running?

When you start the system, these containers will be created:

1. **postgres** - PostgreSQL database on port 5432
2. **redis** - Redis cache on port 6379
3. **rabbitmq** - Message queue on ports 5672 & 15672
4. **photo-api** - Spring Boot backend on port 8080
5. **frontend-host** - React frontend on port 3000

## Architecture Overview

```
┌─────────────────┐
│   Frontend      │ ← http://localhost:3000
│   (React)       │
└────────┬────────┘
         │ REST API
┌────────▼────────┐
│   Backend API   │ ← http://localhost:8080
│  (Spring Boot)  │
└────────┬────────┘
         │
    ┌────┴─────┬──────────┬────────┐
    │          │          │        │
┌───▼───┐  ┌──▼───┐  ┌──▼───┐ ┌──▼────┐
│ DB    │  │Redis │  │RabbitMQ  │ S3   │
│(PG)   │  │Cache │  │Queue │ │(Cloud)│
└───────┘  └──────┘  └──────┘ └───────┘
```

## Features Demonstrated

✅ Photo upload with drag-and-drop
✅ Real-time processing status
✅ Automatic thumbnail generation
✅ Metadata extraction
✅ Event-driven architecture
✅ Circuit breaker patterns
✅ Retry mechanisms with exponential backoff
✅ Caching strategies (Redis)
✅ Message queue (RabbitMQ)
✅ Database persistence (PostgreSQL)
✅ Comprehensive error handling
✅ Health monitoring
✅ Metrics collection

## Default Test Data

The system starts with no data. To test:

1. Use the web UI to upload photos
2. Or use the cURL commands above
3. Check RabbitMQ UI to see message flow
4. View logs to see processing events

## Need Help?

- 📖 Read `README.md` for full documentation
- 📋 Check `QUICKSTART.md` for detailed setup
- 📊 See `PROJECT_SUMMARY.md` for architecture details
- 🔍 View logs: `docker compose logs -f`

---

**Ready?** → Start Docker Desktop, then run `./start.sh`

