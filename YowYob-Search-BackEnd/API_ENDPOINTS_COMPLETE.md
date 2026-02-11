# API Endpoints - Documentation Complète

## Vue d'ensemble

Ce document décrit tous les endpoints disponibles dans YowYob Search Backend, incluant :
- **Search Service** : Recherche et indexation de produits
- **Geo Service** : Géolocalisation et calcul de distances

---

## SEARCH SERVICE (Port 8082)

### 1. Recherche Simple
**Endpoint** : `GET /api/search`

**Description** : Recherche de produits avec filtrage par type et ville. Supporte les synonymes et la recherche par mots-clés.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `q` | string | Non | Requête de recherche (ex: "resto", "hotel", "agence") |
| `type` | string | Non | Filtrer par type (agency, restaurant, hotel, beauty) |
| `city` | string | Non | Filtrer par ville (Douala, Yaoundé, Buea, etc.) |

**Exemples** :
```bash
# Chercher tous les restaurants
curl "http://localhost:8082/api/search?q=resto"

# Chercher les hôtels à Douala
curl "http://localhost:8082/api/search?q=hotel&city=Douala&type=hotel"

# Chercher par synonyme (lodge = hotel)
curl "http://localhost:8082/api/search?q=lodge"

# Chercher les instituts de beauté
curl "http://localhost:8082/api/search?q=salon"
```

**Réponse** :
```json
{
  "success": true,
  "query": "resto",
  "total": 8,
  "results": [
    {
      "id": "1",
      "name": "Restaurant Le Traditionnel",
      "description": "Restaurant traditionnel camerounais",
      "type": "restaurant",
      "city": "Douala",
      "rating": null,
      "detailsUrl": "/api/search/1/details"
    }
  ]
}
```

**Codes de réponse** :
- `200` : Succès
- `400` : Paramètres invalides

---

### 2. Recherche par Proximité (avec Référence)
**Endpoint** : `GET /api/search/proximity`

**Description** : Recherche de produits dans un rayon donné autour d'une ville. Utile pour chercher les établissements à proximité d'une ville spécifique.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `q` | string | Non | Requête de recherche |
| `city` | string | Oui | Ville de référence (centre de recherche) |
| `radius` | double | Non | Rayon de recherche en km (défaut: 10) |

**Exemples** :
```bash
# Chercher des restaurants dans 10km autour de Douala
curl "http://localhost:8082/api/search/proximity?q=restaurant&city=Douala"

# Chercher des hôtels dans 20km autour de Yaoundé
curl "http://localhost:8082/api/search/proximity?q=hotel&city=Yaoundé&radius=20"

# Chercher par synonyme avec rayon spécifique
curl "http://localhost:8082/api/search/proximity?q=resto&city=Buea&radius=15"
```

**Réponse** : Même format que la recherche simple

---

### 3. Recherche Près de l'Utilisateur ⭐
**Endpoint** : `GET /api/search/near-me`

**Description** : Recherche intelligente de proximité avec support pour les expressions naturelles ("près de chez moi", "très loin", etc.) et géolocalisation automatique par IP. **C'est l'endpoint principal pour les applications mobiles/web**.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `q` | string | Non | Requête avec expressions de proximité (ex: "restaurants près de chez moi") |
| `latitude` | double | Non* | Latitude de l'utilisateur (résultat en km) |
| `longitude` | double | Non* | Longitude de l'utilisateur (résultat en km) |
| `ip` | string | Non* | Adresse IP pour géolocalisation automatique |
| `type` | string | Non | Filtrer par type (agency, restaurant, hotel, beauty) |

*Note : Au moins l'une des options suivantes est nécessaire :
- Fournir `latitude` ET `longitude` directement
- Fournir `ip` pour géolocalisation automatique
- Les deux peuvent être utilisés (latitude/longitude ont priorité)

**Expressions de Proximité Supportées** :
| Expression | Rayon | Exemple |
|-----------|-------|---------|
| "très près", "très proche" | 2 km | "restaurants très près de moi" |
| "près", "proche" | 5 km | "hôtels près de chez moi" |
| "à proximité" | 5 km | "agences à proximité" |
| "loin" | 20 km | "restaurants loin de moi" |
| "très loin" | 50 km | "hôtels très loin" |
| (aucune) | 10 km | "restaurants" (rayon par défaut) |

