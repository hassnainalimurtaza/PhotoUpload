#!/bin/bash

# Photo Upload System - Stop Script

echo "🛑 Stopping Photo Upload System..."
echo ""

cd "$(dirname "$0")"

# Stop all services
docker compose down

echo ""
echo "✅ All services stopped"
echo ""
echo "💡 To remove all data (database, cache, etc):"
echo "   docker compose down -v"
echo ""

