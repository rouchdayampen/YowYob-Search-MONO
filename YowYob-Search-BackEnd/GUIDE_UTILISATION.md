# 🚀 Yowyob - Guide d'Utilisation du Système Complet

## 📋 Vue d'ensemble

Yowyob est une plateforme de commerce électronique avec recherche intelligente. Voici comment fonctionne le flux complet :

```
Crawler Service (Port 8086)
    ↓
Scrape des annonces variées (voitures, maisons, électronique, meubles, motos)
    ↓
Listing Service (Base de données - Port 8082)
    ↓ RabbitMQ (Event-driven)
Search Service (Elasticsearch - Port 8083)
    ↓
API Gateway (Port 8080)
    ↓
Frontend / Requêtes client
```

---

## 🏗️ Services déployés

### 1. **Auth Service** (Port 8081)
Gère l'authentification et les tokens JWT
```bash
# Endpoint : POST /api/v1/auth/login
# Credentials crawler :
Email: crawler@yowyob.system
Password: crawler_secure_password_123
```

### 2. **API Gateway** (Port 8080)
Porte d'entrée sécurisée - vérifie les tokens
```bash
# Tous les appels passent par le Gateway
GET  http://localhost:8080/api/search?q=voiture
GET  http://localhost:8080/api/listings/{id}
```

### 3. **Listing Service** (Port 8082)
Base de données pour les annonces
```bash
# Endpoints
GET    /api/listings           # Toutes les annonces
POST   /api/listings           # Créer annonce
GET    /api/listings/{id}      # Détails annonce
PUT    /api/listings/{id}      # Mettre à jour
DELETE /api/listings/{id}      # Supprimer
```

### 4. **Search Service** (Port 8083) - Elasticsearch
Moteur de recherche intelligent
```bash
# Endpoints
GET    /api/search?q=voiture&type=VOITURE&city=Douala
GET    /api/search/{id}/details    # Détails d'une annonce recherchée
POST   /api/search/index           # Indexer un produit
```

### 5. **Crawler Service** (Port 8086)
Collecte les annonces toutes les minutes
```bash
# Fonctionnement automatique via @Scheduled(fixedRate = 60000)
# Scrape OLX Cameroun et Jumia
# Génère des données variées si sites indisponibles
```

---

## 🔍 Guide d'utilisation - Flux complet

