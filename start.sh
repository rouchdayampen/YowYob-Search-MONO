#!/bin/bash

# Configuration
BACKEND_DIR="$(dirname "$0")/YowYob-Search-BackEnd"
FRONTEND_DIR="$(dirname "$0")/YowYob-Search-Frontend"

echo "🚀 Démarrage optimisé de YowYob..."

# 1. Nettoyage des conteneurs orphelins (optionnel mais recommandé)
# cd "$BACKEND_DIR" && docker compose down --remove-orphans

# 2. Démarrage du Backend via Docker Compose
echo "📦 Lancement des services Backend (RAM optimisée)..."
cd "$BACKEND_DIR"
docker compose up -d postgres elasticsearch redis rabbitmq zookeeper kafka
echo "⏳ Attente du démarrage de l'infrastructure (10s)..."
sleep 10

echo "⚙️  Lancement des Microservices..."
docker compose up -d api-gateway auth-service search-final listing-service user-service geo-final crawler-service

# 3. Vérification du Frontend
echo ""
echo "✅ Backend lancé avec succès !"
echo "👉 Pour lancer le frontend, exécute ceci dans un NOUVEAU terminal :"
echo "   cd $FRONTEND_DIR && npm run dev"
echo ""
echo "🌐 Interface : http://localhost:3000"
echo "🔧 API Gateway: http://localhost:8080"
echo "🔍 ElasticSearch: http://localhost:9200"
