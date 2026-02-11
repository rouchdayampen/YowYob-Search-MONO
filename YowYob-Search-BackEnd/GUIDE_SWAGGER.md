# 🎯 Guide Swagger - Tester les Endpoints

## Accès Rapide aux APIs avec Swagger UI

Swagger/OpenAPI est une interface visuelle pour tester les endpoints directement dans le navigateur.

---

## 📍 URLs Swagger par Service

### 1. **Auth Service** (Port 8081)
```
http://localhost:8081/swagger-ui.html
```
**Endpoints**:
- `POST /api/v1/auth/login` - Se connecter
- `POST /api/v1/auth/register` - S'inscrire

---

### 2. **API Gateway** (Port 8080)
```
http://localhost:8080/swagger-ui.html
```
**Tous les endpoints** (routés vers les services):
- `/api/v1/auth/*` → Auth Service
- `/api/search*` → Search Service
- `/api/listings*` → Listing Service

⭐ **C'est celui-ci à utiliser !**

---

### 3. **Search Service** (Port 8083)
```
http://localhost:8083/swagger-ui.html
```
**Endpoints**:
- `GET /api/search` - Chercher les annonces
- `GET /api/search/{id}/details` - Détails d'une annonce
- `POST /api/search/index` - Indexer une annonce

---

### 4. **Listing Service** (Port 8082)
```
http://localhost:8082/swagger-ui.html
```
**Endpoints**:
- `POST /api/listings` - Créer une annonce
- `GET /api/listings/{id}` - Récupérer une annonce
- `GET /api/listings` - Lister les annonces

---

### 5. **Crawler Service** (Port 8086)
```
http://localhost:8086/swagger-ui.html
```
**Endpoints**:
- Info de statut du crawler
- Logs du scraping

---

## 🚀 Comment Tester avec Swagger

### Étape 1: Ouvrir Swagger
1. Ouvre ton navigateur
2. Va à: `http://localhost:8080/swagger-ui.html` (API Gateway)
3. Tu vois la liste de tous les endpoints

### Étape 2: Authentification (LOGIN)

1. Développe la section **"Auth Controller"**
2. Clique sur: **`POST /api/v1/auth/login`**
3. Clique sur **"Try it out"**
4. Dans **"RequestBody"**, remplis:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
5. Clique **"Execute"**
6. **Copie le token** de la réponse:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

### Étape 3: Configurer le JWT Token

1. En haut à droite de Swagger, tu vois un bouton **"Authorize"** 🔒
2. Clique dessus
3. Sélectionne **"Bearer"** (si disponible)
4. Colle le token dans le champ: `Bearer {ton_token}`
5. Clique **"Authorize"**
6. ✅ Tu es connecté !

Maintenant tous les appels incluront le JWT automatiquement.

---

## 🔍 Tester Chaque Endpoint

### 1️⃣ Chercher les Voitures

```
Cherche: /api/search (GET)
```

1. Développe **"Search Controller"**
2. Clique sur: **`GET /api/search`**
3. Clique **"Try it out"**
4. Remplis les Query Parameters:
   - **q** = (laisse vide ou écris "voiture")
   - **type** = `VOITURE`
   - **city** = (laisse vide)
5. Clique **"Execute"**

**Résultat attendu**:
```json
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
      "detailsUrl": "/api/search/550e8400-e29b-41d4-a716-446655440001/details"
    }
  ]
}
```

### 2️⃣ Chercher les Immobiliers

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **type** = `IMMOBILIER`
3. Clique **"Execute"**

Tu verras des appartements, villas, studios.

### 3️⃣ Chercher les Électroniques

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **type** = `ELECTRONIQUE`
3. Clique **"Execute"**

Tu verras iPhones, Samsung, Dell, MacBook.

### 4️⃣ Chercher les Meubles

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **type** = `MEUBLES`
3. Clique **"Execute"**

Tu verras canapés, lits, tables, armoires.

### 5️⃣ Chercher les Motos

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **type** = `MOTO`
3. Clique **"Execute"**

Tu verras Suzuki, Yamaha, Honda motos.

### 6️⃣ Chercher par Ville - Yaoundé

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **city** = `Yaoundé`
3. Clique **"Execute"**

Tu verras toutes les annonces à Yaoundé (toutes catégories).

### 7️⃣ Chercher par Ville - Douala

```
Cherche: /api/search (GET)
```

1. Clique sur **`GET /api/search`** → **"Try it out"**
2. Remplis:
   - **city** = `Douala`
3. Clique **"Execute"**

Tu verras toutes les annonces à Douala.

### 8️⃣ Voir les Détails d'une Annonce

```
Cherche: /api/search/{id}/details (GET)
```

