# Architecture YOWYOB et Guide de Testing

## 1. Vue d'ensemble du système

```
┌─────────────────────────────────────────────────────────────────┐
│                        UTILISATEUR (Frontend)                    │
│                     (React, Angular, Web)                        │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                    HTTP Requests (Port 8080)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│              API GATEWAY (Port 8080) - Spring Cloud              │
│  ┌─ Routage des requêtes                                         │
│  ├─ Validation JWT (Authentification)                            │
│  └─ Équilibrage de charge entre services                         │
└────┬───────────┬────────────────┬──────────────┬─────────────────┘
     │           │                │              │
     │           │                │              │
  Port 8081   Port 8082        Port 8083      Port 8086
     │           │                │              │
     ▼           ▼                ▼              ▼
┌────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────────┐
│ AUTH   │ │ SEARCH   │ │   LISTING    │ │   CRAWLER    │
│Service │ │ Service  │ │   Service    │ │   Service    │
│        │ │          │ │              │ │              │
│ -Login │ │ -Search  │ │ -Store Data  │ │ -Web Scrape  │
│ -Token │ │ -Index   │ │ -DB queries  │ │ -Mock data   │
│ -JWT   │ │ -Details │ │ -Event emit  │ │ -Send events │
└────────┘ └──────────┘ └──────────────┘ └──────────────┘
     │           │                │              │
     │           │                │              │
     │           ▼                │              │
     │        ┌─────────────┐     │              │
     │        │ ELASTICSEARCH │   │              │
     │        │ (Port 9200)  │   │              │
     │        │              │   │              │
     │        │ -Indexation  │   │              │
     │        │ -Full-text   │   │              │
     │        │  search      │   │              │
     │        └──────────────┘   │              │
     │                           │              │
     └─ MySQL DB ────────────────┼──────────────┘
        (Port 3306)              │
        ┌─────────────────────────┘
        │
        ├─ Users table
        ├─ Listings table
        ├─ Categories table
        ├─ RabbitMQ (Port 5672)
        │  └─ Event queue
        └─ Events
```

## 2. Flux complet d'une recherche

### Étape 1: Utilisateur tape "voiture" dans la barre de recherche

```
Frontend → GET /api/search?q=voiture
```

### Étape 2: API Gateway reçoit et route

```
API Gateway (8080)
  ↓
  ├─ Valide le JWT token
  ├─ Identifie la route: /api/search → SEARCH-SERVICE
  └─ Envoie requête vers: http://localhost:8083/api/search?q=voiture
```

### Étape 3: Search Service traite la requête

**Fichier**: `search-service/src/main/java/com/yowyob/search/controller/SearchController.java`

```java
@GetMapping("/api/search")
public Mono<SearchResponse> search(
    @RequestParam(required = false) String q,
    @RequestParam(required = false) String type,
    @RequestParam(required = false) String city
) {
    // Appelle SearchService pour requête Elasticsearch
    return searchService.search(q, type, city);
}
```

### Étape 4: Search Service requête Elasticsearch

**Fichier**: `search-service/src/main/java/com/yowyob/search/service/SearchService.java`

```
SearchService.search("voiture", null, null)
  ↓
  Construit une requête Elasticsearch:
  ┌─────────────────────────────────────────┐
  │ Query: "voiture"                         │
  │ Champs cherchés:                         │
  │   - title (matching fort)                │
  │   - description (matching faible)        │
  │   - category (exact match)               │
  └─────────────────────────────────────────┘
  ↓
  Elasticsearch (Port 9200)
  ↓
  Retourne tous les documents matchant "voiture"
```

### Étape 5: Résultats retournés au Frontend

```
Search Results:
{
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Toyota Corolla 2018 - Excellent état",
      "description": "État excellent, entretien régulier, 45000 km",
      "price": 2890000,
      "type": "VOITURE",
      "category": "VOITURE",
      "city": "Yaoundé, Odza",
      "rating": 4.5,
      "detailsUrl": "/api/search/550e8400-e29b-41d4-a716-446655440001/details"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "Honda Civic Automatique 2019",
      "description": "Climatisation fonctionne, révision à jour",
      "price": 3100000,
      "type": "VOITURE",
      "category": "VOITURE",
      "city": "Douala, Akwa",
      "rating": 4.7,
      "detailsUrl": "/api/search/550e8400-e29b-41d4-a716-446655440002/details"
    }
  ]
}
```

