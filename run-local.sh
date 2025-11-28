#!/bin/bash

# Photo Upload System - Local Development Run Script

set -e

echo "╔═══════════════════════════════════════════════════════╗"
echo "║   Photo Upload System - Local Development Setup      ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher required. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java $JAVA_VERSION found"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.9+."
    exit 1
fi
echo "✅ Maven found"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 18+."
    exit 1
fi
echo "✅ Node.js $(node --version) found"

# Check npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm not found. Please install npm."
    exit 1
fi
echo "✅ npm $(npm --version) found"

echo ""
echo "🔧 Starting infrastructure services (PostgreSQL, Redis, RabbitMQ)..."
echo "   This requires Docker for the databases..."
echo ""

# Start only infrastructure services
docker compose up -d postgres redis rabbitmq 2>/dev/null || {
    echo "❌ Failed to start infrastructure services."
    echo "   Please ensure Docker is running and try again."
    echo ""
    echo "   Alternative: Install PostgreSQL, Redis, and RabbitMQ locally"
    exit 1
}

echo "⏳ Waiting for services to be ready..."
sleep 15

echo ""
echo "✅ Infrastructure services started"
echo ""
echo "🏗️  Building backend..."
cd backend
mvn clean install -DskipTests -q

echo ""
echo "✅ Backend built successfully"
echo ""
echo "📦 Installing frontend dependencies..."
cd ../frontend/host
npm install --silent

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║              Ready to Start Applications!            ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "🚀 Starting Backend (Terminal 1)..."
echo "   Navigate to: cd backend/photo-api"
echo "   Run: mvn spring-boot:run"
echo ""
echo "🚀 Starting Frontend (Terminal 2)..."
echo "   Navigate to: cd frontend/host"
echo "   Run: npm start"
echo ""
echo "Or use the provided scripts:"
echo "   ./run-backend.sh  (in one terminal)"
echo "   ./run-frontend.sh (in another terminal)"
echo ""

