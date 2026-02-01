# 🚀 Guide de Déploiement Complet - YowYob

Ce guide vous explique comment déployer l'intégralité de la plateforme YowYob (Backend, Infrastructure, Monitoring et Frontend) sur votre machine locale.

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé :
- **Docker & Docker Compose** (recommandé : Docker Desktop)
- **Java 17** ou supérieur (pour compiler si besoin)
- **Maven 3.8+**
- **Node.js 18+** & **npm**

---

## 🛠️ Étape 1 : Compilation du Backend (Java)

Tous les microservices Java doivent être compilés pour générer les fichiers `.jar`.

```bash
cd YowYob-Search-BackEnd
# Compilation de tous les services en ignorant les tests pour aller plus vite
mvn clean package -DskipTests
```

---

## 🐳 Étape 2 : Déploiement de l'Infrastructure (Docker)

L'infrastructure comprend PostgreSQL, Elasticsearch, Kafka, RabbitMQ et tous les microservices.

```bash
# Dans le dossier YowYob-Search-BackEnd
docker compose up -d --build
```

**Services Clés déployés :**
- **API Gateway** : `http://localhost:8080`
- **Elasticsearch** : `http://localhost:9200`
- **RabbitMQ (Console)** : `http://localhost:15672` (guest/guest)
- **Grafana (Monitoring)** : `http://localhost:3001` (port configuré dans compose)

---

## 🌐 Étape 3 : Déploiement du Frontend (Next.js)

Une fois que les services Backend sont prêts (vérifier que Elasticsearch est "Healthy"), lancez le client.

```bash
cd ../YowYob-Search-Frontend
# Installation des dépendances
npm install
# Lancement en mode développement
npm run dev -- -p 3000
```

Le site sera accessible sur : **`http://localhost:3000`**

---

## ✅ Étape 4 : Vérification du Déploiement

Pour s'assurer que tout fonctionne, effectuez ces tests rapides :

1. **Santé des containers** : `docker ps` (vérifiez que tous les services sont `Up` ou `Healthy`).
2. **Logs du Crawler** : `docker logs -f crawler-service` (vérifiez qu'il écoute Kafka).
3. **Recherche Front** : Allez sur `localhost:3000`, connectez-vous et faites une recherche (ex: "Batman").

---

## 🛑 Arrêt de la plateforme

Pour tout arrêter proprement :

```bash
# Backend
docker compose down
# Frontend
# Utilisez Ctrl+C dans le terminal du front
```

> [!TIP]
> Si Elasticsearch ne démarre pas, assurez-vous de lui allouer au moins 4Go de RAM dans les réglages de Docker Desktop.