### Étape 6: Utilisateur clique sur un résultat

```
Frontend → GET /api/search/{id}/details
           GET /api/search/550e8400-e29b-41d4-a716-446655440001/details
```

### Étape 7: Search Service retourne les détails + géolocalisation

```
GET /api/search/{id}/details
  ↓
SearchService.getProductById(id)
  ↓
Elasticsearch.get(id)
  ↓
Retourne:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla 2018 - Excellent état",
  "description": "État excellent, entretien régulier, 45000 km",
  "price": 2890000,
  "category": "VOITURE",
  "address": "Yaoundé, Odza",
  "latitude": 3.852,
  "longitude": 11.512,
  "sellerId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 4.5
}
```

Frontend affiche la carte Google Maps avec la géolocalisation !

## 3. Rôle du Crawler Service

### Mission principale
**Remplir la base de données et Elasticsearch avec des annonces.**

### Comment ça marche:

**Fichier**: `crawler-service/src/main/java/com/yowyob/crawler/service/ScraperService.java`

#### A. Authentification (Toutes les 50 minutes)

```java
// À la démarrage, le crawler demande un JWT token à Auth Service
POST http://localhost:8081/api/v1/auth/login
Body: {
  "email": "crawler@yowyob.system",
  "password": "crawler_secure_password_123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}

// AuthTokenService cache ce token et le réutilise
// Si expiré (après 50 mins), demande un nouveau token automatiquement
```

#### B. Scraping toutes les 60 secondes

```java
@Scheduled(fixedDelay = 60000) // Toutes les 60 secondes
private void scrapeListings() {
    try {
        // 1. Essaie de scraper OLX Cameroun
        List<ListingDto> listings = scrapeOLXCameroun();
        
        if (listings.isEmpty()) {
            // 2. Essaie de scraper Jumia Cameroun
            listings = scrapeJumiaCameroun();
        }
        
        if (listings.isEmpty()) {
            // 3. Fallback: génère des données mock (5 annonces par catégorie)
            listings = generateMockListings();
        }
        
        // 4. Envoie au Listing Service
        for (ListingDto listing : listings) {
            sendToListingService(listing);
        }
    } catch (Exception e) {
        log.error("Scraping error", e);
    }
}
```

#### C. Catégories générées (FIXES - Corrigées):

| Catégorie    | Exemples de titres              | Prix min  | Prix max  |
|-----|---|---|---|
| VOITURE    | Toyota Corolla, Honda Civic     | 2.0M FCFA | 3.5M FCFA |
| IMMOBILIER | Studio, Appartement, Villa      | 12M FCFA  | 18M FCFA  |
| ELECTRONIQUE | iPhone, Samsung, Xiaomi       | 400K FCFA | 600K FCFA |
| MEUBLES    | Canapé, Table, Lit              | 240K FCFA | 360K FCFA |
| MOTO       | Suzuki GSX, Yamaha YZF         | 1.2M FCFA | 1.8M FCFA |

#### D. Envoie au Listing Service

```java
private void sendToListingService(ListingDto listing) {
    String token = authTokenService.getToken(); // JWT token
    
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<ListingDto> request = new HttpEntity<>(listing, headers);
    
    // POST vers Listing Service
    restTemplate.postForEntity(
        "http://localhost:8082/api/listings",
        request,
        String.class
    );
}
```

## 4. Rôle du Listing Service

### Mission principale
**Persister les annonces en BD et publier les événements pour Elasticsearch.**

**Fichier**: `listing-service/src/main/java/com/yowyob/listing/controller/ListingController.java`

```java
@PostMapping("/api/listings")
public ResponseEntity<ListingDto> createListing(@RequestBody ListingDto listing) {
    // 1. Valide les données
    // 2. Sauvegarde en MySQL
    ListingEntity savedEntity = listingRepository.save(toEntity(listing));
    
    // 3. Publie un événement RabbitMQ
    rabbitTemplate.convertAndSend(
        "listings.exchange",
        "listing.created",
        savedEntity
    );
    
    // 4. Retourne la réponse
    return ResponseEntity.created(...).body(toDto(savedEntity));
}
```

