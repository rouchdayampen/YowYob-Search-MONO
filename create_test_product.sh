#!/bin/bash

# Configuration
ES_HOST="localhost:9200"
INDEX="crawler-index"

# Product Data (JSON)
# We are creating a product located in "Bastos", a neighborhood in Yaoundé.
# Coordinates for Bastos, Yaoundé: approx 3.8828, 11.5161
JSON_DATA='{
  "title": "Appartement moderne à Bastos",
  "description": "Superbe appartement situé en plein cœur du quartier Bastos. Idéal pour expatriés. Proche des ambassades.",
  "price": 250000,
  "serviceType": "product",
  "category": "Immobilier",
  "location": "Yaoundé",
  "rating": 4.8,
  "latitude": 3.8828,
  "longitude": 11.5161,
  "images": [
    "https://cf.bstatic.com/xdata/images/hotel/max1024x768/265893260.jpg?k=3f27198129023418579058608827725838088085789693710815152865766288&o=&hp=1"
  ],
  "shop": {
      "name": "Immo Bastos Deluxe",
      "phone": "+237 600000000",
      "email": "contact@immobastos.com",
      "address": "Bastos, Yaoundé"
  },
  "createdAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
  "updatedAt": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
}'

echo "Inserting test product into $INDEX..."

curl -s -X POST "$ES_HOST/$INDEX/_doc?refresh=true" \
     -H "Content-Type: application/json" \
     -d "$JSON_DATA"

echo -e "\n\nDone! Product inserted."
echo "You can search for 'Bastos' or 'Yaoundé' to find this product."
