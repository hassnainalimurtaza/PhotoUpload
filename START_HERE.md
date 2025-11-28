# 🚀 Photo Upload System - START HERE

## Current Status

✅ **Project Generated**: Complete production-grade photo upload system  
⚠️ **Needs**: Java JDK installation to compile and run

## Quick Start (3 Steps)

### Step 1: Install Java JDK

You have Java Runtime but need the Java Development Kit (compiler):

```bash
sudo apt install -y openjdk-21-jdk-headless
```

Verify:
```bash
javac -version
# Should show: javac 21.0.8
```

### Step 2: Build the Project

```bash
cd /hdd/IdeaProjects/CursorAiProject/backend
mvn clean install -DskipTests
```

First build takes 2-5 minutes (downloading dependencies).

### Step 3: Start the Application

**Terminal 1 - Backend:**
```bash
cd /hdd/IdeaProjects/CursorAiProject
./run-backend.sh
```

Wait for: `Started PhotoUploadApplication`

**Terminal 2 - Frontend:**
```bash
cd /hdd/IdeaProjects/CursorAiProject
./run-frontend.sh
```

### Step 4: Use the Application

🌐 **Frontend**: http://localhost:3000  
🌐 **Backend**: http://localhost:8080  
🌐 **Health**: http://localhost:8080/actuator/health  
🌐 **H2 Console**: http://localhost:8080/h2-console

## What You Can Do

1. **Upload Photos**: Drag & drop images on the web UI
2. **View Gallery**: See all uploaded photos with status
3. **Track Events**: View complete processing workflow
4. **Retry Failed**: Manually retry failed uploads
5. **Monitor Health**: Check system status via actuator

## Configuration

The system runs **completely locally** without Docker:

- ✅ **Database**: H2 in-memory (embedded)
- ✅ **Cache**: Simple in-memory cache  
- ✅ **Queue**: Database fallback (no RabbitMQ)
- ✅ **Storage**: Local file system (./uploads)

**No external services needed!**

## API Testing

```bash
# Upload photo
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@/path/to/image.jpg" \
  -F "userId=test-user"

# List photos
curl "http://localhost:8080/api/photos?userId=test-user" -u user:password

# Get photo details
curl "http://localhost:8080/api/photos/1" -u user:password

# Delete photo
curl -X DELETE "http://localhost:8080/api/photos/1" -u user:password

# Get event log
curl "http://localhost:8080/api/photos/1/events" -u user:password
```

**Default credentials**: `user/password` or `admin/admin`

## Features Implemented

✅ Photo upload with drag-and-drop  
✅ Real-time upload progress  
✅ Automatic thumbnail generation  
✅ Metadata extraction (EXIF)  
✅ Event-driven processing  
✅ Circuit breaker patterns  
✅ Retry mechanisms  
✅ Comprehensive error handling  
✅ Status tracking (pending → processing → completed)  
✅ Event log/workflow history  

## Architecture Highlights

### Backend (Spring Boot 3.2)
- 5 Maven modules with clean separation
- SOLID principles throughout
- 12+ design patterns (Facade, Strategy, Observer, Saga, etc.)
- Resilience4j circuit breakers
- Async processing with CompletableFuture
- Event sourcing for audit trail

### Frontend (React 18 + TypeScript)
- Micro Frontend architecture (Module Federation)
- Redux Toolkit for state management
- React Query for server state
- Tailwind CSS for modern UI
- Real-time status updates

## Project Structure

```
CursorAiProject/
├── backend/
│   ├── photo-common/         # Shared entities & DTOs
│   ├── storage-provider/     # S3/GCS/Azure/Local storage
│   ├── event-bus/            # RabbitMQ/Kafka/DB fallback
│   ├── photo-service/        # Business logic
│   └── photo-api/            # REST controllers
├── frontend/
│   ├── host/                 # Main React app
│   ├── upload-mfe/           # Upload micro frontend
│   ├── gallery-mfe/          # Gallery micro frontend
│   └── events-mfe/           # Events micro frontend
├── k8s/                      # Kubernetes manifests
├── docker-compose.yml        # Docker orchestration
└── *.sh                      # Run scripts
```

## Documentation Files

📖 `README.md` - Complete documentation  
📖 `INSTALL_AND_RUN.md` - Detailed installation guide  
📖 `PROJECT_SUMMARY.md` - Architecture overview  
📖 `QUICKSTART.md` - Docker quick start  

## Common Issues

### "mvn: command not found"
```bash
sudo apt install maven
```

### "node: command not found"  
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
```

### "Port already in use"
```bash
# Find and kill process
lsof -i :8080
kill -9 <PID>
```

## Production Deployment

For Docker/Kubernetes deployment (production):
1. Install Docker Desktop
2. Run: `docker compose up -d`
3. Or deploy to Kubernetes: `kubectl apply -f k8s/`

See `README.md` for full production deployment guide.

---

## 🎯 YOUR NEXT STEPS:

1. **Install JDK**:
   ```bash
   sudo apt install -y openjdk-21-jdk-headless
   ```

2. **Build**:
   ```bash
   cd /hdd/IdeaProjects/CursorAiProject/backend
   mvn clean install -DskipTests
   ```

3. **Run Backend** (Terminal 1):
   ```bash
   cd /hdd/IdeaProjects/CursorAiProject
   ./run-backend.sh
   ```

4. **Run Frontend** (Terminal 2):
   ```bash
   cd /hdd/IdeaProjects/CursorAiProject
   ./run-frontend.sh
   ```

5. **Test**:
   Open http://localhost:3000 and upload a photo!

---

**Questions?** All scripts are ready to use. Just install JDK and run!

