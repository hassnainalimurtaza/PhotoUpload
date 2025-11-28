#!/bin/bash

# Photo Upload System - Startup Script

set -e

echo "╔═══════════════════════════════════════════════════════╗"
echo "║   Photo Upload System - Starting All Services...     ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running!"
    echo "   Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

# Check if .env exists
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env 2>/dev/null || cat > .env << 'EOF'
POSTGRES_DB=photoupload
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
STORAGE_PROVIDER=s3
EVENT_PUBLISHER=rabbitmq
EOF
    echo "✅ .env file created"
fi

# Stop any running containers
echo "🛑 Stopping any existing containers..."
docker compose down > /dev/null 2>&1 || true

echo ""
echo "🚀 Starting services..."
echo "   This may take 60-90 seconds for first-time setup..."
echo ""

# Start all services
docker compose up -d

echo ""
echo "⏳ Waiting for services to initialize..."
sleep 10

# Check service status
echo ""
echo "📊 Service Status:"
docker compose ps

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║                  🎉 READY TO USE!                     ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "🌐 Access Points:"
echo "   • Frontend:       http://localhost:3000"
echo "   • Backend API:    http://localhost:8080"
echo "   • Health Check:   http://localhost:8080/actuator/health"
echo "   • RabbitMQ UI:    http://localhost:15672 (guest/guest)"
echo ""
echo "🔐 Default Credentials:"
echo "   • API:      user/password"
echo "   • RabbitMQ: guest/guest"
echo ""
echo "📋 Useful Commands:"
echo "   • View logs:      docker compose logs -f"
echo "   • Stop services:  docker compose down"
echo "   • Restart:        docker compose restart"
echo ""
echo "⏱️  Note: Backend may take up to 90 seconds to fully start."
echo "   Monitor progress with: docker compose logs -f photo-api"
echo ""