**Exemples** :
```bash
# Recherche avec coordonnées directes
curl "http://localhost:8082/api/search/near-me?q=resto+pres+de+chez+moi&latitude=4.0511&longitude=9.7679"

# Recherche avec IP (géolocalisation automatique)
curl "http://localhost:8082/api/search/near-me?q=resto+pres+de+chez+moi&ip=127.0.0.1"

# Recherche avec filtre de type
curl "http://localhost:8082/api/search/near-me?q=restaurants+très+près+de+chez+moi&latitude=4.0511&longitude=9.7679&type=restaurant"

# Recherche avec rayon important
curl "http://localhost:8082/api/search/near-me?q=hôtels+très+loin&latitude=3.8667&longitude=11.5167"

# Recherche simple (rayon par défaut 10km)
curl "http://localhost:8082/api/search/near-me?q=salon&latitude=4.0511&longitude=9.7679"
```

**Réponse** :
```json
{
  "success": true,
  "query": "resto pres de chez moi",
  "total": 3,
  "results": [
    {
      "id": "1",
      "name": "Restaurant Le Traditionnel",
      "description": "Restaurant traditionnel camerounais - 0,5 km away",
      "type": "restaurant",
      "city": "Douala",
      "rating": null,
      "detailsUrl": "/api/search/1/details"
    },
    {
      "id": "2",
      "name": "Restaurant La Mer",
      "description": "Fruits de mer frais - 1,2 km away",
      "type": "restaurant",
      "city": "Douala",
      "rating": null,
      "detailsUrl": "/api/search/2/details"
    }
  ]
}
```

**Points Clés** :
- Les résultats sont triés par distance (plus proche en premier)
- La distance en km est ajoutée à la description
- Support des **synonymes** : "resto" → "restaurant", "lodge" → "hotel", "salon" → "beauty", etc.
- **Géolocalisation IP automatique** : Appelle le geo-service pour résoudre l'IP
- **Fallback Douala** : Pour localhost (127.0.0.1), utilise les coordonnées de Douala comme test
- Supporte les **expressions de proximité naturelles en français**

---

### 4. Détails d'un Produit
**Endpoint** : `GET /api/search/{id}/details`

**Description** : Récupère les informations détaillées d'un produit spécifique.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `id` | string | Oui | ID du produit |

**Exemples** :
```bash
# Obtenir les détails du produit ID 1
curl "http://localhost:8082/api/search/1/details"

# Obtenir les détails du produit ID 15
curl "http://localhost:8082/api/search/15/details"
```

**Réponse** :
```json
{
  "success": true,
  "result": {
    "id": "1",
    "name": "Restaurant Le Traditionnel",
    "description": "Restaurant traditionnel camerounais avec spécialités locales",
    "type": "restaurant",
    "city": "Douala",
    "latitude": 4.0511,
    "longitude": 9.7679,
    "rating": 4.5,
    "detailsUrl": "https://example.com/restaurant/1"
  }
}
```

---

### 5. Indexer un Produit
**Endpoint** : `POST /api/search/index`

**Description** : Ajoute un nouveau produit à l'index Elasticsearch. Si les coordonnées ne sont pas fournies, le service géocode automatiquement la ville.

**Corps de la Requête** :
```json
{
  "name": "New Restaurant",
  "description": "Delicious food",
  "type": "restaurant",
  "city": "Douala",
  "latitude": 4.0511,
  "longitude": 9.7679,
  "rating": 4.5
}
```

**Paramètres Optionnels** :
- `latitude`, `longitude` : Si non fournis, seront géocodés automatiquement
- `rating` : Note du produit (optionnel)

**Exemples** :
```bash
# Indexer avec coordonnées complètes
curl -X POST "http://localhost:8082/api/search/index" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Restaurant Nouveau",
    "description": "Cuisine locale",
    "type": "restaurant",
    "city": "Douala",
    "latitude": 4.0511,
    "longitude": 9.7679,
    "rating": 4.0
  }'

# Indexer sans coordonnées (géocodage automatique)
curl -X POST "http://localhost:8082/api/search/index" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hotel Luxe",
    "description": "Hotel 5 stars",
    "type": "hotel",
    "city": "Yaoundé"
  }'
```

