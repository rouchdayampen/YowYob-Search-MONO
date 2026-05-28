# Migration vers l'Architecture Hexagonale (Ports & Adaptateurs)

Ce document explique la démarche et les étapes suivies pour migrer l'ensemble des microservices du projet **YowYob-Search-BackEnd** d'une architecture classique en couches (Layered Architecture - Controller/Service/Repository) vers une **Architecture Hexagonale**.

## 🎯 Objectif de la Migration

L'objectif principal de cette refactorisation était de **découpler la logique métier de l'infrastructure technologique** (Spring Boot, JPA, Elasticsearch, RabbitMQ, REST, etc.). 
Avant la migration, le cœur de l'application dépendait fortement des annotations de frameworks (ex: `@Entity`, `@Document`), rendant le code difficile à tester de manière isolée et complexe à maintenir lors des changements de technologies.

Grâce à l'architecture hexagonale :
1. **Le Domaine est roi** : La logique métier ne dépend plus de rien.
2. **L'Infrastructure est un détail** : La base de données, l'API Web et le bus de messages gravitent autour du domaine.
3. **Haute Testabilité** : Il est possible de tester le domaine unitairement sans charger de contexte Spring ou de base de données.

---

## 🏗️ Nouvelle Structure des Dossiers

Chaque microservice a été réorganisé selon cette arborescence stricte :

```text
src/main/java/com/yowyob/[service]/
│
├── domain/                  # CŒUR MÉTIER (Aucune dépendance Spring/JPA/etc.)
│   ├── model/               # Entités métiers et Value Objects purs (POJOs)
│   ├── port/
│   │   ├── in/              # Use Cases (Ce que l'application propose : interfaces)
│   │   └── out/             # SPI (Ce dont l'application a besoin : interfaces de DB, API externes)
│   └── service/             # Implémentation des cas d'usage (implémente les ports IN)
│
├── adapter/                 # INFRASTRUCTURE (Dépend des frameworks)
│   ├── in/                  # Adaptateurs primaires (Pilotes : REST Controllers, Listeners RabbitMQ)
│   │   ├── rest/
│   │   └── event/
│   └── out/                 # Adaptateurs secondaires (Pilotés : JPA, WebClient, Elasticsearch)
│       ├── persistence/
│       ├── http/
│       └── messaging/
│
├── config/                  # Configuration Spring (Beans, Security, Swagger)
└── dto/                     # Objets de transfert de données (Contrats API)
```

---

## 🚀 Étapes de la Migration (Service par Service)

La migration s'est déroulée de manière itérative, sans casser les contrats d'interface (les DTOs exposés au frontend sont restés identiques).

### 1. Extraction du Modèle de Domaine
Pour chaque service, nous avons commencé par extraire les entités JPA (`@Entity`) ou Elasticsearch (`@Document`) pour créer des **modèles de domaine purs**.
*Exemple : L'entité `ListingJpaEntity` a été vidée de ses annotations pour devenir le POJO `Listing` dans le package `domain/model`.*

### 2. Définition des Ports (Interfaces)
- **Ports IN** : Création d'interfaces représentant les cas d'usage (ex: `ListingUseCase`, `AuthUseCase`).
- **Ports OUT** : Création d'interfaces pour la persistance et les appels externes (ex: `ListingRepository`, `GeoLocationPort`).

### 3. Création des Services Applicatifs
Déplacement de la logique métier depuis les anciens `[Name]Service` vers les `[Name]ApplicationService` situés dans `domain/service/`.
Ces services n'interagissent qu'avec des interfaces (les ports de sortie), ce qui les rend agnostiques vis-à-vis de la base de données.

### 4. Implémentation des Adaptateurs (Adapters)
- **Adaptateurs IN (REST/Events)** : Les anciens `Controllers` ont été transformés en `RestAdapter`. Ils interceptent les requêtes HTTP et appellent le `UseCase` (Port IN).
- **Adaptateurs OUT (Persistance/HTTP)** : Création des adaptateurs qui implémentent les ports de sortie. 
  *Exemple : `ListingJpaAdapter` implémente `ListingRepository` du domaine. Il fait appel à Spring Data JPA en interne.*

### 5. Création des Mappers
Parce que le domaine et l'infrastructure sont séparés, nous avons créé des Mappers explicites (`ListingMapper`, `UserMapper`) pour convertir les objets du Domaine en Entités JPA (ou Documents Elasticsearch), et inversement.

---

## 🛠️ Particularités par Microservice

### `listing-service`
- Transformation complète des listings et des avis (`Reviews`). 
- Isolation de l'envoi d'événements RabbitMQ via le port `ListingEventPublisher`.

### `user-service`
- Création de `UserProfile` et `SearchHistory` en tant que modèles de domaine.
- L'historique de recherche n'est plus couplé à Spring Data au niveau du domaine métier.

### `auth-service`
- Service le plus sensible. L'authentification (Spring Security, JWT, Google OAuth) a été reléguée dans la couche adaptateur `adapter/out/security` et `adapter/out/http`.
- Le domaine manipule uniquement un `AuthUser`. Les événements Kafka de création de compte passent par le port `EventPublisherPort`.

### `geo-service`
- La logique mathématique de calcul de distance (Formule de Haversine) a été rapatriée dans le domaine (`GeoApplicationService`).
- Les appels aux API externes (Nominatim, ip-api) et le cache Redis ont été masqués derrière des adaptateurs HTTP purs.

### `search-service`
- Le parser complexe `KeywordParser`, qui contient la logique sémantique de recherche, a été identifié comme de la logique métier pure et déplacé dans `domain/service/`.
- Elasticsearch a été isolé dans `ElasticsearchProductAdapter`.

---

## 🧹 Le Grand Nettoyage

Une fois que tous les flux passaient correctement par le trio `Adaptateur IN -> Domaine -> Adaptateur OUT`, les anciens dossiers de l'architecture en couches ont été définitivement supprimés (`rm -rf`) pour éviter toute confusion technique et tout conflit de Beans Spring :
- `controller/`
- `service/` (anciens services Spring)
- `repository/` (interfaces Spring Data)
- `entity/` / `document/`

**Résultat final** : Tous les microservices ont été compilés avec succès (`mvn clean compile`). Le code est désormais pérenne, lisible et strictement aligné sur les principes Clean Architecture.
