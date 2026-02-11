# 🚀 Guide de Démarrage Rapide

## Prérequis
- Java 17+
- Maven 3.6+

## Étapes

### 1. Compiler le projet
```bash
cd yowyob-crawler-reconfigured
mvn clean package -DskipTests
```

### 2. Lancer l'application
```bash
mvn spring-boot:run
```

Le crawler démarre sur **http://localhost:8086**

### 3. Tester

**Test simple:**
```bash
curl http://localhost:8086/api/crawler/test
```

**Crawler une URL:**
```bash
curl -X POST "http://localhost:8086/api/crawler/crawl?url=http://example.com"
```

**Avec le backend de test (port 3000):**
```bash
# Dans un autre terminal, lancer d'abord le backend de test
cd test-backend-springboot
mvn spring-boot:run

# Puis tester le crawling
curl -X POST "http://localhost:8086/api/crawler/crawl?url=http://localhost:3000/"
```

### 4. Lancer les tests automatiques
```bash
./test-crawler.sh
```

## URLs de test

### Backend local (port 3000)
- Page d'accueil: `http://localhost:3000/`
- Produit: `http://localhost:3000/product/1`
- Catégorie: `http://localhost:3000/category/electronics`
- API JSON: `http://localhost:3000/api/products`

### Sites publics
- Site simple: `http://example.com`
- Site e-commerce: `https://www.jumia.cm/` (si accessible)

## Endpoints du crawler

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/crawler/health` | GET | Vérifier l'état du service |
| `/api/crawler/test` | GET | Test simple |
| `/api/crawler/crawl?url=<URL>` | POST | Crawler une URL |
| `/api/crawler/stats` | GET | Statistiques |

## Exemple de réponse

```json
{
  "url": "http://localhost:3000/",
  "success": true,
  "httpStatusCode": 200,
  "itemsFound": 8,
  "extractedData": {
    "title": "Boutique Test E-commerce",
    "products": [
      {
        "name": "Téléphone Samsung",
        "price": "250000 FCFA"
      }
    ]
  },
  "durationMs": 1234
}
```

## Problèmes courants

**Port déjà utilisé:**
```bash
# Changer le port dans src/main/resources/application.properties.yml
server:
  port: 8087  # Au lieu de 8086
```

**Timeout:**
```bash
# Augmenter le timeout dans application.properties.yml
crawler:
  timeout: 30000  # 30 secondes
```

## Next Steps

- Ajoutez vos propres extracteurs dans `src/main/java/com/yowyob/crawler/extractor/`
- Personnalisez la configuration dans `application.yml`
- Consultez le README.md complet pour plus de détails
