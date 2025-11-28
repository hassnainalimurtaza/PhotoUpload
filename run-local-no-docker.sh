#!/bin/bash

# Photo Upload System - Fully Local Run (No Docker Required)

set -e

echo "╔═══════════════════════════════════════════════════════╗"
echo "║   Photo Upload System - Local Development (No Docker)║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.9+"
    exit 1
fi
echo "✅ Maven found: $(mvn --version | head -n 1)"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 18+"
    exit 1
fi
echo "✅ Node.js found: $(node --version)"

echo ""
echo "🏗️  Building backend (this may take a few minutes)..."
cd backend
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Backend build failed"
    exit 1
fi

echo ""
echo "✅ Backend built successfully!"
echo ""
echo "📦 Installing frontend dependencies..."
cd ../frontend/host
if [ ! -d "node_modules" ]; then
    npm install
else
    echo "   (dependencies already installed)"
fi

echo ""
echo "✅ Frontend dependencies ready!"
echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║                   🎉 READY TO RUN!                    ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "📋 Next steps:"
echo ""
echo "   1. Open a NEW terminal and run:"
echo "      cd $(pwd)/../.."
echo "      ./run-backend.sh"
echo ""
echo "   2. Open ANOTHER terminal and run:"
echo "      cd $(pwd)/../.."
echo "      ./run-frontend.sh"
echo ""
echo "   Or use tmux/screen to run both in background:"
echo "      ./run-backend.sh &"
echo "      ./run-frontend.sh &"
echo ""
echo "🌐 Access points:"
echo "   • Frontend:    http://localhost:3000"
echo "   • Backend API: http://localhost:8080"
echo "   • H2 Console:  http://localhost:8080/h2-console"
echo "   • Health:      http://localhost:8080/actuator/health"
echo ""
echo "💡 This setup uses:"
echo "   • H2 in-memory database (no PostgreSQL needed)"
echo "   • Simple in-memory cache (no Redis needed)"
echo "   • Database fallback (no RabbitMQ needed)"
echo "   • Local file storage (no S3 needed)"
echo ""

