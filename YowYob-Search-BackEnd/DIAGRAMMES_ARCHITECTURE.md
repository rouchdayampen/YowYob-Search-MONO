# 🏗️ Diagrammes d'Architecture YOWYOB

## 1. Architecture Globale

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          UTILISATEUR FINAL                               │
│               (Web Frontend, Mobile, Desktop App)                        │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                    HTTP/HTTPS Request (Port 8080)
                                  │
                                  ▼
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃              🌐 API GATEWAY (Spring Cloud Gateway)                     ┃
┃                          Port 8080                                      ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃  Responsabilités:                                                       ┃
┃  ✓ Routage des requêtes vers les microservices                         ┃
┃  ✓ Validation du JWT token                                             ┃
┃  ✓ Équilibrage de charge                                               ┃
┃  ✓ Gestion des erreurs 401/403                                         ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
   │                 │                  │                  │
   │ /api/auth       │ /api/search     │ /api/listings   │ /api/crawler
   │                 │                  │                  │
   ▼                 ▼                  ▼                  ▼
┌─────────┐    ┌──────────┐      ┌─────────────┐    ┌─────────────┐
│   AUTH  │    │  SEARCH  │      │   LISTING   │    │   CRAWLER   │
│ SERVICE │    │ SERVICE  │      │   SERVICE   │    │   SERVICE   │
│ Port    │    │  Port    │      │   Port      │    │   Port      │
│ 8081    │    │ 8083     │      │  8082       │    │  8086       │
└────┬────┘    └────┬─────┘      └─────┬───────┘    └──────┬──────┘
     │              │                   │                  │
     │              │                   │                  │
     │    ┌─────────┴───────────────────┴──────┐           │
     │    │                                    │           │
     │    ▼                                    ▼           │
     │  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │           │
     │  ┃  ELASTICSEARCH (Port 9200)      ┃   │           │
     │  ┃  ───────────────────────────    ┃   │           │
     │  ┃  • Indexation des annonces      ┃   │           │
     │  ┃  • Full-text search              ┃   │           │
     │  ┃  • Requêtes par catégorie        ┃   │           │
     │  ┃  • Requêtes par localisation     ┃   │           │
     │  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │           │
     │    △                                  │           │
     │    │ (index + query)                 │           │
     │    │                                  │           │
     └────┼──────────────┬───────────────────┘           │
          │              │                              │
          ▼              ▼                              │
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓             │
        ┃  MySQL DATABASE (Port 3306)     ┃             │
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫             │
        ┃ • users (Auth Service)          ┃             │
        ┃ • listings (Listing Service)    ┃             │
        ┃ • categories (metadata)         ┃             │
        ┃ • reviews (future)              ┃             │
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛             │
          △                                              │
          │ (Crawler → Listing → MySQL)                 │
          │                                              │
          └──────────────────────────────────────────────┘
                          │
                          ▼
          ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
          ┃ RABBITMQ MESSAGE QUEUE         ┃
          ┃ (Port 5672, Port 15672)        ┃
          ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
          ┃ Events:                         ┃
          ┃ • listing.created              ┃
          ┃ • listing.updated              ┃
          ┃ • listing.deleted              ┃
          ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
               ▲                    │
               │ (publie)      (consomme)
               │                    │
        Listing Service    Search Service
                          (indexe en ES)
```

## 2. Flux de Recherche Détaillé

```
┌──────────────────────────────────┐
│ Utilisateur tape "voiture"       │
└────────┬─────────────────────────┘
         │
         │ Frontend émet:
         │ GET /api/search?q=voiture
         │
         ▼
