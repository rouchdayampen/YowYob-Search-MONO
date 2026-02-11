# 🌐 Swagger UI - Accès Direct

## ⚡ Ouverture Rapide

**Clique sur le lien qui correspond à ce que tu veux:**

---

## 🎯 Principal - API Gateway (Recommandé)

### ⭐ **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

C'est celui-ci ! 👆

- ✅ Tous les endpoints
- ✅ Authentification
- ✅ Recherche
- ✅ Création d'annonces
- ✅ Gestion des annonces

---

## 🔐 Auth Service (Login)

### **[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)**

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/auth/validate`

---

## 🔍 Search Service (Recherche)

### **[http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)**

- `GET /api/search`
- `GET /api/search/{id}/details`
- `POST /api/search/index`

---

## 📋 Listing Service (Annonces)

### **[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)**

- `POST /api/listings`
- `GET /api/listings/{id}`
- `GET /api/listings`
- `PUT /api/listings/{id}`
- `DELETE /api/listings/{id}`

---

## 🕷️ Crawler Service (Info)

### **[http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)**

- Info sur le crawler
- Logs de scraping
- Statut du service

---

## 📚 Documentation

Pour un guide complet : [GUIDE_SWAGGER.md](GUIDE_SWAGGER.md)

---

**Prêt à tester?** ▶️ Clique sur le lien API Gateway ci-dessus !
