#!/bin/bash

# Start Backend API locally

cd "$(dirname "$0")/backend/photo-api"

echo "╔═══════════════════════════════════════════════════════╗"
echo "║          Starting Photo Upload Backend API           ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "📍 Location: backend/photo-api"
echo "🌐 Will be available at: http://localhost:8080"
echo "🏥 Health check: http://localhost:8080/actuator/health"
echo ""
echo "⏳ Starting Spring Boot application with 'local' profile..."
echo "   (This may take 30-60 seconds for first startup)"
echo "   Using H2 in-memory database (no external DB needed)"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=local