### Étape 1 : S'authentifier
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Réponse (récupérez le token accessToken)
{
  "success": true,
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzUxMi...",
  "user": {
    "id": "uuid...",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

### Étape 2 : Chercher une annonce
```bash
curl -X GET "http://localhost:8080/api/search?q=voiture&city=Douala" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Réponse
{
  "success": true,
  "query": "voiture",
  "total": 3,
  "results": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Toyota Corolla 2018 [VOITURE]",
      "description": "État excellent, entretien régulier",
      "price": 2800000,
      "category": "VOITURE",
      "city": "Douala, Bonanjo",
      "rating": 0.0,
      "detailsUrl": "/api/search/123e4567-e89b-12d3-a456-426614174000/details"
    }
  ]
}
```

### Étape 3 : Cliquer sur un résultat pour voir les détails
```bash
curl -X GET "http://localhost:8080/api/search/123e4567-e89b-12d3-a456-426614174000/details" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Réponse (détails complets du produit)
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Toyota Corolla 2018",
  "description": "État excellent, entretien régulier, peu km",
  "price": 2800000,
  "category": "VOITURE",
  "type": "LISTING",
  "city": "Douala, Bonanjo",
  "rating": 0.0
}
```

### Étape 4 : Accéder au listing complet avec géolocalisation
```bash
# Si vous avez l'ID du listing (présent dans les résultats de search)
curl -X GET "http://localhost:8080/api/listings/{id}" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Réponse (avec coordonnées GPS)
{
  "id": "uuid...",
  "title": "Toyota Corolla 2018 [VOITURE]",
  "description": "État excellent, entretien régulier, peu km",
  "price": 2800000.0,
  "category": "VOITURE",
  "sellerId": "uuid...",
  "address": "Douala, Bonanjo",
  "latitude": 4.05123,
  "longitude": 9.7234,
  "status": "ACTIVE",
  "createdAt": "2026-01-15T20:30:00",
  "updatedAt": "2026-01-15T20:30:00"
}
```

---

## 📊 Flux de données

### Quand le Crawler s'exécute (toutes les minutes)

```
1. ScraperService.scrapeListings()
   ├─ Essaye de scraper OLX Cameroun
   ├─ Essaye de scraper Jumia Cameroun
   └─ Génère données diversifiées si aucun résultat

2. Authentification du Crawler
   └─ Token obtenu auprès de l'Auth Service

3. Envoi des annonces au Listing Service
   ├─ POST /api/listings
   └─ RabbitMQ envoie événement "CREATED"

4. Search Service reçoit l'événement RabbitMQ
   ├─ Indexe l'annonce dans Elasticsearch
   └─ Rend searchable via /api/search
```

---

## 🔧 Catégories d'annonces générées

Le Crawler crée des annonces variées dans ces catégories :

| Catégorie | Prix base | Exemples |
|-----------|-----------|----------|
| **VOITURE** | 2.5M FCFA | Toyota, Honda, BMW |
| **IMMOBILIER** | 15M FCFA | Villas, Appartements, Studios |
| **ELECTRONIQUE** | 500K FCFA | Téléphones, Laptops, TVs |
| **MEUBLES** | 300K FCFA | Canapés, Lits, Tables |
| **MOTO** | 1.5M FCFA | Motos, Scooters |

---

## 🗺️ Géolocalisation

Chaque annonce inclut :
- **address**: Localisation textuelle (ex: "Yaoundé, Odza")
- **latitude**: Coordonnée GPS nord-sud (3.8 - 4.05)
- **longitude**: Coordonnée GPS est-ouest (9.7 - 11.7)

### Villes couvertes
- Yaoundé (Odza, Bastos, Mfoundi)
- Douala (Bonanjo, Akwa, Deido)
- Buea (Molyko)
- Bamenda (Mankon)
- Bafoussam (Metchi)
- Limbe (Down Beach)

---

## 🧪 Tests rapides

### 1. Vérifier que le Crawler crée des annonces
```bash
curl http://localhost:8080/api/listings \
  -H "Authorization: Bearer TOKEN"
```

### 2. Rechercher par catégorie
```bash
# Toutes les voitures
curl "http://localhost:8080/api/search?q=voiture&type=VOITURE" \
  -H "Authorization: Bearer TOKEN"

# Immobilier à Yaoundé
curl "http://localhost:8080/api/search?q=villa&city=Yaoundé" \
  -H "Authorization: Bearer TOKEN"
```

### 3. Vérifier la santé des services
```bash
curl http://localhost:8081/api/v1/auth/health      # Auth
curl http://localhost:8082/api/listings/health      # Listing
curl http://localhost:8083/api/search/health        # Search
curl http://localhost:8086/api/crawler/health       # Crawler
```

---

## ⚠️ Notes importantes

1. **Crawler** s'exécute automatiquement toutes les minutes
2. **RabbitMQ** doit être en cours d'exécution pour la synchronisation
3. **Elasticsearch** doit être accessible pour les recherches
4. Les **tokens JWT** expirent après 1 heure
5. Les **prix varient** de ±20% à chaque scrape

---

## 🚀 Prochaines étapes

- [ ] Ajouter pagination aux résultats de recherche
- [ ] Implémenter filtres avancés (prix min/max, date)
- [ ] Ajouter système de notations
- [ ] Implémenter chat entre acheteur/vendeur
- [ ] Ajouter uploads d'images
- [ ] Mettre en cache les résultats populaires