1. D'abord, fais une recherche (étape 1️⃣)
2. **Copie l'ID** d'une annonce: `550e8400-e29b-41d4-a716-446655440001`
3. Développe **"Search Controller"**
4. Clique sur: **`GET /api/search/{id}/details`**
5. Clique **"Try it out"**
6. Remplis:
   - **id** = `550e8400-e29b-41d4-a716-446655440001`
7. Clique **"Execute"**

**Résultat**:
```json
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

✅ La **géolocalisation** est incluse !

### 9️⃣ Créer une Annonce

```
Crée: /api/listings (POST)
```

1. Développe **"Listing Controller"**
2. Clique sur: **`POST /api/listings`**
3. Clique **"Try it out"**
4. Remplis le **RequestBody**:
```json
{
  "title": "MacBook Pro 14 pouces - Neuf 2023",
  "description": "Neuf jamais utilisé, boîte scellée d'usine",
  "price": 850000,
  "category": "ELECTRONIQUE",
  "address": "Yaoundé, Bastos",
  "latitude": 3.86,
  "longitude": 11.5,
  "sellerId": "550e8400-e29b-41d4-a716-446655440000"
}
```
5. Clique **"Execute"**

**Résultat**: `201 Created` avec l'ID de la nouvelle annonce

### 🔟 Chercher l'Annonce Créée

1. Retourne à étape 1️⃣ (**`GET /api/search`**)
2. Cherche par **type** = `ELECTRONIQUE`
3. Tu verras ton MacBook Pro !

✅ La nouvelle annonce est **immédiatement cherchable** !

---

## 📊 Vue d'ensemble des Endpoints

| Endpoint | Méthode | Description | Paramètres |
|----------|---------|-------------|-----------|
| `/api/v1/auth/login` | POST | Se connecter | email, password |
| `/api/v1/auth/register` | POST | S'inscrire | email, password |
| `/api/search` | GET | Chercher annonces | q, type, city |
| `/api/search/{id}/details` | GET | Détails annonce | id |
| `/api/listings` | POST | Créer annonce | title, description, price, etc. |
| `/api/listings/{id}` | GET | Récupérer annonce | id |
| `/api/listings` | GET | Lister annonces | page, size |

---

## 🎯 Résumé des Catégories à Tester

| Type | URL | Résultat attendu |
|------|-----|-----------------|
| VOITURE | `type=VOITURE` | Toyota, Honda, Peugeot, etc. |
| IMMOBILIER | `type=IMMOBILIER` | Apartements, villas, studios |
| ELECTRONIQUE | `type=ELECTRONIQUE` | iPhone, Samsung, Dell, MacBook |
| MEUBLES | `type=MEUBLES` | Canapés, lits, tables, armoires |
| MOTO | `type=MOTO` | Suzuki, Yamaha, Honda motos |

---

## 🐛 Debugging dans Swagger

### Erreur: 401 Unauthorized
```
❌ "No Authorization header"
✅ Solutio: Clique "Authorize" et colle ton JWT token
```

### Erreur: 500 Connection Refused
```
❌ "Cannot connect to localhost:8080"
✅ Solution: Vérifier que les services tournent
  docker ps
  ps aux | grep java
```

### Erreur: 404 Not Found
```
❌ "Cannot GET /api/search"
✅ Solution: Vérifier l'URL et les paramètres
```

---

## 💡 Avantages de Swagger

✅ **Interface visuelle** - Pas besoin de cURL ou Postman  
✅ **Documentation automatique** - Les endpoints sont documentés  
✅ **Tests interactifs** - Clique et teste directement  
✅ **Validation** - Swagger valide les données  
✅ **JSON bien formaté** - Réponses lisibles  
✅ **Schémas** - Vois la structure attendue  

---

## 📝 Ordre de Test Recommandé

1. **Login** → Obtient le JWT
2. **Authorize** → Configure le token
3. **Search Voitures** → Voit les voitures
4. **Search Immobilier** → Voit les immobiliers
5. **Search Électronique** → Voit les électroniques
6. **Search Meubles** → Voit les meubles
7. **Search Motos** → Voit les motos
8. **Search Yaoundé** → Voit par ville
9. **Search Douala** → Voit par ville
10. **Détails** → Copie un ID et voit les détails
11. **Créer Annonce** → Crée une nouvelle annonce
12. **Chercher la Nouvelle** → Voit le nouveau MacBook

**Temps total**: ~30 minutes

---

## 🔗 Liens Directs

```
Auth Service Swagger:
http://localhost:8081/swagger-ui.html

API Gateway Swagger (UTILISÉ CELUI-CI):
http://localhost:8080/swagger-ui.html

Search Service Swagger:
http://localhost:8083/swagger-ui.html

Listing Service Swagger:
http://localhost:8082/swagger-ui.html

Crawler Service Swagger:
http://localhost:8086/swagger-ui.html
```

---

**Créé**: 16 janvier 2026  
**Pour**: Tester les endpoints via Swagger UI
