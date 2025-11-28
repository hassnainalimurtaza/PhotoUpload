# 🚀 Install and Run Guide - Photo Upload System

## ⚠️ Issue Detected: Missing Java Compiler (JDK)

You have **Java Runtime (JRE)** installed but need the **Java Development Kit (JDK)** to compile the project.

## Step 1: Install Java JDK (Required)

Run this command to install Java 21 JDK:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
```

Verify installation:
```bash
javac -version
# Should show: javac 21.0.8
```

## Step 2: Build the Backend

```bash
cd /hdd/IdeaProjects/CursorAiProject/backend
mvn clean install -DskipTests
```

This will take 2-5 minutes for the first build (downloading dependencies).

## Step 3: Run Backend (Terminal 1)

```bash
cd /hdd/IdeaProjects/CursorAiProject
./run-backend.sh
```

**Wait for this message:**
```
Started PhotoUploadApplication in X.XXX seconds
```

## Step 4: Run Frontend (Terminal 2 - New Terminal)

```bash
cd /hdd/IdeaProjects/CursorAiProject
./run-frontend.sh
```

## Step 5: Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health  
- **H2 Database Console**: http://localhost:8080/h2-console

## 🎯 Quick Test

### Test Backend API:
```bash
# Health check
curl http://localhost:8080/actuator/health | jq

# Upload a photo (create a test image first)
curl -X POST http://localhost:8080/api/photos/upload \
  -u user:password \
  -F "file=@/path/to/test-image.jpg" \
  -F "userId=test-user-123"

# List photos
curl "http://localhost:8080/api/photos?userId=test-user-123" \
  -u user:password | jq
```

### Test Frontend UI:
1. Open http://localhost:3000
2. Drag and drop an image file
3. Watch the upload progress
4. Navigate to Gallery to see uploaded photos
5. Check Events to see processing workflow

## 📋 What Runs Locally (No Docker Needed)

✅ **Backend**: Spring Boot on port 8080  
✅ **Frontend**: React dev server on port 3000  
✅ **Database**: H2 in-memory database (embedded)  
✅ **Cache**: Simple in-memory cache (embedded)  
✅ **Queue**: Database fallback (no RabbitMQ needed)  
✅ **Storage**: Local file system (./uploads directory)  

## 🛑 Stopping the Application

Press `Ctrl+C` in each terminal running the backend/frontend.

## 🐛 Troubleshooting

### Problem: "javac: command not found"
```bash
# Install JDK
sudo apt install -y openjdk-21-jdk-headless

# Verify
javac -version
```

### Problem: "Port 8080 already in use"
```bash
# Find what's using port 8080
lsof -i :8080

# Kill it
kill -9 <PID>

# Or change port in application-local.yml
```

### Problem: Maven build fails
```bash
# Clean everything
cd backend
mvn clean

# Try again
mvn install -DskipTests
```

### Problem: Frontend won't start
```bash
cd frontend/host

# Remove node_modules
rm -rf node_modules package-lock.json

# Reinstall
npm install

# Start
npm start
```

## 💡 Development Tips

### View Backend Logs
The backend logs will show in the terminal where you ran `./run-backend.sh`

### View H2 Database
1. Go to: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:photoupload`
3. Username: `sa`
4. Password: (leave empty)
5. Click "Connect"

### Uploaded Files Location
Photos are stored locally at: `/hdd/IdeaProjects/CursorAiProject/backend/photo-api/uploads/`

### API Documentation
All endpoints:
- POST /api/photos/upload
- GET /api/photos
- GET /api/photos/{id}
- DELETE /api/photos/{id}
- GET /api/photos/{id}/events
- POST /api/photos/{id}/retry

Default credentials: `user/password` or `admin/admin`

## 🔄 Auto-Restart on Code Changes

### Backend (Spring Boot DevTools):
Already configured - changes to Java files will trigger auto-restart

### Frontend (Webpack Dev Server):
Already configured - changes to React files will trigger hot reload

## ✅ Success Checklist

- [ ] JDK 21 installed (`javac -version` works)
- [ ] Backend builds successfully (`mvn clean install`)
- [ ] Backend starts (`./run-backend.sh`)
- [ ] Health endpoint returns 200 (`curl http://localhost:8080/actuator/health`)
- [ ] Frontend installs dependencies (`npm install`)
- [ ] Frontend starts (`./run-frontend.sh`)
- [ ] Can access UI at http://localhost:3000
- [ ] Can upload a photo via UI
- [ ] Can see photo in gallery

---

## 🎉 Next Steps After Installation

1. Install JDK: `sudo apt install -y openjdk-21-jdk-headless`
2. Build: `cd backend && mvn clean install -DskipTests`
3. Run backend: `./run-backend.sh` (Terminal 1)
4. Run frontend: `./run-frontend.sh` (Terminal 2)
5. Open: http://localhost:3000
6. Test: Upload a photo!

**Need help?** Check the logs in the terminal where backend/frontend are running.

