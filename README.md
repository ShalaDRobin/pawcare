# 🐾 PawCare — Plateforme d'adoption et protection animale

Application web Spring Boot dédiée à l'adoption d'animaux, au signalement d'animaux perdus, à la sensibilisation et au forum communautaire.

---

## 🛠️ Stack technique

| Couche | Technologie |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Vue / Templates | Thymeleaf + HTML/CSS/JS vanilla |
| Sécurité | Spring Security 6 |
| Base de données | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Utilitaire | Lombok |
| IDE recommandé | IntelliJ IDEA |

---

## ⚙️ Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Java 17** (pas 21, pas 11 — exactement 17)
- **Maven 3.8+**
- **MySQL 8.x**
- **IntelliJ IDEA** (Community ou Ultimate)

---

## 🚀 Installation pas à pas

### 1. Cloner le projet

```bash
git clone https://github.com/TON_USERNAME/pawcare.git
cd pawcare
```

### 2. Créer la base de données MySQL

Ouvre MySQL Workbench ou ton terminal MySQL et exécute :

```sql
CREATE DATABASE pawcare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> Le mot de passe MySQL utilisé dans ce projet est **`sarah`** (compte `root`).
> C'est une application de test — ne jamais utiliser ce mot de passe en production.

### 3. Vérifier `application.properties`

Le fichier `src/main/resources/application.properties` est déjà configuré :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pawcare_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=sarah
spring.jpa.hibernate.ddl-auto=update
```

> `ddl-auto=update` signifie que Hibernate crée/met à jour les tables automatiquement au démarrage. Tu n'as **pas besoin** d'importer un fichier SQL.

### 4. Ouvrir dans IntelliJ IDEA

1. **File → Open** → sélectionne le dossier `pawcare`
2. IntelliJ détecte automatiquement le `pom.xml` Maven → clique **"Load Maven Project"** si la popup apparaît
3. Attends que l'indexation et le téléchargement des dépendances soient terminés (barre de progression en bas)

### 5. Configurer le SDK Java 17 dans IntelliJ

Si IntelliJ affiche des erreurs rouges sur le projet :

1. **File → Project Structure → Project**
2. **SDK** → sélectionne ou ajoute **Java 17**
3. **Language Level** → mettre **17**
4. Clique **Apply → OK**

### 6. Activer Lombok (important !)

Lombok génère automatiquement les getters/setters/constructeurs. Sans lui, IntelliJ affiche des fausses erreurs rouges.

1. **File → Settings → Plugins** → cherche **Lombok** → installer si absent
2. **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
3. Cocher **"Enable annotation processing"**
4. Cliquer **Apply → OK**

> ⚠️ Sans cette étape, tous les modèles (`ForumPost`, `User`, etc.) semblent avoir des erreurs rouges alors que le code est correct.

### 7. Lancer l'application

Dans IntelliJ, ouvre `src/main/java/animalplatform/AnimalAdoptionPlatformApplication.java` et clique le bouton ▶️ vert, ou :

```bash
mvn spring-boot:run
```

L'application démarre sur : **http://localhost:8080**

---

## 🔴 Résolution des erreurs rouges dans IntelliJ

Voici les erreurs rouges courantes et leurs solutions :

### ❌ "Cannot resolve symbol" sur les getters/setters (`getTitle()`, `getId()`, etc.)
**Cause :** Lombok pas activé.
**Solution :** Étape 6 ci-dessus (activer annotation processing).

### ❌ "Cannot resolve symbol 'Page'" ou imports Spring manquants
**Cause :** Maven n'a pas téléchargé les dépendances.
**Solution :** Clic droit sur `pom.xml` → **Maven → Reload Project**

### ❌ "SDK not defined" ou "Module SDK is not defined"
**Cause :** Java 17 pas configuré dans IntelliJ.
**Solution :** Étape 5 ci-dessus (Project Structure → SDK 17).

### ❌ "Table doesn't exist" au démarrage
**Cause :** La base de données `pawcare_db` n'a pas été créée.
**Solution :** Étape 2 — créer la base manuellement dans MySQL.

### ❌ "Access denied for user 'root'@'localhost'"
**Cause :** Le mot de passe MySQL ne correspond pas.
**Solution :** Modifier `spring.datasource.password` dans `application.properties` pour mettre ton mot de passe MySQL local.

