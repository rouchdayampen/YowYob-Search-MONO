#!/bin/bash
echo "🚀 Démarrage de YowYob..."

# Backend Docker
cd "$(dirname "$0")/YowYob-Search-BackEnd"
docker start yowyob-elasticsearch yowyob-postgres yowyob-redis yowyob-rabbitmq kafka yowyob-embeddings yowyob-monolith crawler-service 2>/dev/null

echo ""
echo "✅ Backend démarré !"
echo "👉 Lance le frontend dans un autre terminal :"
echo "   cd YowYob-Search-Frontend && npm run dev"
echo ""
echo "🌐 Frontend : http://localhost:3000"
echo "🔧 Backend  : http://localhost:8080"
