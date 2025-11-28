#!/bin/bash

# Start Frontend locally

cd "$(dirname "$0")/frontend/host"

echo "╔═══════════════════════════════════════════════════════╗"
echo "║         Starting Photo Upload Frontend (React)       ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "📍 Location: frontend/host"
echo "🌐 Will be available at: http://localhost:3000"
echo ""
echo "⏳ Starting development server..."
echo "   (Browser will open automatically)"
echo ""

npm start

