# Blog Platform - Applications

Une plateforme de blog moderne construite avec une architecture microservices, utilisant React pour le frontend, Spring Boot pour le backend, et PostgreSQL comme base de données.

## Table des matières

- [Architecture Générale](#architecture-générale)
- [Frontend](#frontend)
- [Backend](#backend)
- [Base de Données](#base-de-données)
- [Clean Architecture](#clean-architecture)
- [Démarrage Rapide](#démarrage-rapide)

---

## Architecture Générale

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│    Frontend     │────▶│    Backend      │────▶│   PostgreSQL    │
│   (React 19)    │     │  (Spring Boot)  │     │      (v15)      │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
      :80                    :8080                   :5432
```

| Composant | Technologie | Description |
|-----------|-------------|-------------|
| Frontend | React 19 + TypeScript | Interface utilisateur moderne et réactive |
| Backend | Spring Boot 3.4.1 + WebFlux | API REST réactive avec Clean Architecture |
| Database | PostgreSQL 15 | Stockage relationnel des données |

---

## Frontend

### Technologies

| Technologie | Version | Rôle |
|-------------|---------|------|
| React | 19.2.0 | Framework UI |
| TypeScript | 5.9.3 | Typage statique |
| Vite | 7.2.4 | Build tool & dev server |
| React Router | 7.12.0 | Routage client |
| Zustand | 5.0.10 | Gestion d'état global |
| TanStack Query | 5.90.19 | Gestion des données serveur |
| Tailwind CSS | 3.4.19 | Framework CSS utilitaire |
| Lucide React | 0.562.0 | Bibliothèque d'icônes |

### Structure du Projet

```
frontend-ui/
├── src/
│   ├── api/                    # Repositories API
│   │   ├── articles.ts         # Endpoints articles
│   │   └── auth.ts             # Endpoints authentification
│   │
│   ├── components/
│   │   ├── ui/                 # Composants réutilisables (Button, Image)
│   │   ├── layout/             # Header, Footer, Hero, AdminLayout
│   │   └── articles/           # Composants spécifiques aux articles
│   │
│   ├── hooks/
│   │   └── useArticles.ts      # Hook pour infinite scroll
│   │
│   ├── pages/
│   │   ├── HomePage.tsx
│   │   ├── ArticlesPage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── admin/              # Pages d'administration
│   │
│   ├── store/
│   │   ├── authStore.ts        # État d'authentification (Zustand)
│   │   └── uiStore.ts          # État UI
│   │
│   ├── types/                  # Interfaces TypeScript
│   └── lib/                    # Utilitaires
│
├── nginx.conf                  # Configuration reverse proxy
├── Dockerfile                  # Build multi-stage
└── package.json
```

### Gestion d'État

**Zustand** est utilisé pour la gestion d'état globale avec deux stores principaux :

```typescript
// authStore - État d'authentification (persisté dans localStorage)
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}
```

**React Query** gère l'état serveur avec :
- Cache automatique des requêtes
- Invalidation intelligente
- Infinite queries pour la pagination

### Routes

| Route | Page | Accès |
|-------|------|-------|
| `/` | Accueil | Public |
| `/articles` | Liste des articles | Public |
| `/login` | Connexion | Public |
| `/register` | Inscription | Public |
| `/admin` | Dashboard admin | Admin uniquement |
| `/admin/articles` | Gestion articles | Admin uniquement |
| `/admin/users` | Gestion utilisateurs | Admin uniquement |

---

## Backend

### Technologies

| Technologie | Version | Rôle |
|-------------|---------|------|
| Spring Boot | 3.4.1 | Framework principal |
| Java | 21 | Langage (LTS) |
| Spring WebFlux | - | Web réactif non-bloquant |
| R2DBC | - | Accès DB réactif |
| Spring Security | - | Sécurité & authentification |
| JWT (JJWT) | 0.12.6 | Tokens d'authentification |
| MapStruct | 1.6.3 | Mapping DTO automatique |
| Flyway | - | Migrations de base de données |
| SpringDoc OpenAPI | 2.8.3 | Documentation API (Swagger) |

### Structure du Projet (Clean Architecture)

```
spring-api/src/main/java/sn/noreyni/springapi/
│
├── domain/                          # Couche Domaine
│   ├── model/                       # Entités métier pures
│   │   ├── Article.java
│   │   ├── User.java
│   │   ├── Comment.java
│   │   ├── Tag.java
│   │   └── ArticleStatus.java
│   └── repository/                  # Interfaces des repositories
│
├── application/                     # Couche Application
│   ├── facade/                      # Orchestration des use cases
│   │   ├── ArticleFacade.java
│   │   ├── UserFacade.java
│   │   ├── AuthFacade.java
│   │   └── CommentFacade.java
│   │
│   ├── usecase/                     # Cas d'utilisation (CQRS)
│   │   ├── article/
│   │   │   ├── command/             # Écriture
│   │   │   │   ├── CreateArticleCommand.java
│   │   │   │   └── DeleteArticleCommand.java
│   │   │   └── query/               # Lecture
│   │   │       ├── GetArticleByIdQuery.java
│   │   │       └── GetArticleListQuery.java
│   │   ├── user/
│   │   │   ├── command/
│   │   │   └── query/
│   │   └── comment/
│   │       ├── command/
│   │       └── query/
│   │
│   ├── dto/                         # Data Transfer Objects
│   ├── mapper/                      # MapStruct mappers
│   └── service/                     # Services applicatifs
│
├── infrastructure/                  # Couche Infrastructure
│   ├── config/                      # Configurations Spring
│   │   ├── SecurityConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── DataInitializer.java
│   │
│   ├── security/                    # Sécurité JWT
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── ReactiveUserDetailsServiceImpl.java
│   │
│   ├── persistence/
│   │   ├── repository/              # Implémentations R2DBC
│   │   └── entity/                  # Entités de persistence
│   │
│   └── exception/                   # Gestion des erreurs
│
└── web/                             # Couche Présentation
    ├── controller/                  # Contrôleurs REST
    │   ├── ArticleController.java
    │   ├── AuthController.java
    │   ├── UserController.java
    │   └── CommentController.java
    ├── request/                     # DTOs de requête
    └── response/                    # DTOs de réponse
```

### Endpoints API

#### Authentification

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |

#### Articles

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/articles` | Liste paginée des articles |
| GET | `/api/articles/{id}` | Détails d'un article |
| POST | `/api/articles` | Créer un article (auth) |
| DELETE | `/api/articles/{id}` | Supprimer un article (auth) |
| GET | `/api/articles/author/{authorId}` | Articles par auteur |

#### Utilisateurs & Commentaires

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/users` | Liste des utilisateurs |
| GET | `/api/comments/article/{id}` | Commentaires d'un article |
| POST | `/api/comments` | Ajouter un commentaire |

#### Documentation & Monitoring

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Documentation OpenAPI |
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Métriques Prometheus |

### Programmation Réactive

Le backend utilise **Spring WebFlux** pour une programmation entièrement réactive et non-bloquante :

```java
// Exemple de controller réactif
@GetMapping("/{id}")
public Mono<ArticleResponse> getArticle(@PathVariable Long id) {
    return articleFacade.getArticleById(id);
}

@GetMapping
public Flux<ArticleResponse> getAllArticles(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return articleFacade.getAllArticles(page, size);
}
```

**Avantages :**
- Meilleure utilisation des ressources
- Scalabilité accrue
- Gestion efficace des connexions simultanées

---

## Base de Données

### PostgreSQL 15

PostgreSQL est utilisé comme système de gestion de base de données relationnelle principal.

### Schéma de la Base de Données

```sql
┌─────────────────────────────────────────────────────────────────┐
│                           USERS                                  │
├─────────────────────────────────────────────────────────────────┤
│ id (PK)  │ username │ email │ password │ role │ created_at      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          ARTICLES                                │
├─────────────────────────────────────────────────────────────────┤
│ id (PK) │ title │ content │ imageUrl │ status │ authorId (FK)   │
│ views   │ likes │ created_at │ updated_at                       │
└─────────────────────────────────────────────────────────────────┘
        │                                   │
        │ 1:N                               │ N:M
        ▼                                   ▼
┌───────────────────┐             ┌─────────────────────┐
│     COMMENTS      │             │    ARTICLE_TAGS     │
├───────────────────┤             ├─────────────────────┤
│ id (PK)           │             │ article_id (FK)     │
│ content           │             │ tag_id (FK)         │
│ authorId (FK)     │             └─────────────────────┘
│ articleId (FK)    │                       │
│ created_at        │                       │
└───────────────────┘                       ▼
                                  ┌─────────────────────┐
                                  │        TAGS         │
                                  ├─────────────────────┤
                                  │ id (PK)             │
                                  │ name                │
                                  └─────────────────────┘
```

### Migrations Flyway

Les migrations sont gérées automatiquement par Flyway :

| Version | Description |
|---------|-------------|
| V1 | Schéma initial (users, articles, comments) |
| V2 | Index de performance |
| V3 | Colonne imageUrl |
| V4 | Colonne status (DRAFT/PUBLISHED) |
| V5 | Views, likes et support des tags |

Les fichiers de migration se trouvent dans :
```
spring-api/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_indexes.sql
├── V3__add_image_url.sql
├── V4__add_article_status.sql
└── V5__add_views_likes_tags.sql
```

### Connexion

Le backend utilise deux types de connexion :

| Type | Usage | Driver |
|------|-------|--------|
| R2DBC | Opérations réactives | r2dbc-postgresql |
| JDBC | Migrations Flyway | postgresql |

---

## Clean Architecture

### Principes Appliqués

L'application suit les principes de la **Clean Architecture** (Uncle Bob) pour garantir une séparation claire des responsabilités et une testabilité maximale.

```
                    ┌─────────────────────────┐
                    │                         │
                    │      Frameworks &       │
                    │       Drivers           │
                    │   (Web, DB, External)   │
                    │                         │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │                         │
                    │   Interface Adapters    │
                    │  (Controllers, Repos)   │
                    │                         │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │                         │
                    │   Application Layer     │
                    │   (Use Cases, DTOs)     │
                    │                         │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │                         │
                    │      Domain Layer       │
                    │   (Entities, Rules)     │
                    │                         │
                    └─────────────────────────┘
```

### Couches et Responsabilités

#### 1. Couche Domaine (`domain/`)

La couche la plus interne, sans dépendances externes.

```java
// Entité métier pure
public class Article {
    private Long id;
    private String title;
    private String content;
    private ArticleStatus status;
    private Long authorId;
    private Integer views;
    private Integer likes;
    // Logique métier uniquement
}
```

**Contient :**
- Entités métier (Article, User, Comment, Tag)
- Interfaces des repositories
- Règles métier pures

#### 2. Couche Application (`application/`)

Orchestre les cas d'utilisation et coordonne le flux de données.

```java
// Facade qui coordonne les use cases
@Service
public class ArticleFacade {
    private final CreateArticleCommand createCommand;
    private final GetArticleListQuery listQuery;

    public Mono<ArticleResponse> createArticle(CreateArticleRequest request) {
        return createCommand.execute(request);
    }
}
```

**Contient :**
- Facades (points d'entrée pour les contrôleurs)
- Use Cases (Commands & Queries - pattern CQRS)
- DTOs (Data Transfer Objects)
- Mappers (MapStruct)

#### 3. Couche Infrastructure (`infrastructure/`)

Implémente les détails techniques et les intégrations externes.

```java
// Implémentation du repository avec R2DBC
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    private final R2dbcArticleRepository r2dbcRepository;

    @Override
    public Mono<Article> findById(Long id) {
        return r2dbcRepository.findById(id)
            .map(this::toDomain);
    }
}
```

**Contient :**
- Configuration Spring (Security, OpenAPI)
- Implémentations des repositories (R2DBC)
- Services de sécurité (JWT)
- Gestion des exceptions

#### 4. Couche Web (`web/`)

Gère les interactions HTTP.

```java
@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleFacade articleFacade;

    @PostMapping
    public Mono<ArticleResponse> create(@RequestBody CreateArticleRequest request) {
        return articleFacade.createArticle(request);
    }
}
```

**Contient :**
- Contrôleurs REST
- DTOs de requête/réponse
- Validation des entrées

### Pattern CQRS

L'application implémente un pattern **CQRS simplifié** (Command Query Responsibility Segregation) :

```
┌─────────────────────────────────────────────────────────────────┐
│                         USE CASES                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Commands (Écriture)           Queries (Lecture)               │
│   ├── CreateArticleCommand      ├── GetArticleByIdQuery         │
│   ├── DeleteArticleCommand      ├── GetArticleListQuery         │
│   ├── CreateUserCommand         ├── GetArticlesByAuthorQuery    │
│   ├── UpdateUserCommand         └── LoginQuery                  │
│   └── AddCommentCommand                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Flux des Dépendances

```
Web (Controller) ──▶ Application (Facade) ──▶ Domain (Model)
                              │                    ▲
                              ▼                    │
                    Infrastructure (Repository) ───┘
```

**Règle fondamentale :** Les dépendances pointent toujours vers l'intérieur (vers le domaine).

### Avantages de cette Architecture

| Avantage | Description |
|----------|-------------|
| **Testabilité** | Chaque couche peut être testée indépendamment |
| **Maintenabilité** | Modifications isolées sans effets de bord |
| **Flexibilité** | Facile de changer de framework ou de base de données |
| **Lisibilité** | Structure claire et prévisible |
| **Évolutivité** | Ajout de fonctionnalités sans refactoring majeur |

---

## Démarrage Rapide

### Prérequis

- Docker & Docker Compose
- Node.js 20+ (pour le développement frontend)
- Java 21+ (pour le développement backend)
- Maven 3.9+

### Lancement avec Docker Compose

```bash
# Depuis le dossier apps/
docker-compose up -d

# L'application sera disponible sur :
# Frontend : http://localhost
# Backend API : http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui.html
```

### Développement Local

**Backend :**
```bash
cd spring-api
./mvnw spring-boot:run
```

**Frontend :**
```bash
cd frontend-ui
npm install
npm run dev
```

### Variables d'Environnement

**Backend (`application.yml`) :**
```yaml
DB_HOST: localhost
DB_PORT: 5432
DB_NAME: blogdb
DB_USER: postgres
DB_PASSWORD: postgres
JWT_SECRET: your-secret-key
```

**Frontend (`.env`) :**
```env
VITE_API_URL=http://localhost:8080/api
```

---

## Structure des Dossiers

```
apps/
├── docker-compose.yml          # Orchestration locale
├── README.md                   # Ce fichier
│
├── frontend-ui/                # Application React
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
└── spring-api/                 # Application Spring Boot
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   └── resources/
    │   │       └── db/migration/
    │   └── test/
    ├── Dockerfile
    └── pom.xml
```

---

## Auteur

Développé avec la Clean Architecture pour une application moderne, scalable et maintenable.