### ❌ "Port 8080 already in use"
**Cause :** Une autre appli tourne déjà sur le port 8080.
**Solution :** Changer le port dans `application.properties` : `server.port=8081`

---

## 🗂️ Structure du projet

```
src/main/
├── java/animalplatform/
│   ├── controller/          ← Contrôleurs Spring MVC (HTTP)
│   ├── model/               ← Entités JPA (tables BDD)
│   ├── repository/          ← Interfaces Spring Data (requêtes BDD)
│   ├── service/             ← Logique métier
│   └── dto/                 ← Objets de transfert de données
│
└── resources/
    ├── templates/pawcare/
    │   ├── index.html           ← Page d'accueil principale
    │   ├── components/          ← Sections réutilisables (forum, hero, etc.)
    │   ├── pages/               ← Pages dédiées (forum-post, categorie...)
    │   └── fragments/           ← Navbar, footer, modales, toast
    ├── static/
    │   ├── css/                 ← Feuilles de style
    │   └── js/                  ← Scripts (forum.js, pawcare-main.js)
    └── application.properties   ← Configuration Spring Boot
```

---

## 📖 Fonctionnement des sections

### 🔐 Authentification (Login / Inscription)

**Fichiers clés :**
- `controller/AuthController.java`
- `templates/pawcare/fragments/auth-modals.html`
- `model/User.java`, `service/UserService.java`

**Fonctionnement :**
- La connexion et l'inscription se font via des **modales** qui s'ouvrent sur la page d'accueil (`openModal('login')` / `openModal('signup')`)
- L'utilisateur connecté est stocké dans la **session HTTP** : `session.setAttribute("user", user)`
- Dans les templates Thymeleaf, on vérifie la session avec `${session.user != null}`
- Spring Security est configuré pour sécuriser les routes — seuls les utilisateurs connectés peuvent poster, commenter, etc.
- La déconnexion se fait via `GET /auth/logout` qui invalide la session

**Routes :**
```
POST /auth/login       → connexion
POST /auth/register    → inscription
GET  /auth/logout      → déconnexion
```

---

### 🐾 Adoption

**Fichiers clés :**
- `controller/AdoptionController.java`
- `service/AdoptionService.java`
- `model/AdoptionPost.java`
- `templates/pawcare/components/adoption-section.html`

**Fonctionnement :**
- Les annonces d'adoption sont des `AdoptionPost` liés à un `User`
- La page d'accueil affiche les 6 dernières annonces via `adoptionService.getRecentPosts(6)`
- Les images uploadées sont stockées dans le dossier `uploads/` et servies comme ressources statiques
- Upload limité à **10 MB** par fichier (`spring.servlet.multipart.max-file-size=10MB`)

---

### 🔍 Animaux perdus

**Fichiers clés :**
- `controller/LostAnimalController.java`
- `model/LostAnimalPost.java`
- `templates/pawcare/components/lost-animals-section.html`

**Fonctionnement :**
- Système de signalement d'animaux perdus ou trouvés
- Chaque `LostAnimalPost` a un statut (`LOST` / `FOUND`)
- La page d'accueil affiche les 4 derniers signalements

---

### 💚 Sensibilisation

**Fichiers clés :**
- `controller/SensibilisationController.java`
- `service/SensibilisationService.java`
- `model/Sensibilisation.java`
- `repository/SensibilisationRepository.java`
- `templates/pawcare/components/sensibilisation-section.html`
- `templates/pawcare/pages/sensibilisation-list.html`

**Fonctionnement :**
- Les articles de sensibilisation sont créés par les utilisateurs connectés
- Sur la page d'accueil : carrousel des articles récents avec défilement automatique
- Bouton **"Voir tous les articles"** → redirige vers `/sensibilisation` qui liste tous les articles **du plus récent au plus ancien** (`findByIsPublishedTrueOrderByPublishedAtDesc`)
- La page liste supporte un filtre par **catégorie** (`GET /sensibilisation?category=Soins`)

**Routes :**
```
GET  /sensibilisation              → liste complète (plus récent → plus ancien)
GET  /sensibilisation?category=X  → filtrée par catégorie
GET  /sensibilisation/{id}         → détail d'un article
GET  /sensibilisation/create       → formulaire de création (connecté)
POST /sensibilisation/create       → soumettre un article
POST /sensibilisation/{id}/like    → liker un article
```

---

### 💬 Forum & Catégories

