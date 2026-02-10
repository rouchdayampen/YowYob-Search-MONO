#!/bin/bash

# Configuration
ES_HOST="localhost:9200"
INDEX="crawler-index"

echo "Creating diverse test data in $INDEX..."

# 1. RESTAURANT (for "je veux manger", "faim", "resto")
JSON_RESTO='{
  "title": "Le Gourmet de Bastos",
  "description": "Restaurant gastronomique au cœur de Bastos. Cuisine locale et internationale. Ambiance feutrée.",
  "price": 15000,
  "serviceType": "product",
  "category": "Restaurant",
  "location": "Yaoundé",
  "rating": 4.9,
  "latitude": 3.8830,
  "longitude": 11.5165,
  "images": [
    "https://cf.bstatic.com/xdata/images/hotel/max1024x768/265893260.jpg"
  ],
  "shop": {
      "name": "Le Gourmet",
      "address": "Bastos, Yaoundé"
  },
  "createdAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
  "updatedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
}'

curl -s -X POST "$ES_HOST/$INDEX/_doc?refresh=true" -H "Content-Type: application/json" -d "$JSON_RESTO"
echo ""
echo "Inserted Restaurant."

# 2. HOTEL (for "dormir", "hotel")
JSON_HOTEL='{
  "title": "Hôtel Mont Fébé",
  "description": "Hôtel 4 étoiles offrant une vue panoramique sur la ville de Yaoundé. Piscine, spa et chambres luxueuses.",
  "price": 85000,
  "serviceType": "product",
  "category": "Immobilier", 
  "location": "Yaoundé",
  "rating": 4.5,
  "latitude": 3.8950,
  "longitude": 11.5050,
  "images": [
    "https://cf.bstatic.com/xdata/images/hotel/max1024x768/265893260.jpg"
  ],
  "shop": {
      "name": "Mont Fébé",
      "address": "Mont Fébé, Yaoundé"
  },
  "createdAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
  "updatedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
}'
# Note: Keeping category "Immobilier" or "Hôtel" depending on mapping? 
# KeywordParser maps "hotel" -> "Immobilier" currently (based on my edit: CATEGORY_MAPPING.put("hotel", catImmo);). 
# So I set category "Immobilier" here to ensure match. 
# Wait, if inferredCategory is "Immobilier", and doc category is "Immobilier", it matches.

curl -s -X POST "$ES_HOST/$INDEX/_doc?refresh=true" -H "Content-Type: application/json" -d "$JSON_HOTEL"
echo ""
echo "Inserted Hotel."

# 3. ELECTRONICS (for "iphon", "telephone")
JSON_PHONE='{
  "title": "iPhone 15 Pro Max",
  "description": "Le dernier smartphone d Apple. 256GB, Titane Naturel. Neuf scellé.",
  "price": 950000,
  "serviceType": "product",
  "category": "Electronique",
  "location": "Douala",
  "rating": 5.0,
  "latitude": 4.0511,
  "longitude": 9.7679,
  "images": [
    "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-finish-select-202309-6-7inch-naturaltitanium"
  ],
  "shop": {
      "name": "iStore Douala",
      "address": "Akwa, Douala"
  },
  "createdAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
  "updatedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
}'

curl -s -X POST "$ES_HOST/$INDEX/_doc?refresh=true" -H "Content-Type: application/json" -d "$JSON_PHONE"
echo ""
echo "Inserted iPhone."

echo "Done creating test data."
