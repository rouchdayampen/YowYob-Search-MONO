# 📘 MANUEL DE L'UTILISATEUR - YOWYOB SEARCH ENGINE

**Version :** 1.0  
**Date :** 05 Février 2026  
**Produit :** YowYob Search Engine  

---

## 📑 Table des Matières

1. [Introduction](#1-introduction)
2. [Accès et Démarrage](#2-accès-et-démarrage)
3. [Interface Utilisateur](#3-interface-utilisateur)
4. [Guide de Recherche Pas-à-Pas](#4-guide-de-recherche-pas-à-pas)
5. [Consultation des Détails](#5-consultation-des-détails)
6. [Dépannage et FAQ](#6-dépannage-et-faq)

---

<div style="page-break-after: always;"></div>

## 1. Introduction

Bienvenue sur **YowYob Search Engine**, votre moteur de recherche centralisé pour les petites annonces au Cameroun. 

**Ce que YowYob fait pour vous :**
*   **Recherche centralisée :** Plus besoin de visiter 10 sites différents (Jumia, OLX, etc.). YowYob regroupe tout.
*   **Géolocalisation :** Trouvez des produits proches de chez vous.
*   **Rapidité :** Une recherche instantanée grâce à notre technologie avancée.

---

## 2. Accès et Démarrage

### Prérequis
*   Un navigateur web moderne (Chrome, Firefox, Safari, Edge).
*   Une connexion internet.

### Accéder à l'application
L'application est accessible via votre navigateur à l'adresse suivante (une fois déployée) :
> **http://localhost:3000** (Environnement de test)

À l'ouverture, vous arrivez directement sur la page d'accueil simplifiée, conçue comme un moteur de recherche (style Google).

---

<div style="page-break-after: always;"></div>

## 3. Interface Utilisateur

### 🏠 Page d'Accueil

L'interface est épurée pour se concentrer sur l'essentiel : la recherche.

**Éléments clés :**
1.  **Barre de Recherche Centrale :** Tapez ce que vous cherchez (ex: "iPhone 15", "Appartement Makepe").
2.  **Bouton "Rechercher" :** Lance la requête.
3.  **Filtres Rapides (Optionnel) :** Boutons pour filtrer par catégorie (Immobilier, Véhicules, Électronique...).

*(Note : Imaginez ici une capture d'écran montrant une barre de recherche centrée sur fond minimaliste)*

### 🔍 Page de Résultats

Une fois la recherche lancée, vous voyez :
*   **Liste des résultats :** Chaque annonce affiche une image, le titre, le prix et la ville.
*   **Carte Interactive :** Une carte à droite (ou en bas sur mobile) montrant la position des articles.

---

## 4. Guide de Recherche Pas-à-Pas

### Scénario : Je cherche une voiture à Douala

1.  **Saisissez vos mots-clés :**
    Dans la barre de recherche, tapez : `Toyota Corolla Douala`.
    
2.  **Validez :**
    Appuyez sur `Entrée` ou cliquez sur la loupe.

3.  **Analysez les résultats :**
    Le système va scanner toutes les sources disponibles.
    
    ```mermaid
    graph LR
        A[Utilisateur] -->|Recherche 'Toyota'| B(YowYob)
        B -->|Scan| C[OLX]
        B -->|Scan| D[Jumia]
        B -->|Recherche| E[Base Locale]
        C & D & E -->|Résultats| F[Liste Unifiée]
    ```

4.  **Affinez (si nécessaire) :**
    Utilisez les filtres pour ne voir que la catégorie "Véhicules" si des accessoires apparaissent.

---

<div style="page-break-after: always;"></div>

## 5. Consultation des Détails

### Voir une annonce

Cliquez sur n'importe quel résultat pour ouvrir la **Fiche Détail**.

**Ce que vous y trouverez :**
*   **Prix :** Mis en évidence.
*   **Description complète :** Telle que fournie par le vendeur original.
*   **Localisation Précise :**
    *   Adresse écrite (ex: "Bonanjo, Douala").
    *   **Position GPS :** Un marqueur sur la carte vous permet de voir exactement où se trouve l'objet pour planifier votre trajet.

### Exemple de Fiche Produit

| Information | Exemple |
| :--- | :--- |
| **Titre** | iPhone 13 Pro Max 256GB |
| **Prix** | 450,000 FCFA |
| **Vendeur** | Pro Mobile Douala |
| **Source** | Jumia (Vérifié) |
| **Localisation** | Akwa, Douala |

---

## 6. Dépannage et FAQ

**Q : Je ne trouve aucun résultat.**
*   **R :** Essayez des termes plus généraux. Au lieu de "Toyota Corolla 2018 Grise", essayez juste "Toyota Corolla".

**Q : Les prix sont-ils à jour ?**
*   **R :** Oui, YowYob actualise les prix toutes les heures pour garantir la fiabilité.

**Q : Comment contacter le vendeur ?**
*   **R :** Sur la fiche détail, cliquez sur le bouton "Voir l'annonce originale" ou "Contacter" pour être redirigé vers la plateforme source ou voir le numéro.

---
*© 2026 YowYob Inc. - Tous droits réservés.*
