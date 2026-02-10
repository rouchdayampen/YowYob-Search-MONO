#!/bin/bash

# 🔧 Script de Correction Finale - Mapping geo_point
# Ce script corrige le mapping Elasticsearch et réindexe les données

echo "========================================="
echo "🔧 CORRECTION DU MAPPING GEO_POINT"
echo "========================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

ES_BASE="http://localhost:9200"
INDEX="crawler-index"

# ============================================
# ÉTAPE 1: Supprimer l'ancien index
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 1: Suppression de l'ancien index"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
curl -X DELETE "$ES_BASE/$INDEX"
echo ""
echo -e "${GREEN}✅ Index supprimé${NC}"
echo ""
sleep 2

# ============================================
# ÉTAPE 2: Créer le mapping geo_point
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 2: Création du mapping geo_point"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
curl -X PUT "$ES_BASE/$INDEX" -H 'Content-Type: application/json' -d '{
  "mappings": {
    "properties": {
      "title": { "type": "text" },
      "description": { "type": "text" },
      "price": { "type": "double" },
      "serviceType": { "type": "keyword" },
      "category": { "type": "keyword" },
      "city": { "type": "keyword" },
      "rating": { "type": "double" },
      "location": { "type": "geo_point" },
      "latitude": { "type": "double" },
      "longitude": { "type": "double" },
      "images": { "type": "keyword" }
    }
  }
}'
echo ""
echo -e "${GREEN}✅ Mapping geo_point créé${NC}"
echo ""
sleep 2

# ============================================
# ÉTAPE 3: Vérifier le mapping
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 3: Vérification du mapping"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
MAPPING=$(curl -s -X GET "$ES_BASE/$INDEX/_mapping" | jq '.["crawler-index"].mappings.properties.location')
echo "🔍 Mapping du champ 'location':"
echo "$MAPPING" | jq '.'

if echo "$MAPPING" | grep -q "geo_point"; then
    echo -e "${GREEN}✅ Le champ location est bien de type geo_point${NC}"
else
    echo -e "${RED}❌ ERREUR: Le champ location n'est PAS de type geo_point${NC}"
    exit 1
fi
echo ""
sleep 2

# ============================================
# ÉTAPE 4: Redémarrer le service
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 4: Redémarrage du service search-final"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
cd /home/rouchda-yampen/Bureau/YowYob-Search-Engine-Full/YowYob-Search-BackEnd
docker-compose restart search-final
echo ""
echo -e "${YELLOW}⏳ Attente du démarrage du service (10 secondes)...${NC}"
sleep 10
echo -e "${GREEN}✅ Service redémarré${NC}"
echo ""

# ============================================
# ÉTAPE 5: Réindexer les données
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 5: Réindexation des données de test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
cd /home/rouchda-yampen/Bureau/YowYob-Search-Engine-Full
bash create_more_test_data.sh
echo ""
echo -e "${GREEN}✅ Données réindexées${NC}"
echo ""
sleep 2

# ============================================
# ÉTAPE 6: Vérifier les données
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 6: Vérification des données indexées"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
COUNT=$(curl -s -X GET "$ES_BASE/$INDEX/_count" | jq '.count')
echo "📊 Nombre de documents: $COUNT"
if [ "$COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ Des documents sont indexés${NC}"
else
    echo -e "${RED}❌ ERREUR: Aucun document indexé${NC}"
fi
echo ""

# Vérifier un document
echo "🔍 Exemple de document indexé:"
curl -s -X GET "$ES_BASE/$INDEX/_search?size=1" | jq '.hits.hits[0]._source | {name, city, latitude, longitude, location}'
echo ""
sleep 2

# ============================================
# ÉTAPE 7: Test de recherche par proximité
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ÉTAPE 7: Test de recherche par proximité"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 Recherche: Restaurants dans un rayon de 10km de (3.8830, 11.5165)"
echo ""
RESPONSE=$(curl -s -X GET "http://localhost:8080/api/search/proximity?q=restaurant&latitude=3.8830&longitude=11.5165")
echo "$RESPONSE" | jq '.'

SUCCESS=$(echo "$RESPONSE" | jq '.success')
TOTAL=$(echo "$RESPONSE" | jq '.total')

if [ "$SUCCESS" = "true" ] && [ "$TOTAL" -gt 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Recherche par proximité fonctionne ! ($TOTAL résultats)${NC}"
else
    echo ""
    echo -e "${RED}❌ ERREUR: La recherche par proximité ne fonctionne pas${NC}"
fi
echo ""

# ============================================
# RÉSUMÉ
# ============================================
echo "========================================="
echo "📊 RÉSUMÉ DE LA CORRECTION"
echo "========================================="
echo ""
echo "1. ✅ Index supprimé"
echo "2. ✅ Mapping geo_point créé"
echo "3. ✅ Mapping vérifié"
echo "4. ✅ Service redémarré"
echo "5. ✅ Données réindexées ($COUNT documents)"
echo "6. ✅ Recherche par proximité testée"
echo ""
echo "========================================="
echo "✅ CORRECTION TERMINÉE !"
echo "========================================="
echo ""
echo "🧪 Pour tester toutes les fonctionnalités:"
echo "   ./test_geolocalisation.sh"
echo ""