┌──────────────────────────────────────────┐
│ API GATEWAY (Port 8080)                  │
│ ┌──────────────────────────────────────┐ │
│ │ 1. Reçoit la requête                 │ │
│ │ 2. Valide le JWT token               │ │
│ │ 3. Identifie la route:               │ │
│ │    /api/search → SEARCH-SERVICE      │ │
│ │ 4. Route vers http://localhost:8083  │ │
│ └──────────────────────────────────────┘ │
└──────────┬───────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│ SEARCH SERVICE (Port 8083)               │
│ ┌──────────────────────────────────────┐ │
│ │ SearchController.search(q="voiture") │ │
│ │         ↓                             │ │
│ │ SearchService.search()                │ │
│ │  ├─ Crée la query Elasticsearch      │ │
│ │  ├─ Cherche dans: title, description│ │
│ │  └─ Filtre: category, city           │ │
│ └──────────────────────────────────────┘ │
└──────────┬───────────────────────────────┘
           │
           │ Query avec Elasticsearch API
           │ {
           │   "query": {
           │     "multi_match": {
           │       "query": "voiture",
           │       "fields": ["title", "description"]
           │     }
           │   }
           │ }
           │
           ▼
┌──────────────────────────────────────────┐
│ ELASTICSEARCH (Port 9200)                │
│ ┌──────────────────────────────────────┐ │
│ │ Index: "listings"                    │ │
│ │                                      │ │
│ │ Documents cherchant "voiture":       │ │
│ │ ├─ "Toyota Corolla 2018" ✓           │ │
│ │ ├─ "Honda Civic Automatique" ✓       │ │
│ │ ├─ "Peugeot 307 XS" ✓                │ │
│ │ └─ "Renault Logan Gris" ✓            │ │
│ │                                      │ │
│ │ Total: 42 résultats trouvés          │ │
│ └──────────────────────────────────────┘ │
└──────────┬───────────────────────────────┘
           │
           │ Résultats:
           │ [
           │   {
           │     "id": "550e8400...",
           │     "title": "Toyota Corolla 2018",
           │     "price": 2890000,
           │     "category": "VOITURE",
           │     "detailsUrl": "/api/search/550e8400.../details"
           │   },
           │   {...},
           │   {...}
           │ ]
           │
           ▼
┌──────────────────────────────────────────┐
│ API GATEWAY (Port 8080)                  │
│ └─ Retourne les résultats au frontend   │
└──────────┬───────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│ Frontend affiche:                        │
│ ┌──────────────────────────────────────┐ │
│ │ Résultats (42 voitures trouvées)    │ │
│ ├──────────────────────────────────────┤ │
│ │ 1. Toyota Corolla 2018              │ │
│ │    Prix: 2.890.000 FCFA             │ │
│ │    [Voir détails] ← Clique ici      │ │
│ ├──────────────────────────────────────┤ │
│ │ 2. Honda Civic Automatique          │ │
│ │    Prix: 3.100.000 FCFA             │ │
│ │    [Voir détails]                   │ │
│ └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

## 3. Flux Complet d'une Annonce Créée

```
    FRONTEND
       │
       │ Utilisateur remplit le formulaire:
       │ - Titre: "MacBook Pro 2023"
       │ - Prix: 850.000 FCFA
       │ - Catégorie: ELECTRONIQUE
       │
       ▼
┌──────────────────────────────────────┐
│ Frontend envoie:                     │
│ POST /api/listings                   │
│ Body: {...listing data...}           │
└────────┬────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────┐
│ API GATEWAY (Port 8080)              │
│ • Valide JWT                         │
│ • Route vers port 8082               │
└────────┬────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────┐
│ LISTING SERVICE (Port 8082)                  │
│                                              │
│ ListingController.createListing()            │
│  ├─ Valide le DTO                           │
│  ├─ Génère un UUID                          │
│  │ (550e8400-e29b-41d4-a716-446655440099)   │
│  │                                          │
│  ├─ INSERT en MySQL                         │
│  │ ```sql                                   │
│  │ INSERT INTO listings                     │
│  │ (id, title, price, category)             │
│  │ VALUES (550e8400..., "MacBook Pro", ...) │
│  │ ```                                      │
│  │                                          │
│  └─ PUBLIE l'événement RabbitMQ             │
│    ```json                                  │
│    {                                        │
│      "event": "listing.created",            │
│      "id": "550e8400...",                   │
│      "title": "MacBook Pro 2023"            │
│    }                                        │
│    ```                                      │
└─────────┬──────────────────────────────────┘
          │
          ▼ MySQL saved ✓
    ┌─────────────────┐
    │     MySQL       │
    │     (persiste)  │
    └─────────────────┘
          │
          │ RabbitMQ queue
          ▼
┌──────────────────────────────────────────────┐
│ SEARCH SERVICE (écoute le message)           │
│                                              │
│ SearchService.onListingCreated()             │
│  ├─ Reçoit: listing.created event            │
│  ├─ Récupère les données complètes           │
│  │                                          │
│  └─ INDEX en Elasticsearch                  │
│    ```json                                  │
│    POST /listings/_doc/550e8400...          │
│    {                                        │
│      "title": "MacBook Pro 2023",           │
│      "description": "Neuf jamais utilisé",  │
│      "price": 850000,                       │
│      "category": "ELECTRONIQUE"              │
│    }                                        │
│    ```                                      │
└─────────┬──────────────────────────────────┘
          │
          ▼ Indexed ✓
    ┌─────────────────────┐
    │ ELASTICSEARCH       │
    │ (cherchable)        │
    └─────────────────────┘
          │
          │ Utilisateur cherche:
          │ GET /api/search?type=ELECTRONIQUE
          │
          ▼
    ┌─────────────────────────────────┐
    │ RÉSULTAT: MacBook Pro 2023      │
    │ Prix: 850.000 FCFA              │
    │ État: Nouveau                   │
    │ [Voir détails]                  │
    └─────────────────────────────────┘
```

