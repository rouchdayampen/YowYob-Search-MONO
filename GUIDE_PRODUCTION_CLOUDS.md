# ☁️ Guide de Déploiement Cloud (Production) - YowYob

Ce guide détaille comment mettre en ligne la plateforme YowYob en utilisant **Vercel** pour le Frontend et **Render** pour le Backend.

---

## 🏗️ Architecture de Production
- **Frontend** : Next.js sur [Vercel](https://vercel.com)
- **Backend & DB** : Microservices sur [Render](https://render.com)
- **Infrastructure** : 
    - PostgreSQL & Redis sur Render.
    - **Note** : Pour Kafka, Elasticsearch et RabbitMQ, utilisez des services managés (ex: CloudAMQP, Confluent Cloud, Elastic.co) car le déploiement via Docker sur Render Free est limité pour ces services complexes.

---

## 🌐 1. Déploiement Frontend (Vercel)

### Étape A : Push sur GitHub
Assurez-vous que votre dossier `YowYob-Search-Frontend` est sur un dépôt GitHub.

### Étape B : Configuration sur Vercel
1. Importez votre projet sur Vercel.
2. Ajoutez les **Environment Variables** suivantes :
   - `NEXTAUTH_URL` : Votre URL Vercel (ex: `https://votre-app.vercel.app`)
   - `NEXTAUTH_SECRET` : Un secret aléatoire (générez avec `openssl rand -base64 32`)
   - `NEXT_PUBLIC_API_URL` : L'URL de votre **API Gateway** sur Render (ex: `https://yowyob-gateway.onrender.com`)
   - `GOOGLE_CLIENT_ID` : Votre ID Google Cloud
   - `GOOGLE_CLIENT_SECRET` : Votre Secret Google Cloud

---

## ⚙️ 2. Déploiement Backend (Render)

Chaque microservice doit être déployé comme un "Web Service".

### Étape A : Bases de Données
1. Créez un service **PostgreSQL** sur Render.
2. Créez un service **Redis** sur Render.
3. Copiez les URLs de connexion (Internal Database URL).

### Étape B : Déploiement des Microservices
Pour chaque service (Auth, Search, User, Listing, Gateway), créez un "Web Service" pointant vers le repo GitHub.
- **Root Directory** : `YowYob-Search-BackEnd/[nom-du-service]`
- **Build Command** : `mvn clean package -DskipTests`
- **Start Command** : `java -jar target/*.jar`

### Étape C : Variables d'Environnement (Backend)
Configurez ces variables dans l'onglet **Environment** de Render pour chaque service :
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL` : URL de votre Postgres Render.
- `SPRING_REDIS_HOST` : Host de votre Redis Render.
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` : URL de votre instance Kafka Cloud.
- `SPRING_ELASTICSEARCH_URIS` : URL de votre instance Elasticsearch Cloud.

---

## 🔑 3. Correction Google Auth (Production)

L'erreur `invalid_client` ou `OAuth client not found` survient car Google doit connaître vos URLs de production.

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Éditez votre **Client ID OAuth 2.0**.
3. **Origines JavaScript autorisées** :
   - Ajoutez `https://votre-app.vercel.app`
4. **URI de redirection autorisés** :
   - Ajoutez `https://votre-app.vercel.app/api/auth/callback/google`
5. **URGENT** : Vérifiez que le `GOOGLE_CLIENT_ID` dans Vercel est EXACTEMENT le même que celui dans la console Google.

---

## 📋 Checklist Post-Déploiement
- [ ] Le Frontend communique avec la Gateway (CORS configurés).
- [ ] La Gateway redirige vers les services internes de Render via leurs noms d'hôtes internes.
- [ ] Les services Kafka et Elasticsearch managés sont accessibles par les microservices.