### Flux de l'événement RabbitMQ:

```
Listing Service
  ↓ (publie "ListingCreatedEvent")
RabbitMQ Queue
  ↓ (route message)
Search Service (écoute)
  ↓ (reçoit le message)
Search Service indexe en Elasticsearch
  ↓
Elasticsearch sauvegarde le document
  ↓
L'annonce est maintenant cherchable !
```

## 5. Interactions avec la Base de Données MySQL

### Schema (Vue simplifiée)

```sql
-- Table USERS (créée par Auth Service)
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255),
  role ENUM('ADMIN', 'USER', 'CRAWLER'),
  created_at TIMESTAMP
);

-- Table LISTINGS (créée par Listing Service)
CREATE TABLE listings (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12,2),
  category VARCHAR(50),
  address VARCHAR(255),
  latitude DECIMAL(10,8),
  longitude DECIMAL(11,8),
  seller_id VARCHAR(36) FOREIGN KEY,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Index pour optimiser les recherches
CREATE INDEX idx_category ON listings(category);
CREATE INDEX idx_price ON listings(price);
CREATE INDEX idx_seller ON listings(seller_id);
```

### Requêtes MySQL typiques:

```sql
-- 1. Listing Service: Insérer une annonce
INSERT INTO listings (id, title, category, price, address, seller_id)
VALUES ('550e8400...', 'Toyota Corolla', 'VOITURE', 2890000, 'Yaoundé', '123e4567...');

-- 2. Search Service: Chercher par catégorie (utilisé si Elasticsearch est down)
SELECT * FROM listings 
WHERE category = 'VOITURE' 
ORDER BY created_at DESC 
LIMIT 50;

-- 3. Crawler: Vérifier les annonces créées aujourd'hui
SELECT COUNT(*) FROM listings 
WHERE DATE(created_at) = CURDATE();
```

## 6. Tous les Endpoints API

### 6.1 AUTH SERVICE (Port 8081)

#### Login
```
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

#### Register
```
POST http://localhost:8081/api/v1/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securepass123"
}

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "newuser@example.com"
}
```

### 6.2 SEARCH SERVICE (Port 8083)

#### Recherche globale
```
GET http://localhost:8083/api/search?q=voiture&type=VOITURE&city=Yaoundé

Query Parameters:
- q: Texte à chercher (optionnel)
- type: Catégorie (VOITURE, IMMOBILIER, ELECTRONIQUE, MEUBLES, MOTO) (optionnel)
- city: Ville (optionnel)

Response:
{
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Toyota Corolla 2018",
      "description": "État excellent...",
      "price": 2890000,
      "type": "VOITURE",
      "city": "Yaoundé, Odza",
      "detailsUrl": "/api/search/550e8400-e29b-41d4-a716-446655440001/details"
    }
  ],
  "totalCount": 42
}
```

#### Détails d'une annonce
```
GET http://localhost:8083/api/search/{id}/details
GET http://localhost:8083/api/search/550e8400-e29b-41d4-a716-446655440001/details

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla 2018 - Excellent état",
  "description": "État excellent, entretien régulier, 45000 km",
  "price": 2890000,
  "category": "VOITURE",
  "address": "Yaoundé, Odza",
  "latitude": 3.852,
  "longitude": 11.512,
  "sellerId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Index une annonce (Interne)
```
POST http://localhost:8083/api/search/index
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla",
  "description": "État excellent",
  "price": 2890000,
  "category": "VOITURE"
}

Response: 201 Created
```

### 6.3 LISTING SERVICE (Port 8082)

#### Créer une annonce
```
POST http://localhost:8082/api/listings
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Toyota Corolla 2018",
  "description": "État excellent, entretien régulier",
  "price": 2890000,
  "category": "VOITURE",
  "address": "Yaoundé, Odza",
  "latitude": 3.852,
  "longitude": 11.512,
  "sellerId": "550e8400-e29b-41d4-a716-446655440000"
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla 2018",
  ...
}
```

#### Récupérer une annonce
```
GET http://localhost:8082/api/listings/{id}
Authorization: Bearer {jwt_token}

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla 2018",
  ...
}
```

