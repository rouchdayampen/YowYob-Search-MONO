#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ────────────────────────────────────────────────
KAFKA_CONTAINER="kafka"
ES_HOST="http://localhost:9200"
ES_INDEX="listings"
DB_CONTAINER="postgres"
DB_NAME="yowyob_listings"
DB_USER="postgres"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "${GREEN}✅ $1${NC}"; }
fail() { echo -e "${RED}❌ $1${NC}"; exit 1; }
info() { echo -e "${YELLOW}ℹ️  $1${NC}"; }

# ── Étape 1 : Redémarrer le crawler en mode mock ─────────────────
info "Étape 1 — Redémarrage du Crawler avec stratégie google-local-mock"
docker-compose stop crawler-service
# On force l'activation du test mock et on désactive les autres stratégies
SCRAPER_GOOGLELOCALMOCK_ENABLED=true \
SCRAPER_OSM_ENABLED=false \
SCRAPER_COINAFRIQUE_ENABLED=false \
SCRAPER_DIRECTORY_CM_ENABLED=false \
docker-compose up -d crawler-service

sleep 5

# ── Étape 2 : Vérifier la publication Kafka ──────────────────────
info "Étape 2 — Vérification de la publication Kafka"
KAFKA_OUTPUT=$(docker exec -i "$KAFKA_CONTAINER" \
  kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic crawler.listings.events \
  --from-beginning \
  --timeout-ms 15000 2>/dev/null || true)

if echo "$KAFKA_OUTPUT" | grep -q "Tchokos SARL"; then
    pass "Kafka — message 'Tchokos SARL' trouvé dans crawler.listings.events"
else
    fail "Kafka — message 'Tchokos SARL' absent du topic"
fi

if echo "$KAFKA_OUTPUT" | grep -q "TCHOKOS SERVICE EXPRESS"; then
    pass "Kafka — message 'TCHOKOS SERVICE EXPRESS' trouvé"
else
    fail "Kafka — message 'TCHOKOS SERVICE EXPRESS' absent"
fi

# ── Étape 3 : Vérifier PostgreSQL ────────────────────────────────
info "Étape 3 — Vérification PostgreSQL (attente 10s pour le consumer)"
sleep 10

DB_RESULT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c \
  "SELECT title, rating, phone, reviews_count
   FROM listings
   WHERE title IN ('Tchokos SARL','TCHOKOS SERVICE EXPRESS');")

if echo "$DB_RESULT" | grep -q "3.7"; then
    pass "PostgreSQL — rating=3.7 présent pour Tchokos SARL"
else
    fail "PostgreSQL — rating=3.7 absent"
fi

if echo "$DB_RESULT" | grep -q "6 91 98 10 47"; then
    pass "PostgreSQL — phone='6 91 98 10 47' présent"
else
    fail "PostgreSQL — phone absent"
fi

if echo "$DB_RESULT" | grep -q "5.0"; then
    pass "PostgreSQL — rating=5.0 présent pour TCHOKOS SERVICE EXPRESS"
else
    fail "PostgreSQL — rating=5.0 absent"
fi

# ── Étape 4 : Vérifier Elasticsearch ─────────────────────────────
info "Étape 4 — Vérification Elasticsearch (attente 10s pour sync)"
sleep 10

ES_RESULT=$(curl -s -X GET "$ES_HOST/$ES_INDEX/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match": {
        "title": "Tchokos"
      }
    },
    "_source": ["title", "rating", "reviewsCount", "phone", "openingHours"]
  }')

ES_COUNT=$(echo "$ES_RESULT" | python3 -c \
  "import sys,json; data=json.load(sys.stdin); print(data['hits']['total']['value'] if 'hits' in data and 'total' in data['hits'] else 0)")

if [ "$ES_COUNT" -ge 2 ]; then
    pass "Elasticsearch — $ES_COUNT documents indexés"
else
    fail "Elasticsearch — documents insuffisants (trouvé: $ES_COUNT, attendu: 2)"
fi

if echo "$ES_RESULT" | grep -q "3.7"; then
    pass "Elasticsearch — rating=3.7 indexé correctement"
else
    fail "Elasticsearch — rating=3.7 absent de l'index"
fi

if echo "$ES_RESULT" | grep -q "6 91 98 10 47"; then
    pass "Elasticsearch — phone indexé correctement"
else
    fail "Elasticsearch — phone absent de l'index"
fi

# ── Étape 5 : Restaurer la stratégie OSM ─────────────────────────
info "Étape 5 — Restauration de la configuration par défaut (OSM)"
docker-compose stop crawler-service
docker-compose up -d crawler-service

# ── Résumé ───────────────────────────────────────────────────────
echo ""
echo "════════════════════════════════════════"
pass "Pipeline E2E validé avec succès"
echo "════════════════════════════════════════"
echo ""
info "Prochaine étape : ouvrir le frontend et rechercher 'Tchokos'"
info "Vérifier : étoiles, téléphone, horaires, image affichés correctement"