**Réponse** :
```json
{
  "success": true,
  "id": "24",
  "message": "Product indexed successfully"
}
```

---

### 6. Santé du Service
**Endpoint** : `GET /api/search/health`

**Description** : Vérife que le service Search fonctionne correctement.

**Exemples** :
```bash
curl "http://localhost:8082/api/search/health"
```

**Réponse** :
```
Search Service with Elasticsearch and GeoService Integration!
```

---

## GEO SERVICE (Port 8085)

### 1. Géocoder une Adresse
**Endpoint** : `GET /api/geo/geocode`

**Description** : Convertit une adresse/ville en coordonnées (latitude, longitude). Utilise un système de géocodage interne avec cache Redis.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `address` | string | Oui | Adresse ou nom de ville à géocoder |

**Exemples** :
```bash
# Géocoder une ville
curl "http://localhost:8085/api/geo/geocode?address=Douala"

# Géocoder une adresse spécifique
curl "http://localhost:8085/api/geo/geocode?address=Yaoundé"

# Géocoder Bafoussam
curl "http://localhost:8085/api/geo/geocode?address=Bafoussam"
```

**Réponse** :
```json
{
  "latitude": 4.0511,
  "longitude": 9.7679,
  "city": "Douala",
  "country": "Cameroon"
}
```

**Caching** : Les résultats sont mis en cache dans Redis pour 30 jours

---

### 2. Calculer la Distance
**Endpoint** : `POST /api/geo/distance`

**Description** : Calcule la distance entre deux points géographiques en utilisant la formule de Haversine.

**Corps de la Requête** :
```json
{
  "lat1": 4.0511,
  "lon1": 9.7679,
  "lat2": 3.8667,
  "lon2": 11.5167
}
```

**Exemples** :
```bash
# Calculer la distance entre Douala et Yaoundé
curl -X POST "http://localhost:8085/api/geo/distance" \
  -H "Content-Type: application/json" \
  -d '{
    "lat1": 4.0511,
    "lon1": 9.7679,
    "lat2": 3.8667,
    "lon2": 11.5167
  }'
```

**Réponse** :
```json
{
  "distance": 245.67,
  "unit": "km"
}
```

---

### 3. Géolocalisation par IP ⭐
**Endpoint** : `GET /api/geo/ip-location`

**Description** : Résout une adresse IP en coordonnées géographiques. Utilise l'API ipapi.co comme backend avec cache Redis (30 jours). **C'est le service utilisé pour la géolocalisation automatique dans `/api/search/near-me`**.

**Paramètres** :
| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `ip` | string | Oui | Adresse IP à géolocaliser |

**Exemples** :
```bash
# Géolocaliser une IP publique
curl "http://localhost:8085/api/geo/ip-location?ip=8.8.8.8"

# Géolocaliser localhost (retourne Douala par défaut)
curl "http://localhost:8085/api/geo/ip-location?ip=127.0.0.1"

# Géolocaliser une IP française
curl "http://localhost:8085/api/geo/ip-location?ip=80.236.131.218"
```

**Réponse** :
```json
{
  "latitude": 37.42301,
  "longitude": -122.083352,
  "city": "Mountain View",
  "country": "United States"
}
```

**Fallback** : 
- Pour localhost/IPs invalides : retourne les coordonnées de Douala (4.0511, 9.7679)
- En cas d'erreur d'API : retourne également Douala

**Caching** : 
- Résultats stockés dans Redis pour 30 jours
- Évite les appels répétés à l'API externe

---

### 4. Santé du Service
**Endpoint** : `GET /api/geo/health`

**Description** : Vérifie que le service Geo fonctionne correctement.

**Exemples** :
```bash
curl "http://localhost:8085/api/geo/health"
```

**Réponse** :
```
Geo Service is running!
```

---

## Cas d'Usage Pratiques

