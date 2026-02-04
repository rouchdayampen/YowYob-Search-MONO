# 🚀 Guide de Déploiement Complet - YowYob

Ce guide décrit les étapes nécessaires pour démarrer, gérer et surveiller l'écosystème YowYob (Moteur de Recherche d'Annonces).

---

## 🏗️ Architecture du Système

YowYob utilise une architecture moderne basée sur les microservices :

- **Frontend** : Application Next.js 14+ (React) pour une interface rapide et SEO-friendly.
- **Backend (Microservices)** :
    - `api-gateway` : Point d'entrée unique, gère la sécurité et le routage.
    - `auth-service` : Gestion des utilisateurs et authentification JWT.
    - `listing-service` : Gestion CRUD des annonces et stockage PostgreSQL.
    - `search-service` : Moteur de recherche haute performance basé sur Elasticsearch.
    - `crawler-service` : Automatisation du scraping (OLX, Jumia) et génération de données.
    - `geo-service` : Gestion des coordonnées GPS et adresses.
- **Infrastructure** :
    - **PostgreSQL** : Données relationnelles.
    - **Elasticsearch** : Indexation et recherche textuelle.
    - **Redis** : Cache pour les performances.
    - **RabbitMQ / Kafka** : Communication asynchrone entre services.
- **Observabilité** : Grafana, Loki (logs), Prometheus (métriques), Tempo (traces).

---

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé :
- **Docker & Docker Compose** (fortement recommandé).
- **Node.js 18+** & **npm** (pour le frontend).
- **Java 17** & **Maven** (uniquement si vous souhaitez compiler manuellement).

---

## 🚀 Procédure de Démarrage

### 1. Lancer le Backend (Infrastructure & Microservices)
La manière la plus simple est d'utiliser Docker Compose qui gère tout l'écosystème.

```bash
cd YowYob-Search-BackEnd
docker compose up -d
```

> [!NOTE]
> Le premier démarrage peut prendre quelques minutes car ElasticSearch et PostgreSQL doivent s'initialiser et les images doivent être construites.

### 2. Lancer le Frontend
Ouvrez un nouveau terminal pour le client :

```bash
cd YowYob-Search-Frontend
npm install
npm run dev
```

L'application sera disponible sur **`http://localhost:3000`**.

---

## 🔍 Tableau de Bord des Accès

| Composant | URL | Accès / Infos |
| :--- | :--- | :--- |
| **Application Web** | `http://localhost:3000` | Interface principale |
| **API Gateway** | `http://localhost:8080` | URL de base pour les APIs |
| **Grafana** | `http://localhost:3001` | Monitoring (Pas de login requis) |
| **RabbitMQ Console** | `http://localhost:15672` | Login: `guest` / Pass: `guest` |
| **Elasticsearch** | `http://localhost:9200` | Statut du moteur de recherche |
| **Prometheus** | `http://localhost:9090` | Base de données de métriques |

---

## 🛠️ "Relancer" le Système (Maintenance)

Si vous rencontrez des erreurs de connexion ou si vous avez modifié le code backend :

### Redémarrage complet (Recommandé)
```bash
# Dans YowYob-Search-BackEnd
docker compose down
docker compose up -d --build
```

### Relancer un service spécifique (ex: le Crawler)
Si un service s'est arrêté prématurément (souvent dû à une attente de Kafka/DB) :
```bash
docker compose start crawler-service
```

---

## 📊 Monitoring & Logs

Pour voir ce qui se passe "sous le capot" :
1. Allez sur **`http://localhost:3001`** (Grafana).
2. Cliquez sur **"Explore"** (icône boussole).
3. Sélectionnez **"Loki"** dans la liste déroulante en haut à gauche.
4. Utilisez le sélecteur d'étiquettes `{container_name="api-gateway"}` pour voir les logs d'un service spécifique.

---

## 💡 Résolution des Problèmes Courants

- **Elasticsearch ne démarre pas** : Assurez-vous d'avoir alloué au moins 4Go de RAM à Docker dans vos réglages globaux.
- **Erreurs de connexion Kafka** : C'est normal au tout premier démarrage si les services démarrent plus vite que Kafka. Relancez simplement les containers concernés.
- **Données manquantes** : Le `crawler-service` peuple la base de données automatiquement. Attendez environ 1 minute après le démarrage complet pour voir les premières annonces apparaître.

---

*Guide généré pour le projet YowYob Search Engine.*
