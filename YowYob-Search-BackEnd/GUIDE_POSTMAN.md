# Guide Postman - YOWYOB

## Étape 1: Télécharger Postman

1. Va sur: https://www.postman.com/downloads/
2. Télécharge la version pour Linux
3. Installe-la: `tar -xzf Postman-linux-x64.tar.gz`
4. Lance: `./Postman/app/Postman`

## Étape 2: Importer la Collection

1. Ouvre Postman
2. Clique sur **Import** (en haut à gauche)
3. Sélectionne **Upload Files**
4. Trouve et sélectionne: `YOWYOB_Postman_Collection.json`
5. Clique **Import**

Tu devrais voir 12 requêtes prêtes à tester !

## Étape 3: Configurer l'environnement

1. Clique sur **Environments** (à gauche)
2. Clique sur **YOWYOB API Collection** (ou crée un nouveau)
3. Configure les variables:

```
Variable        | Type   | Value
base_url       | string | http://localhost:8080
auth_token     | string | (sera rempli automatiquement après Login)
user_id        | string | 550e8400-e29b-41d4-a716-446655440000
```

4. **Save**

## Étape 4: Tester les endpoints

### Commande 1️⃣: LOGIN (Obligatoire d'abord !)

```
Sélectionne: "1. AUTH - Login"
Clique: Send
```

**Résultat attendu:**
```
Status: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

✅ Le token est automatiquement sauvegardé dans `auth_token` !

### Commande 2️⃣: RECHERCHE - Voitures

```
Sélectionne: "2. SEARCH - Toutes les voitures"
Clique: Send
```

**Résultat attendu:**
```
Status: 200 OK
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
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "Honda Civic Automatique 2019",
      "description": "Climatisation fonctionne, révision à jour",
      "price": 3100000,
      "type": "VOITURE",
      "category": "VOITURE",
      "city": "Douala, Akwa"
    }
  ],
  "totalCount": 10
}
```

### Commande 3️⃣: RECHERCHE - Immobilier

```
Sélectionne: "3. SEARCH - Tous les immobiliers"
Clique: Send
```

**Tu devrais voir des appartements et villas avec les bons prix !**

### Commande 4️⃣: RECHERCHE - Meubles

```
Sélectionne: "4. SEARCH - Tous les meubles"
Clique: Send
```

**Tu devrais voir des canapés, lits, tables (PAS de voitures !)**

### Commande 5️⃣: RECHERCHE - Électronique

```
Sélectionne: "5. SEARCH - Tous les électroniques"
Clique: Send
```

**Tu devrais voir des iPhones, Samsung, Dell, MacBook**

### Commande 6️⃣: RECHERCHE - Motos

```
Sélectionne: "6. SEARCH - Tous les motos"
Clique: Send
```

**Tu devrais voir des Suzuki, Yamaha, Honda motos**

### Commande 7️⃣: RECHERCHE par VILLE - Yaoundé

```
Sélectionne: "7. SEARCH - Recherche à Yaoundé"
Clique: Send
```

### Commande 8️⃣: RECHERCHE par VILLE - Douala

```
Sélectionne: "8. SEARCH - Recherche à Douala"
Clique: Send
```

### Commande 9️⃣: DÉTAILS d'une annonce

1. D'abord, exécute la Commande 2 (recherche voitures)
2. Copie l'ID d'une voiture: ex: `550e8400-e29b-41d4-a716-446655440001`
3. Va à "9. SEARCH - Détails d'une annonce"
4. **Remplace** `{listing_id}` par l'ID copié:

```
Avant: {{base_url}}/api/search/{listing_id}/details
Après: {{base_url}}/api/search/550e8400-e29b-41d4-a716-446655440001/details
```

5. Clique: **Send**

**Résultat:**
```
Status: 200 OK
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

### Commande 🔟: CRÉER une nouvelle annonce

```
Sélectionne: "10. LISTING - Créer une annonce"
Clique: Send
```

**Résultat:**
```
Status: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440099",
  "title": "MacBook Pro 14 pouces - Neuf 2023",
  "description": "Neuf jamais utilisé, boîte scellée d'usine",
  "price": 850000,
  "category": "ELECTRONIQUE",
  "address": "Yaoundé, Bastos"
}
```

Maintenant, recherche le MacBook:
```
Va à "5. SEARCH - Tous les électroniques"
Clique: Send
Tu verras le MacBook dans les résultats ! ✅
```

## Ordre recommandé pour tester

```
1. LOGIN (obtient le token)
   ↓
2. SEARCH - Voitures
   ↓
3. SEARCH - Immobilier
   ↓
4. SEARCH - Meubles
   ↓
5. SEARCH - Électronique
   ↓
6. SEARCH - Motos
   ↓
7. SEARCH - Yaoundé
   ↓
8. SEARCH - Douala
   ↓
9. DÉTAILS (copie un ID de la requête 2)
   ↓
10. CRÉER une annonce
    ↓
11. RECHERCHER le MacBook (requête 5)
```

## Debugging

### Erreur: 401 Unauthorized

```
❌ Erreur: "Authorization token was not specified"
✅ Solution: Refaire LOGIN d'abord (Commande 1)
```

### Erreur: 500 Connection Refused

```
❌ Erreur: "Connection refused: localhost/127.0.0.1:8080"
✅ Solution: 
  1. Vérifier que les services tournent: docker ps
  2. Vérifier: ps aux | grep java
  3. Redémarrer les services si nécessaire
```

### Erreur: 404 Not Found

```
❌ Erreur: "Cannot GET /api/search"
✅ Solution: 
  1. Vérifier l'URL (pas d'espace ni d'accent)
  2. Vérifier que le Search Service tourne sur port 8083
  3. Vérifier que API Gateway tourne sur port 8080
```

## Personnaliser les requêtes

Tu peux modifier les requêtes facilement:

### Chercher une ville différente:

1. Ouvre "7. SEARCH - Recherche à Yaoundé"
2. Dans **Params**, change `city` de `Yaoundé` à `Buea`
3. Clique: Send

### Chercher par mot-clé:

1. Ouvre "2. SEARCH - Toutes les voitures"
2. Dans **Params**, change `q` de `voiture` à `Toyota`
3. Clique: Send

### Créer une annonce différente:

1. Ouvre "10. LISTING - Créer une annonce"
2. Va au **Body** (onglet Body)
3. Modifie le JSON:

```json
{
  "title": "Villa 4 chambres - Douala Akwa",
  "description": "Villa récemment rénovée, très confortable",
  "price": 150000000,
  "category": "IMMOBILIER",
  "address": "Douala, Akwa",
  "latitude": 4.05,
  "longitude": 9.75,
  "sellerId": "550e8400-e29b-41d4-a716-446655440000"
}
```

4. Clique: Send

## Voir les détails techniques

Postman te montre aussi:

- **Status**: Code HTTP (200, 201, 400, 401, 500)
- **Time**: Temps de réponse en ms
- **Size**: Taille de la réponse en bytes
- **Headers**: En-têtes HTTP
- **Body**: Réponse JSON

Pour debugger, clique sur **Pretty** (dans Body) pour formatter le JSON.

## Conseils

1. **Sauvegarde tes requêtes**: Postman les sauvegarde auto
2. **Teste toujours LOGIN d'abord**: Sinon erreur 401
3. **Copie les IDs**: Pour tester les détails
4. **Modifie le JSON**: Crée différentes annonces
5. **Consulte les logs**: `docker logs <container>` pour déboguer

---

**Dernière mise à jour**: 16 janvier 2026  
**Créateur**: Assistant GitHub Copilot