#### Lister toutes les annonces
```
GET http://localhost:8082/api/listings?page=0&size=10
Authorization: Bearer {jwt_token}

Response:
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 15,
  "currentPage": 0
}
```

### 6.4 API GATEWAY (Port 8080)

L'API Gateway route toutes les requêtes:

```
GET  /api/search      → Search Service (8083)
GET  /api/search/{id}/details → Search Service (8083)
POST /api/listings    → Listing Service (8082)
GET  /api/listings/{id} → Listing Service (8082)
POST /api/v1/auth/login → Auth Service (8081)
POST /api/v1/auth/register → Auth Service (8081)
```

**Toutes les requêtes doivent passer par le Gateway !**

```
Frontend: GET http://localhost:8080/api/search?q=voiture
  ↓
API Gateway valide JWT
  ↓
Routes vers: http://localhost:8083/api/search?q=voiture
  ↓
Retourne réponse au Frontend
```

## 7. Comment tester avec Postman

### 7.1 Installation de Postman

1. Télécharge Postman: https://www.postman.com/downloads/
2. Crée un compte gratuit
3. Ouvre Postman

### 7.2 Configuration de l'environnement

Dans Postman:
1. Clique sur **Environments** (en haut à gauche)
2. Clique sur **Create New Environment**
3. Nomme-le: `YOWYOB Local`
4. Ajoute ces variables:

```
Variable Name          | Type   | Initial Value
-------------------------------------------
base_url              | string | http://localhost:8080
auth_token            | string | (vide)
user_id               | string | 550e8400-e29b-41d4-a716-446655440000
```

5. Clique **Save**

### 7.3 Collection Postman (Importer/Créer)

Crée une nouvelle collection: **YOWYOB API**

#### Requête 1: Login (obtenir JWT)

```
Request Name: Login
Method: POST
URL: {{base_url}}/api/v1/auth/login
Headers:
  Content-Type: application/json

Body (raw - JSON):
{
  "email": "user@example.com",
  "password": "password123"
}

Tests (ajoute dans l'onglet Tests):
var jsonData = pm.response.json();
pm.environment.set("auth_token", jsonData.token);
```

**Pour tester:**
1. Sélectionne l'environnement `YOWYOB Local`
2. Clique **Send**
3. Le token est automatiquement sauvegardé dans `auth_token`

#### Requête 2: Rechercher les voitures

```
Request Name: Search Voitures
Method: GET
URL: {{base_url}}/api/search?q=voiture&type=VOITURE
Headers:
  Authorization: Bearer {{auth_token}}
  Content-Type: application/json

Clique: Send

Response:
{
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Toyota Corolla 2018",
      "price": 2890000,
      "category": "VOITURE",
      "detailsUrl": "/api/search/550e8400-e29b-41d4-a716-446655440001/details"
    }
  ]
}
```

#### Requête 3: Voir les détails d'une annonce

```
Request Name: Get Listing Details
Method: GET
URL: {{base_url}}/api/search/550e8400-e29b-41d4-a716-446655440001/details
Headers:
  Authorization: Bearer {{auth_token}}

Clique: Send

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Toyota Corolla 2018 - Excellent état",
  "description": "État excellent, entretien régulier, 45000 km",
  "price": 2890000,
  "category": "VOITURE",
  "address": "Yaoundé, Odza",
  "latitude": 3.852,
  "longitude": 11.512
}
```

#### Requête 4: Chercher par catégorie (Immobilier)

```
Request Name: Search Immobilier
Method: GET
URL: {{base_url}}/api/search?type=IMMOBILIER
Headers:
  Authorization: Bearer {{auth_token}}

Clique: Send
```

#### Requête 5: Chercher par ville

```
Request Name: Search Douala
Method: GET
URL: {{base_url}}/api/search?city=Douala
Headers:
  Authorization: Bearer {{auth_token}}

Clique: Send
```

#### Requête 6: Créer une annonce

