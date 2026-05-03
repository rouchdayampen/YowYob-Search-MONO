#!/bin/bash

# =============================================
# YowYob - Script de démarrage LÉGER
# Sans Kafka/Zookeeper/Crawler → ~3.5 Go RAM
# =============================================

BACKEND_DIR="$(dirname "$0")/YowYob-Search-BackEnd"
FRONTEND_DIR="$(dirname "$0")/YowYob-Search-Frontend"
COMPOSE_FILE="docker-compose.light.yml"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   🚀 YowYob - Démarrage Mode Léger         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# --- Vérification RAM ---
AVAILABLE_RAM=$(free -m | awk '/^Mem:/{print $7}')
echo -e "${YELLOW}💾 RAM disponible : ${AVAILABLE_RAM} Mo${NC}"
if [ "$AVAILABLE_RAM" -lt 2000 ]; then
  echo -e "${RED}⚠️  Moins de 2 Go de RAM disponible ! Risque de plantage.${NC}"
  echo -e "${RED}   Ferme d'autres applications d'abord.${NC}"
  read -p "Continuer quand même ? (o/N) " confirm
  [[ "$confirm" != "o" && "$confirm" != "O" ]] && exit 1
fi

echo ""
echo -e "${YELLOW}📌 SERVICES ACTIVÉS :${NC}"
echo -e "   ✅ PostgreSQL, Redis, RabbitMQ, Elasticsearch"
echo -e "   ✅ api-gateway, auth, user, listing, search, geo"
echo -e "   ❌ Kafka, Zookeeper, crawler, monitoring (trop gourmands)"
echo ""

# --- Nettoyage ---
echo -e "${BLUE}🧹 Arrêt et nettoyage des anciens conteneurs...${NC}"
cd "$BACKEND_DIR"
docker compose -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null
echo -e "${GREEN}   ✓ Nettoyé${NC}"
echo ""

# --- VAGUE 1 : Infrastructure de base ---
echo -e "${BLUE}📦 VAGUE 1 — PostgreSQL, Redis, RabbitMQ...${NC}"
docker compose -f "$COMPOSE_FILE" up -d postgres redis rabbitmq

echo -e "${YELLOW}   ⏳ Attente que PostgreSQL soit healthy (max 60s)...${NC}"
for i in {1..12}; do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' yowyob-postgres 2>/dev/null)
  if [ "$STATUS" = "healthy" ]; then
    echo -e "${GREEN}   ✓ PostgreSQL prêt !${NC}"
    break
  fi
  echo -e "${YELLOW}   ... tentative $i/12 (PostgreSQL: $STATUS)${NC}"
  sleep 5
done

echo ""

# --- VAGUE 2 : Elasticsearch ---
echo -e "${BLUE}🔍 VAGUE 2 — Elasticsearch (256Mo RAM)...${NC}"
docker compose -f "$COMPOSE_FILE" up -d elasticsearch

echo -e "${YELLOW}   ⏳ Attente qu'Elasticsearch démarre (max 90s)...${NC}"
for i in {1..18}; do
  if curl -s http://localhost:9200/_cluster/health 2>/dev/null | grep -q '"status"'; then
    echo -e "${GREEN}   ✓ Elasticsearch prêt !${NC}"
    break
  fi
  echo -e "${YELLOW}   ... tentative $i/18${NC}"
  sleep 5
done

echo ""

# --- VAGUE 3 : Geo Service ---
echo -e "${BLUE}⚙️  VAGUE 3 — Geo Service...${NC}"
docker compose -f "$COMPOSE_FILE" up -d geo-final
sleep 10
echo -e "${GREEN}   ✓ Geo service lancé${NC}"

echo ""

# --- VAGUE 4 : Auth + User ---
echo -e "${BLUE}⚙️  VAGUE 4 — Auth Service + User Service...${NC}"
docker compose -f "$COMPOSE_FILE" up -d auth-service user-service
echo -e "${YELLOW}   ⏳ Attente 20s (JVM warmup)...${NC}"
sleep 20
echo -e "${GREEN}   ✓ Auth & User lancés${NC}"

echo ""

# --- VAGUE 5 : Listing + Search ---
echo -e "${BLUE}⚙️  VAGUE 5 — Listing Service + Search Service...${NC}"
docker compose -f "$COMPOSE_FILE" up -d listing-service search-final
echo -e "${YELLOW}   ⏳ Attente 20s (JVM warmup)...${NC}"
sleep 20
echo -e "${GREEN}   ✓ Listing & Search lancés${NC}"

echo ""

# --- VAGUE 6 : API Gateway ---
echo -e "${BLUE}⚙️  VAGUE 6 — API Gateway...${NC}"
docker compose -f "$COMPOSE_FILE" up -d api-gateway
echo -e "${YELLOW}   ⏳ Attente 15s...${NC}"
sleep 15
echo -e "${GREEN}   ✓ API Gateway lancé${NC}"

echo ""

# --- Résumé des conteneurs ---
echo -e "${BLUE}📊 État des conteneurs :${NC}"
docker compose -f "$COMPOSE_FILE" ps

echo ""
echo -e "${BLUE}💾 Consommation RAM :${NC}"
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   ✅ Backend prêt ! URLs :                        ║${NC}"
echo -e "${GREEN}║      API Gateway  : http://localhost:8080         ║${NC}"
echo -e "${GREEN}║      Auth Service : http://localhost:8081         ║${NC}"
echo -e "${GREEN}║      Listing      : http://localhost:8083         ║${NC}"
echo -e "${GREEN}║      Search       : http://localhost:8082         ║${NC}"
echo -e "${GREEN}║      User         : http://localhost:8084         ║${NC}"
echo -e "${GREEN}║      Geo          : http://localhost:8085         ║${NC}"
echo -e "${GREEN}║      RabbitMQ UI  : http://localhost:15672        ║${NC}"
echo -e "${GREEN}║      ElasticSearch: http://localhost:9200         ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════╝${NC}"
echo ""

# --- Frontend ---
echo -e "${BLUE}🌐 Démarrage du Frontend Next.js...${NC}"
cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
  echo -e "${YELLOW}📦 Installation des dépendances npm (première fois)...${NC}"
  npm install
fi

echo -e "${GREEN}🚀 Frontend sur http://localhost:3000 (Ctrl+C pour arrêter)${NC}"
echo ""
npm run dev