**Fichiers clés :**
- `controller/ForumController.java`
- `service/ForumService.java`
- `model/ForumPost.java`, `model/ForumCategory.java`, `model/ForumComment.java`
- `repository/ForumPostRepository.java`, `ForumCategoryRepository.java`, `ForumCommentRepository.java`
- `templates/pawcare/components/forum-section.html` → section sur la page d'accueil
- `templates/pawcare/pages/forum-post.html` → page de détail d'un post
- `templates/pawcare/pages/forum-category.html` → page filtrée par catégorie

**Fonctionnement général :**

Le forum est composé de 3 niveaux :
1. **Catégories** (`ForumCategory`) — ex : Chiens, Chats, Santé & Soins
2. **Posts** (`ForumPost`) — sujets de discussion, liés à une catégorie et un utilisateur
3. **Commentaires** (`ForumComment`) — réponses à un post, avec support de réponses imbriquées (un commentaire peut avoir un `parentComment`)

**Section accueil (`forum-section.html`) :**
- Sidebar affiche toutes les catégories avec leur nombre de posts (injectées via `HomeController` → `categories`)
- Cliquer sur une catégorie redirige vers `GET /forum/categorie/{id}`
- Le fil principal affiche les 4 derniers posts (`recentForumPosts`)

**Page catégorie (`/forum/categorie/{id}`) :**
- Liste tous les posts de la catégorie, paginés par 20
- Sidebar reste visible avec toutes les catégories (`allCategories`)
- La catégorie active est mise en surbrillance

**Page post (`/forum/post/{id}`) :**
- Affiche le post complet avec ses commentaires
- Les variables de session sont injectées en JavaScript via Thymeleaf (`th:inline="javascript"`) : `postId`, `isAuthenticated`, `isLocked`
- Le formulaire de commentaire appelle `POST /forum/post/{id}/commenter` en fetch (AJAX)
- Les réponses imbriquées appellent `POST /forum/post/{id}/comment/{commentId}/repondre`
- Après succès, la page se recharge pour afficher le nouveau commentaire

**Routes forum :**
```
GET  /forum/categorie/{id}                          → posts filtrés par catégorie
GET  /forum/post/{id}                               → détail d'un post
GET  /forum/creer                                   → formulaire nouveau post
POST /forum/creer                                   → soumettre un post
POST /forum/post/{id}/commenter                     → ajouter un commentaire (JSON)
POST /forum/post/{id}/comment/{cid}/repondre        → répondre à un commentaire (JSON)
POST /forum/post/{id}/liker                         → liker un post (JSON)
POST /forum/comment/{id}/liker                      → liker un commentaire (JSON)
POST /forum/post/{id}/modifier                      → modifier un post
POST /forum/post/{id}/supprimer                     → supprimer un post (JSON)
POST /forum/post/{id}/epingler                      → épingler (admin)
POST /forum/post/{id}/verrouiller                   → verrouiller (admin)
```

> **Important :** Les routes `/commenter` et `/liker` retournent du **JSON** (`@ResponseBody`), pas une redirection. Le JS de la page gère la réponse et recharge la page si succès.

---

## 🗄️ Base de données

Les tables sont créées automatiquement par Hibernate au premier démarrage (`ddl-auto=update`). Voici les tables principales :

| Table | Description |
|---|---|
| `users` | Comptes utilisateurs |
| `adoption_posts` | Annonces d'adoption |
| `lost_animal_posts` | Signalements d'animaux perdus |
| `sensibilisation` | Articles de sensibilisation |
| `forum_categories` | Catégories du forum |
| `forum_posts` | Sujets de discussion |
| `forum_comments` | Commentaires et réponses |

---

## 📝 Notes pour le développeur suivant

- Les données de test peuvent être insérées manuellement dans MySQL ou via des scripts SQL dans `src/main/resources/data.sql` (à créer)
- Le module **mail** est configuré mais désactivé (les credentials Gmail sont des placeholders) — ne pas s'inquiéter si des warnings apparaissent liés au mail au démarrage
- Le dossier `uploads/` est créé automatiquement à la racine du projet au premier upload d'image
- Les fichiers dans `target/` ne doivent **pas** être commités — vérifier que `.gitignore` exclut bien `target/`

---

## .gitignore recommandé

```gitignore
target/
*.class
*.jar
.idea/
*.iml
uploads/
.DS_Store
application-local.properties
```