## 4. Flux du Crawler Service

```
┌────────────────────────────────────────┐
│ Scheduler: Toutes les 60 secondes      │
│ @Scheduled(fixedDelay = 60000)        │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ 1. AUTHENTIFICATION                    │
│                                        │
│ AuthTokenService.getToken()            │
│  ├─ Vérifie cache (token valide?)     │
│  │  NON → authentifie le crawler      │
│  │                                    │
│  └─ POST /api/v1/auth/login           │
│    Body:                              │
│    {                                  │
│      "email": "crawler@yowyob.system",│
│      "password": "..."                │
│    }                                  │
│                                        │
│    Response:                          │
│    {                                  │
│      "token": "eyJhbGci...",          │
│      "expiresIn": 3600                │
│    }                                  │
└────────┬───────────────────────────────┘
         │ Token obtenu ✓
         │
         ▼
┌────────────────────────────────────────┐
│ 2. SCRAPING                            │
│                                        │
│ scrapeListings()                       │
│  │                                    │
│  ├─ TRY: scrapeOLXCameroun()           │
│  │  └─ GET https://cm.olx.com/        │
│  │     └─ Parse HTML (Jsoup)          │
│  │        └─ 5s timeout               │
│  │                                    │
│  ├─ If empty → TRY: scrapeJumia()     │
│  │  └─ GET https://www.jumia.cm/      │
│  │     └─ Parse HTML                  │
│  │                                    │
│  └─ If still empty → generateMock()   │
│     ├─ 5 annonces VOITURE             │
│     ├─ 5 annonces IMMOBILIER          │
│     ├─ 5 annonces ELECTRONIQUE        │
│     ├─ 5 annonces MEUBLES             │
│     └─ 5 annonces MOTO                │
│                                        │
│     Résultat: 25 annonces générées    │
└────────┬───────────────────────────────┘
         │ Annonces collectées
         │
         ▼
┌────────────────────────────────────────┐
│ 3. ENVOI AU LISTING SERVICE            │
│                                        │
│ for each listing:                     │
│  └─ POST /api/listings                │
│     Headers:                          │
│       Authorization: Bearer {token}   │
│     Body: ListingDto {...}            │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ 4. WORKFLOW (voir flux #3)             │
│                                        │
│ ├─ Listing Service sauvegarde en MySQL│
│ ├─ Publie l'événement RabbitMQ        │
│ ├─ Search Service reçoit l'événement  │
│ ├─ Search Service indexe en ES        │
│ └─ L'annonce est cherchable !         │
└────────┬───────────────────────────────┘
         │
         └─ Recommence dans 60 secondes
            (Loop ∞)
```

## 5. État des Services

