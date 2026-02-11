# 📡 API Endpoints - Référence complète

## 🔐 Authentication Service (Port 8081)

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response 200:
{
  "success": true,
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzUxMi...",
  "user": {
    "id": "uuid",
    "name": "User Name",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "Nouveau User",
  "email": "new@example.com",
  "password": "password123"
}

Response 201: Created + token
```

### Health
```http
GET /api/v1/auth/health

Response 200:
"Auth Service is running!"
```

---

## 📦 Listing Service (Port 8082)

### Create Listing
```http
POST /api/listings
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "title": "Toyota Corolla 2018",
  "description": "État excellent",
  "price": 2800000,
  "category": "VOITURE",
  "address": "Douala, Bonanjo",
  "latitude": 4.05,
  "longitude": 9.73
}

Response 201: Created
```

### Get All Listings
```http
GET /api/listings
Authorization: Bearer {TOKEN}

Response 200:
[
  {
    "id": "uuid",
    "title": "Toyota Corolla 2018",
    "price": 2800000,
    "category": "VOITURE",
    "address": "Douala, Bonanjo",
    "latitude": 4.05,
    "longitude": 9.73,
    "status": "ACTIVE",
    "createdAt": "2026-01-15T20:30:00"
  }
]
```

### Get Listing by ID
```http
GET /api/listings/{id}
Authorization: Bearer {TOKEN}

Response 200:
{
  "id": "uuid",
  "title": "Toyota Corolla 2018",
  "description": "État excellent",
  "price": 2800000,
  "category": "VOITURE",
  "sellerId": "uuid",
  "address": "Douala, Bonanjo",
  "latitude": 4.05,
  "longitude": 9.73,
  "status": "ACTIVE",
  "createdAt": "2026-01-15T20:30:00",
  "updatedAt": "2026-01-15T20:30:00"
}
```

### Update Listing
```http
PUT /api/listings/{id}
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "title": "Toyota Corolla 2018 - Updated",
  "price": 2700000
}

Response 200: Updated
```

### Delete Listing
```http
DELETE /api/listings/{id}
Authorization: Bearer {TOKEN}

Response 204: No Content
```

### Get Listings by Seller
```http
GET /api/listings/seller/{sellerId}
Authorization: Bearer {TOKEN}

Response 200: [array of listings]
```

---

## 🔍 Search Service (Port 8083)

### Search Products
```http
GET /api/search?q=voiture&type=VOITURE&city=Douala
Authorization: Bearer {TOKEN}

Parameters:
- q: keyword (string, optional)
- type: product type (string, optional)
- city: location (string, optional)

Response 200:
{
  "success": true,
  "query": "voiture",
  "total": 3,
  "results": [
    {
      "id": "uuid",
      "name": "Toyota Corolla 2018 [VOITURE]",
      "description": "État excellent",
      "price": 2800000,
      "type": "LISTING",
      "category": "VOITURE",
      "city": "Douala, Bonanjo",
      "rating": 0.0,
      "detailsUrl": "/api/search/uuid/details"
    }
  ]
}
```

### Get Product Details
```http
GET /api/search/{id}/details
Authorization: Bearer {TOKEN}

Response 200:
{
  "id": "uuid",
  "name": "Toyota Corolla 2018",
  "description": "État excellent, entretien régulier",
  "price": 2800000,
  "category": "VOITURE",
  "type": "LISTING",
  "city": "Douala, Bonanjo",
  "rating": 0.0
}
```

### Index Product
```http
POST /api/search/index
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "name": "Nouveau Produit",
  "description": "Description complète",
  "price": 500000,
  "category": "ELECTRONIQUE",
  "city": "Yaoundé, Odza",
  "type": "LISTING"
}

Response 201: Indexed
```

### Health
```http
GET /api/search/health

Response 200:
"Search Service with Elasticsearch!"
```

---

## 🌐 API Gateway (Port 8080)

**Tous les endpoints passent par le Gateway avec authentification JWT**

### Login (Gateway)
```http
POST /api/v1/auth/login
```
*(Forwardé à Auth Service)*

### Search (Gateway)
```http
GET /api/search?q=voiture
Authorization: Bearer {TOKEN}
```
*(Forwardé à Search Service)*

### Listing (Gateway)
```http
GET /api/listings/{id}
Authorization: Bearer {TOKEN}
```
*(Forwardé à Listing Service)*

---

## 🤖 Crawler Service (Port 8086)

### Health
```http
GET /api/crawler/health

Response 200:
"Crawler Service is running!"
```

**Note** : Le Crawler s'exécute automatiquement sans endpoint HTTP  
Exécution : Toutes les minutes (@Scheduled(fixedRate = 60000))

---

## 📊 Filtres & Paramètres de recherche

### Catégories supportées
- VOITURE
- IMMOBILIER
- ELECTRONIQUE
- MEUBLES
- MOTO
- DIVERS

### Villes supportées
- Yaoundé, Odza
- Yaoundé, Bastos
- Yaoundé, Mfoundi
- Douala, Bonanjo
- Douala, Akwa
- Douala, Deido
- Buea, Molyko
- Bamenda, Mankon
- Bafoussam, Metchi
- Limbe, Down Beach

### Types de produits
- LISTING (par défaut)

---

## 🧪 Exemples de requêtes complètes

### Exemple 1 : Rechercher toutes les voitures à Douala
```bash
curl "http://localhost:8080/api/search?q=voiture&type=VOITURE&city=Douala" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMi..."
```

### Exemple 2 : Voir les détails d'une annonce trouvée
```bash
curl "http://localhost:8080/api/search/123e4567-e89b-12d3-a456-426614174000/details" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMi..."
```

### Exemple 3 : Voir localisation complète avec GPS
```bash
curl "http://localhost:8080/api/listings/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMi..."
```

### Exemple 4 : Créer une nouvelle annonce
```bash
curl -X POST "http://localhost:8080/api/listings" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMi..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Honda Civic 2020",
    "description": "Automatique, très bon état",
    "price": 3500000,
    "category": "VOITURE",
    "address": "Yaoundé, Bastos",
    "latitude": 3.85,
    "longitude": 11.52
  }'
```

---

## ⚠️ Codes d'erreur

| Code | Signification |
|------|---------------|
| 200 | ✓ OK |
| 201 | ✓ Créé |
| 204 | ✓ Supprimé/No Content |
| 400 | ✗ Requête invalide |
| 401 | ✗ Non authentifié (token manquant/invalide) |
| 403 | ✗ Non autorisé |
| 404 | ✗ Non trouvé |
| 500 | ✗ Erreur serveur |

---

## 🔐 Headers requis

```
Authorization: Bearer {accessToken}
Content-Type: application/json (pour POST/PUT)
```

---

## ⏱️ Timings

- **Auth Token** : Valide 1 heure
- **Crawler** : S'exécute toutes les minutes
- **Search Index** : Mis à jour en temps réel via RabbitMQ
- **API Response** : < 200ms pour recherches

