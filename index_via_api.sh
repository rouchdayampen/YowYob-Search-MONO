#!/bin/bash
API_URL="http://localhost:8082/api/search/index"

echo "🆕 Création de l index avec le mapping geo_point manuel..."
curl -s -X PUT "http://localhost:9200/crawler-index" -H 'Content-Type: application/json' -d '{
  "mappings": {
    "properties": {
      "location": { "type": "geo_point" },
      "title": { "type": "text" },
      "description": { "type": "text" },
      "price": { "type": "double" },
      "serviceType": { "type": "keyword" },
      "category": { "type": "keyword" },
      "city": { "type": "keyword" },
      "rating": { "type": "double" },
      "latitude": { "type": "double" },
      "longitude": { "type": "double" },
      "images": { "type": "keyword" },
      "_class": { "type": "keyword" }
    }
  }
}'

echo "🚀 Indexation des données de test via l API du service..."

# 1. RESTAURANT à Yaoundé
echo "📍 Indexation: Restaurant Le Gourmet de Bastos"
curl -s -X POST "$API_URL" -H "Content-Type: application/json" -d '{
  "id": "resto-1",
  "title": "Le Gourmet de Bastos",
  "description": "Restaurant gastronomique au cœur de Bastos. Cuisine locale et internationale. Ambiance feutrée.",
  "price": 15000,
  "type": "listing",
  "category": "Restaurant",
  "city": "Yaoundé",
  "rating": 4.9,
  "latitude": 3.8830,
  "longitude": 11.5165,
  "images": ["https://cf.bstatic.com/xdata/images/hotel/max1024x768/265893260.jpg"]
}' | jq '.'

# 2. HOTEL à Yaoundé
echo "📍 Indexation: Hôtel Mont Fébé"
curl -s -X POST "$API_URL" -H "Content-Type: application/json" -d '{
  "id": "hotel-1",
  "title": "Hôtel Mont Fébé",
  "description": "Hôtel 4 étoiles offrant une vue panoramique sur la ville de Yaoundé. Piscine, spa et chambres luxueuses.",
  "price": 85000,
  "type": "listing",
  "category": "Immobilier",
  "city": "Yaoundé",
  "rating": 4.5,
  "latitude": 3.8950,
  "longitude": 11.5050,
  "images": ["https://cf.bstatic.com/xdata/images/hotel/max1024x768/265893260.jpg"]
}' | jq '.'

# 3. IPHONE à Douala
echo "📍 Indexation: iPhone 15 Pro Max"
curl -s -X POST "$API_URL" -H "Content-Type: application/json" -d '{
  "id": "phone-1",
  "title": "iPhone 15 Pro Max",
  "description": "Le dernier smartphone d Apple. 256GB, Titane Naturel. Neuf scellé.",
  "price": 950000,
  "type": "listing",
  "category": "Electronique",
  "city": "Douala",
  "rating": 5.0,
  "latitude": 4.0511,
  "longitude": 9.7679,
  "images": ["https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-finish-select-202309-6-7inch-naturaltitanium"]
}' | jq '.'

# 4. PIZZA à Yaoundé (proche du restaurant)
echo "📍 Indexation: Pizzeria Napoli"
curl -s -X POST "$API_URL" -H "Content-Type: application/json" -d '{
  "id": "pizza-1",
  "title": "Pizzeria Napoli",
  "description": "Pizzeria italienne authentique. Pizzas au feu de bois, pâtes fraîches.",
  "price": 8000,
  "type": "listing",
  "category": "Restaurant",
  "city": "Yaoundé",
  "rating": 4.7,
  "latitude": 3.8820,
  "longitude": 11.5170,
  "images": []
}' | jq '.'

# 5. CAFE à Yaoundé (un peu plus loin)
echo "📍 Indexation: Café des Arts"
curl -s -X POST "$API_URL" -H "Content-Type: application/json" -d '{
  "id": "cafe-1",
  "title": "Café des Arts",
  "description": "Café cosy avec terrasse. Excellent café, pâtisseries maison.",
  "price": 2500,
  "type": "listing",
  "category": "Restaurant",
  "city": "Yaoundé",
  "rating": 4.6,
  "latitude": 3.8700,
  "longitude": 11.5200,
  "images": []
}' | jq '.'

echo ""
echo "✅ Indexation terminée !"
echo ""
echo "📊 Vérification du nombre de documents:"
curl -s -X GET "http://localhost:9200/crawler-index/_count" | jq '.'

echo ""
echo "🗺️ Vérification du mapping geo_point:"
curl -s -X GET "http://localhost:9200/crawler-index/_mapping?pretty" | jq '.["crawler-index"].mappings.properties | {location, latitude, longitude, city}'