```
Request Name: Create Listing
Method: POST
URL: {{base_url}}/api/listings
Headers:
  Authorization: Bearer {{auth_token}}
  Content-Type: application/json

Body (raw - JSON):
{
  "title": "MacBook Pro 2023 - Neuf",
  "description": "Neuf jamais utilisé, boîte scellée",
  "price": 850000,
  "category": "ELECTRONIQUE",
  "address": "Yaoundé, Bastos",
  "latitude": 3.86,
  "longitude": 11.5,
  "sellerId": "550e8400-e29b-41d4-a716-446655440000"
}

Clique: Send

Response: 201 Created
```

### 7.4 Ordre recommandé pour tester

1. **Login** - Obtient le JWT token
2. **Search Voitures** - Cherche les voitures
3. **Get Listing Details** - Voir les détails d'une voiture
4. **Search Immobilier** - Cherche les appartements
5. **Search Douala** - Cherche à Douala
6. **Create Listing** - Crée une nouvelle annonce
7. **Search [nouveau titre]** - Trouve ta nouvelle annonce

### 7.5 Debugging dans Postman

Si tu as une erreur:

```
500 Error - Connection Refused
├─ Vérifier que les services tournent: docker ps
├─ Vérifier le port: lsof -i :8080

401 Unauthorized
├─ Vérifier le JWT token: {{auth_token}}
├─ Login d'abord pour obtenir un nouveau token

400 Bad Request
├─ Vérifier le JSON format (pas de commentaires)
├─ Vérifier les champs obligatoires
```

## 8. Points clés à retenir

### Flux de recherche complet:

```
Utilisateur tape "voiture" 
  ↓
Frontend → API Gateway (8080)
  ↓
API Gateway valide JWT
  ↓
Routes vers Search Service (8083)
  ↓
Search Service query Elasticsearch
  ↓
Elasticsearch retourne résultats
  ↓
Frontend affiche les 42 voitures trouvées
```

### Flux de création d'annonce:

```
Utilisateur remplit le formulaire
  ↓
Frontend → API Gateway (8080)
  ↓
API Gateway → Listing Service (8082)
  ↓
Listing Service sauvegarde en MySQL
  ↓
Listing Service publie l'événement RabbitMQ
  ↓
Search Service reçoit l'événement
  ↓
Search Service index en Elasticsearch
  ↓
L'annonce est maintenant cherchable !
```

### Flux du Crawler:

```
Toutes les 60 secondes
  ↓
Crawler demande JWT token
  ↓
Crawler scrape OLX ou Jumia (ou génère données mock)
  ↓
Crawler envoie 5 annonces au Listing Service avec JWT
  ↓
Listing Service sauvegarde en MySQL
  ↓
RabbitMQ publie les événements
  ↓
Search Service les index
  ↓
Les utilisateurs les trouvent via recherche !
```

## 9. Dépannage rapide

| Problème | Solution |
|----------|----------|
| Port 8080 déjà utilisé | `lsof -i :8080` puis `kill -9 <PID>` |
| Search Service ne répond pas | Vérifier: `docker ps` (MySQL, RabbitMQ, ES) |
| JWT token expiré | Refaire Login |
| Catégories mélangées | ✅ FIXÉ - Redémarrer le crawler |
| Elasticsearch down | Données quand même sauvegardées en MySQL |
| RabbitMQ down | Crawler peut quand même envoyer au Listing Service |

## 10. Fichiers clés du projet

```
yowyob-backend_qui_marche/
├── auth-service/
│   └── src/main/java/com/yowyob/auth/
│       ├── controller/AuthController.java
│       ├── service/AuthService.java
│       └── config/DataInitializer.java
├── api-gateway/
│   └── src/main/java/com/yowyob/gateway/
│       └── config/GatewayConfig.java
├── search-service/
│   └── src/main/java/com/yowyob/search/
│       ├── controller/SearchController.java
│       ├── service/SearchService.java
│       └── repository/ProductSearchRepository.java
├── listing-service/
│   └── src/main/java/com/yowyob/listing/
│       ├── controller/ListingController.java
│       ├── service/ListingService.java
│       └── entity/ListingEntity.java
├── crawler-service/
│   └── src/main/java/com/yowyob/crawler/
│       ├── service/ScraperService.java (RÉPARÉ)
│       ├── service/AuthTokenService.java
│       └── dto/ListingDto.java
└── docker-compose.yml
```

---

**Créé**: 16 janvier 2026  
**Dernière modification**: Avec fix des catégories
