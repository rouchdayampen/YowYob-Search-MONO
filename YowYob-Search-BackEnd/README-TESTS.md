# 🧪 Guide de Test Manuel du Backend (Architecture Hexagonale)

Ce document explique comment démarrer l'ensemble du projet en local et tester manuellement les microservices, notamment avec **Postman**.

---

## 🛠️ Prérequis

Assurez-vous d'avoir installé sur votre machine :
- **Java 17+** et **Maven**
- **Docker** et **Docker Compose**
- **Postman** (pour l'exécution des tests API)

---

## 🚀 Étape 1 : Compiler les Microservices

Puisque nous avons migré vers une architecture hexagonale et supprimé l'ancienne structure, les anciens tests unitaires sont obsolètes. Nous devons compiler les services en ignorant ces vieux tests.

Ouvrez un terminal à la racine du projet (`YowYob-Search-BackEnd`) et exécutez la commande suivante :

```bash
for svc in api-gateway auth-service geo-service listing-service notification-service search-service user-service; do 
  echo "Construction de $svc..."
  (cd $svc && mvn clean package -Dmaven.test.skip=true)
done
```

> **Résultat attendu :** Chaque dossier affichera un `BUILD SUCCESS` et un fichier `.jar` sera créé dans leur dossier `target/` respectif.

---

## 🐳 Étape 2 : Lancer l'Infrastructure avec Docker

Une fois les exécutables `.jar` générés, nous allons lancer les bases de données (PostgreSQL, Redis, Elasticsearch, Kafka, Zookeeper) ainsi que nos propres microservices.

Toujours à la racine du projet, lancez :

```bash
docker compose up --build -d
```
*Note : Le paramètre `--build` force Docker à recréer les images de vos services pour prendre en compte les nouveaux `.jar`.*

### Vérifier le bon démarrage
Il est crucial d'attendre que l'API Gateway et les bases de données soient totalement démarrées. Vous pouvez vérifier l'état général avec :
```bash
docker compose ps
```
Pour voir les logs d'un service spécifique en temps réel (par exemple le `search-service`) :
```bash
docker compose logs -f search-service
```

---

## 🎯 Étape 3 : Tester avec Postman

À la racine du projet se trouve le fichier **`YOWYOB_Postman_Collection.json`**. Ce fichier contient toutes les requêtes prêtes à l'emploi.

1. **Ouvrez Postman**.
2. Cliquez sur **"Import"** (en haut à gauche).
3. Sélectionnez le fichier `YOWYOB_Postman_Collection.json`.
4. La collection va apparaître dans votre barre latérale gauche.

### Ordre recommandé pour les tests manuels :

1. **Test de l'Auth Service**
   - Créez un compte utilisateur.
   - Connectez-vous (Login) pour récupérer un **Token JWT**.
   - *Pensez à injecter ce token dans les headers d'autorisation (Bearer Token) pour les requêtes suivantes.*

2. **Test du User Service**
   - Créez / Récupérez votre profil utilisateur.

3. **Test du Listing Service**
   - Créez une nouvelle annonce (Listing).
   - Vérifiez que vous pouvez la récupérer par son ID.

4. **Test du Geo Service**
   - Testez les routes de géocodage avec une adresse (ex: `Douala`).

5. **Test du Search Service**
   - Faites une recherche textuelle.
   - *Astuce : Vérifiez dans les logs du `search-service` si les annonces créées à l'étape 3 sont bien indexées via RabbitMQ.*

---

## 🛑 Étape 4 : Arrêt et Nettoyage

Une fois vos tests terminés, pour arrêter proprement les conteneurs et libérer les ports de votre machine :

```bash
docker compose down
```

> [!TIP]
> **Problème de Cache ?**
> Si vous faites de nouvelles modifications dans le code et que les changements ne semblent pas pris en compte, relancez toujours la commande Maven (Étape 1), puis relancez `docker compose up --build -d`.
