#!/bin/bash

# 🧪 Script de Test Complet - Service de Géolocalisation
# Ce script teste toutes les fonctionnalités de géolocalisation après la correction

echo "========================================="
echo "🧪 TESTS DU SERVICE DE GÉOLOCALISATION"
echo "========================================="
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
API_BASE="http://localhost:8082/api/search"
ES_BASE="http://localhost:9200"

echo "📋 Configuration:"
echo "  - API Gateway: $API_BASE"
echo "  - Elasticsearch: $ES_BASE"
echo ""

# ============================================
# TEST 1: Vérification du Mapping Elasticsearch
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 1: Vérification du Mapping geo_point"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 Mapping actuel du champ 'location':"
MAPPING=$(curl -s -X GET "$ES_BASE/crawler-index/_mapping" | jq '.["crawler-index"].mappings.properties.location')
echo "$MAPPING" | jq '.'

if echo "$MAPPING" | grep -q "geo_point"; then
    echo -e "${GREEN}✅ Le champ location est bien de type geo_point${NC}"
else
    echo -e "${RED}❌ ERREUR: Le champ location n'est PAS de type geo_point${NC}"
    echo -e "${YELLOW}⚠️  Type actuel: $(echo "$MAPPING" | jq -r '.type')${NC}"
fi
echo ""

# ============================================
# TEST 2: Comptage des Documents
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 2: Nombre de Documents Indexés"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
COUNT=$(curl -s -X GET "$ES_BASE/crawler-index/_count" | jq '.count')
echo "📊 Nombre de documents: $COUNT"
if [ "$COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ Des documents sont indexés${NC}"
else
    echo -e "${RED}❌ ERREUR: Aucun document indexé${NC}"
fi
echo ""

# ============================================
# TEST 3: Recherche par Proximité (Yaoundé)
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 3: Recherche par Proximité - Restaurants à Yaoundé"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 Requête: Restaurants dans un rayon de 5km de (3.8830, 11.5165)"
echo ""
RESPONSE=$(curl -s -X GET "$API_BASE/near-me?q=restaurant&latitude=3.8830&longitude=11.5165&type=all")
echo "$RESPONSE" | jq '.'

TOTAL=$(echo "$RESPONSE" | jq '.total')
if [ "$TOTAL" -gt 0 ]; then
    echo -e "${GREEN}✅ Recherche par proximité fonctionne ($TOTAL résultats)${NC}"
else
    echo -e "${RED}❌ ERREUR: Aucun résultat trouvé${NC}"
fi
echo ""

# ============================================
# TEST 4: Recherche Standard (Comparaison)
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 4: Recherche Standard - Restaurant"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
RESPONSE=$(curl -s -X GET "$API_BASE?q=restaurant&type=all")
echo "$RESPONSE" | jq '{success, query, total, results: [.results[] | {title, city, latitude, longitude}]}'
echo ""

# ============================================
# TEST 5: Recherche avec Rayon Personnalisé
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 5: Recherche avec Rayon de 1km"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 Requête: Restaurants dans un rayon de 1km de (3.8830, 11.5165)"
echo ""
RESPONSE=$(curl -s -X GET "$API_BASE/proximity?q=restaurant&latitude=3.8830&longitude=11.5165&radius=1")
echo "$RESPONSE" | jq '{total, results: [.results[] | {title, city, distanceKm}]}'
echo ""

# ============================================
# TEST 6: Recherche "Près de chez moi"
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "TEST 6: Recherche 'Près de chez moi'"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 Requête: 'restaurants près de chez moi' avec coordonnées"
echo ""
RESPONSE=$(curl -s -X GET "$API_BASE/near-me?q=restaurants+près+de+chez+moi&latitude=3.8830&longitude=11.5165")
echo "$RESPONSE" | jq '{success, total}'
echo ""

# ============================================
# RÉSUMÉ DES TESTS
# ============================================
echo "========================================="
echo "📊 RÉSUMÉ DES TESTS"
echo "========================================="
echo ""
echo "1. Mapping geo_point: Vérifier manuellement ci-dessus"
echo "2. Documents indexés: $COUNT"
echo "3. Recherche par proximité: Vérifier les résultats ci-dessus"
echo "4. Recherche standard: Vérifier les résultats ci-dessus"
echo "5. Rayon personnalisé: Vérifier les résultats ci-dessus"
echo "6. 'Près de chez moi': Vérifier les résultats ci-dessus"
echo ""
echo "========================================="
echo "✅ Tests terminés !"
echo "========================================="