```
SERVICE            | PORT  | STATUT      | RÔLE
────────────────────────────────────────────────────────────────
Auth Service       | 8081  | ✓ Running   | Génère JWT tokens
API Gateway        | 8080  | ✓ Running   | Route requests
Search Service     | 8083  | ✓ Running   | Cherche dans ES
Listing Service    | 8082  | ✓ Running   | Persiste en MySQL
Crawler Service    | 8086  | ✓ Running   | Remplit la BD
────────────────────────────────────────────────────────────────
Elasticsearch      | 9200  | ✓ Running   | Index & search
RabbitMQ           | 5672  | ✓ Running   | Message queue
MySQL              | 3306  | ✓ Running   | Persistence
────────────────────────────────────────────────────────────────

Pour vérifier:
$ docker ps
$ ps aux | grep java
$ lsof -i :8080
$ curl http://localhost:9200
$ docker exec elasticsearch curl localhost:9200
```

## 6. Boucle Complète: Utilisateur → Crawler → BD → Recherche

```
                                      UTILISATEUR
                                           │
                      JOUR 1: 10:00 UTC   │
                                           │
                      ┌─────────────────────┴──────────┐
                      │                                │
                      ▼                                ▼
            ┌──────────────────┐        ┌──────────────────────┐
            │ Recherche        │        │ Crawler s'exécute    │
            │ GET /api/search  │        │ @Scheduled 60s       │
            │                  │        │                      │
            │ Résultats:       │        │ - Authentifie crawler│
            │ Aucune annonce ! │        │ - Scrape sites web   │
            │                  │        │ - Génère mock data   │
            └──────────────────┘        │ - Envoie au Listing  │
                                        └──────────┬───────────┘
                                                   │
                                      JOUR 1: 10:01 UTC
                                                   │
                                    Listing Service
                                    MySQL Insertion
                                    RabbitMQ event
                                                   │
                                                   ▼
                                    Search Service
                                    Elasticsearch Index
                                                   │
                                      JOUR 1: 10:02 UTC
                                                   │
            ┌──────────────────┐                   │
            │ Recherche        │◄──────────────────┘
            │ GET /api/search  │
            │                  │
            │ Résultats:       │
            │ ✓ 25 annonces !  │
            │   - 5 voitures   │
            │   - 5 immobilier │
            │   - 5 électro    │
            │   - 5 meubles    │
            │   - 5 motos      │
            └──────────────────┘
                      │
                      │ Utilisateur clique sur une annonce
                      │ GET /api/search/{id}/details
                      │
                      ▼
            ┌──────────────────────┐
            │ Détails de l'annonce │
            │ • Titre              │
            │ • Prix               │
            │ • Description        │
            │ • Adresse            │
            │ • Latitude / Longitude│
            │ • Google Maps        │
            └──────────────────────┘
                      │
                      │ Chaque minute, le crawler ajoute
                      │ 25 nouvelles annonces
                      │
                      ▼
            ┌──────────────────────┐
            │ BD se remplit        │
            │ toutes les minutes ! │
            │                      │
            │ Jour 1: 25 annonces  │
            │ Jour 2: 50 annonces  │
            │ Jour 3: 75 annonces  │
            │ ...                  │
            └──────────────────────┘
```

## 7. Table de Routage (API Gateway)

```
REQUEST          | ROUTE              | SERVICE         | PORT
──────────────────────────────────────────────────────────────
GET  /api/search | /api/search        | SEARCH-SERVICE  | 8083
POST /api/listings | /api/listings    | LISTING-SERVICE | 8082
GET  /api/listings/{id} | /api/listings/{id} | LISTING-SERVICE | 8082
POST /api/v1/auth/login | /api/v1/auth/login | AUTH-SERVICE | 8081
POST /api/v1/auth/register | /api/v1/auth/register | AUTH-SERVICE | 8081

Règles:
├─ Toutes les requêtes PASSENT par le Gateway
├─ JWT validé avant routage
├─ Erreur 401 si token invalide
└─ Erreur 404 si route non trouvée
```

---

**Diagrams créés avec Markdown**  
**Dernière update**: 16 janvier 2026