### Cas 1 : Application Mobile - Recherche Proche de l'Utilisateur
```bash
# L'app envoie l'IP du client
curl "http://localhost:8082/api/search/near-me?q=restaurants+pres+de+chez+moi&ip=USER_IP"

# Ou avec coordonnées GPS
curl "http://localhost:8082/api/search/near-me?q=restaurants+pres+de+chez+moi&latitude=GPS_LAT&longitude=GPS_LON"
```

### Cas 2 : Recherche Générale avec Filtre de Type
```bash
# Chercher uniquement les restaurants
curl "http://localhost:8082/api/search?q=resto&type=restaurant"

# Chercher uniquement les hôtels à Douala
curl "http://localhost:8082/api/search?q=lodge&type=hotel&city=Douala"
```

### Cas 3 : Recherche Intelligente avec Proximité
```bash
# L'utilisateur dit "Je cherche des hôtels très près de chez moi"
curl "http://localhost:8082/api/search/near-me?q=hôtels+très+près+de+chez+moi&latitude=4.0511&longitude=9.7679"
# → Rayon de recherche = 2 km, résultats triés par distance
```

### Cas 4 : Administration - Ajouter un Établissement
```bash
curl -X POST "http://localhost:8082/api/search/index" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nouveau Restaurant",
    "description": "Cuisine fusion",
    "type": "restaurant",
    "city": "Bamenda",
    "rating": 4.5
  }'
# → Géocodage automatique de Bamenda
```

---

## Synonymes Supportés

Le système supporte les synonymes suivants via Elasticsearch :

| Mot Clé | Synonymes | Exemples |
|---------|-----------|----------|
| **agence** | tour, operator, tourism, tourisme, travel_agency | "tour", "operator" → trouve les agences |
| **restaurant** | resto, cafe, café, bistro, grill, table, dining | "resto", "café", "bistro" → trouve les restaurants |
| **hotel** | lodge, auberge, resort, inn, motel, palace, accommodation | "lodge", "auberge", "resort" → trouve les hôtels |
| **beaute** | salon, institut, spa, wellness, cosmetics | "salon", "spa", "wellness" → trouve les instituts de beauté |

---

## Architecture d'Intégration

```
Client (Web/Mobile)
    ↓
[Search Service - Port 8082]
    ├─→ Elasticsearch (Recherche, Synonymes)
    ├─→ Geo Service (Géolocalisation)
    └─→ Redis (Cache)
    
[Geo Service - Port 8085]
    ├─→ ipapi.co (Géolocalisation IP)
    ├─→ Redis (Cache - 30 jours)
    └─→ Elasticsearch (Géocodage - villes Cameroun)
```

---

## Authentification

Actuellement, **aucune authentification** n'est requise. Tous les endpoints sont publics.

Pour les déploiements en production, envisagez d'ajouter :
- API Keys
- JWT Tokens
- OAuth 2.0

---

## Limitations et Considérations

### Rate Limiting
- L'API ipapi.co (géolocalisation IP) a une limite de 30000 requêtes/mois en version gratuite
- Les résultats sont mis en cache pour 30 jours dans Redis

### Faux Positifs en Recherche
- Si une description mentionne un mot-clé (ex: "hotel avec restaurant"), cet établissement sera retourné même si ce n'est pas sa catégorie principale
- **Solution** : Utilisez le paramètre `type` pour filtrer par catégorie exacte

### Villes Supportées
Les villes Camerounaises supportées incluent :
- Douala, Yaoundé, Buea, Bafoussam, Bamenda, Garoua, Limbe, Dschang, Kumba, etc.

Pour ajouter une nouvelle ville, modifiez `setup_synonyms_index.sh`

---

## Support et Debugging

Pour debugguer les requêtes :

```bash
# Voir tous les documents dans Elasticsearch
curl "http://localhost:9200/products/_search" | jq '.hits.hits'

# Vérifier le cache Redis
redis-cli KEYS "geo:ip:*"
redis-cli GET "geo:ip:8.8.8.8"

# Voir les logs des services
tail -f /tmp/search-service.log
tail -f /tmp/geo-service.log
```

---

**Version** : 1.0.0  
**Dernière Mise à Jour** : 22 janvier 2026
