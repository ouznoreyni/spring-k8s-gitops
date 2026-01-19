# Blog Platform - Applications

<p align="center">
  <a href="README.md">Français</a> |
  <strong>English</strong>
</p>

A modern blog platform built with a microservices architecture, using React for the frontend, Spring Boot for the backend, and PostgreSQL as the database.

## Table of Contents

- [General Architecture](#general-architecture)
- [Frontend](#frontend)
- [Backend](#backend)
- [Database](#database)
- [Clean Architecture](#clean-architecture)
- [Quick Start](#quick-start)

---

## General Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│    Frontend     │────▶│    Backend      │────▶│   PostgreSQL    │
│   (React 19)    │     │  (Spring Boot)  │     │      (v15)      │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
      :80                    :8080                   :5432
```

| Component | Technology | Description |
|-----------|------------|-------------|
| Frontend | React 19 + TypeScript | Modern and reactive user interface |
| Backend | Spring Boot 3.4.1 + WebFlux | Reactive REST API with Clean Architecture |
| Database | PostgreSQL 15 | Relational data storage |

---

## Frontend

### Technologies

| Technology | Version | Role |
|------------|---------|------|
| React | 19.2.0 | UI Framework |
| TypeScript | 5.9.3 | Static typing |
| Vite | 7.2.4 | Build tool & dev server |
| React Router | 7.12.0 | Client-side routing |
| Zustand | 5.0.10 | Global state management |
| TanStack Query | 5.90.19 | Server state management |
| Tailwind CSS | 3.4.19 | Utility CSS framework |
| Lucide React | 0.562.0 | Icon library |

### Project Structure

```
frontend-ui/
├── src/
│   ├── api/                    # API Repositories
│   │   ├── articles.ts         # Articles endpoints
│   │   └── auth.ts             # Authentication endpoints
│   │
│   ├── components/
│   │   ├── ui/                 # Reusable components (Button, Image)
│   │   ├── layout/             # Header, Footer, Hero, AdminLayout
│   │   └── articles/           # Article-specific components
│   │
│   ├── hooks/
│   │   └── useArticles.ts      # Hook for infinite scroll
│   │
│   ├── pages/
│   │   ├── HomePage.tsx
│   │   ├── ArticlesPage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── admin/              # Administration pages
│   │
│   ├── store/
│   │   ├── authStore.ts        # Authentication state (Zustand)
│   │   └── uiStore.ts          # UI state
│   │
│   ├── types/                  # TypeScript interfaces
│   └── lib/                    # Utilities
│
├── nginx.conf                  # Reverse proxy configuration
├── Dockerfile                  # Multi-stage build
└── package.json
```

### State Management

**Zustand** is used for global state management with two main stores:

```typescript
// authStore - Authentication state (persisted in localStorage)
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}
```

**React Query** manages server state with:
- Automatic query caching
- Smart invalidation
- Infinite queries for pagination

### Routes

| Route | Page | Access |
|-------|------|--------|
| `/` | Home | Public |
| `/articles` | Article list | Public |
| `/login` | Login | Public |
| `/register` | Registration | Public |
| `/admin` | Admin dashboard | Admin only |
| `/admin/articles` | Article management | Admin only |
| `/admin/users` | User management | Admin only |

---

## Backend

### Technologies

| Technology | Version | Role |
|------------|---------|------|
| Spring Boot | 3.4.1 | Main framework |
| Java | 21 | Language (LTS) |
| Spring WebFlux | - | Non-blocking reactive web |
| R2DBC | - | Reactive DB access |
| Spring Security | - | Security & authentication |
| JWT (JJWT) | 0.12.6 | Authentication tokens |
| MapStruct | 1.6.3 | Automatic DTO mapping |
| Flyway | - | Database migrations |
| SpringDoc OpenAPI | 2.8.3 | API documentation (Swagger) |

### Project Structure (Clean Architecture)

```
spring-api/src/main/java/sn/noreyni/springapi/
│
├── domain/                          # Domain Layer
│   ├── model/                       # Pure business entities
│   │   ├── Article.java
│   │   ├── User.java
│   │   ├── Comment.java
│   │   ├── Tag.java
│   │   └── ArticleStatus.java
│   └── repository/                  # Repository interfaces
│
├── application/                     # Application Layer
│   ├── facade/                      # Use case orchestration
│   │   ├── ArticleFacade.java
│   │   ├── UserFacade.java
│   │   ├── AuthFacade.java
│   │   └── CommentFacade.java
│   │
│   ├── usecase/                     # Use cases (CQRS)
│   │   ├── article/
│   │   │   ├── command/             # Write
│   │   │   │   ├── CreateArticleCommand.java
│   │   │   │   └── DeleteArticleCommand.java
│   │   │   └── query/               # Read
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
│   └── service/                     # Application services
│
├── infrastructure/                  # Infrastructure Layer
│   ├── config/                      # Spring configurations
│   │   ├── SecurityConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── DataInitializer.java
│   │
│   ├── security/                    # JWT Security
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── ReactiveUserDetailsServiceImpl.java
│   │
│   ├── persistence/
│   │   ├── repository/              # R2DBC implementations
│   │   └── entity/                  # Persistence entities
│   │
│   └── exception/                   # Error handling
│
└── web/                             # Presentation Layer
    ├── controller/                  # REST Controllers
    │   ├── ArticleController.java
    │   ├── AuthController.java
    │   ├── UserController.java
    │   └── CommentController.java
    ├── request/                     # Request DTOs
    └── response/                    # Response DTOs
```

### API Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registration |
| POST | `/api/auth/login` | Login |

#### Articles

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/articles` | Paginated article list |
| GET | `/api/articles/{id}` | Article details |
| POST | `/api/articles` | Create article (auth) |
| DELETE | `/api/articles/{id}` | Delete article (auth) |
| GET | `/api/articles/author/{authorId}` | Articles by author |

#### Users & Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | User list |
| GET | `/api/comments/article/{id}` | Article comments |
| POST | `/api/comments` | Add comment |

#### Documentation & Monitoring

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | OpenAPI documentation |
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics |

### Reactive Programming

The backend uses **Spring WebFlux** for fully reactive, non-blocking programming:

```java
// Reactive controller example
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

**Benefits:**
- Better resource utilization
- Increased scalability
- Efficient handling of concurrent connections

---

## Database

### PostgreSQL 15

PostgreSQL is used as the primary relational database management system.

### Database Schema

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

### Flyway Migrations

Migrations are managed automatically by Flyway:

| Version | Description |
|---------|-------------|
| V1 | Initial schema (users, articles, comments) |
| V2 | Performance indexes |
| V3 | imageUrl column |
| V4 | status column (DRAFT/PUBLISHED) |
| V5 | Views, likes and tag support |

Migration files are located in:
```
spring-api/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_indexes.sql
├── V3__add_image_url.sql
├── V4__add_article_status.sql
└── V5__add_views_likes_tags.sql
```

### Connection

The backend uses two connection types:

| Type | Usage | Driver |
|------|-------|--------|
| R2DBC | Reactive operations | r2dbc-postgresql |
| JDBC | Flyway migrations | postgresql |

---

## Clean Architecture

### Applied Principles

The application follows **Clean Architecture** principles (Uncle Bob) to ensure clear separation of responsibilities and maximum testability.

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

### Layers and Responsibilities

#### 1. Domain Layer (`domain/`)

The innermost layer, with no external dependencies.

```java
// Pure business entity
public class Article {
    private Long id;
    private String title;
    private String content;
    private ArticleStatus status;
    private Long authorId;
    private Integer views;
    private Integer likes;
    // Business logic only
}
```

**Contains:**
- Business entities (Article, User, Comment, Tag)
- Repository interfaces
- Pure business rules

#### 2. Application Layer (`application/`)

Orchestrates use cases and coordinates data flow.

```java
// Facade coordinating use cases
@Service
public class ArticleFacade {
    private final CreateArticleCommand createCommand;
    private final GetArticleListQuery listQuery;

    public Mono<ArticleResponse> createArticle(CreateArticleRequest request) {
        return createCommand.execute(request);
    }
}
```

**Contains:**
- Facades (entry points for controllers)
- Use Cases (Commands & Queries - CQRS pattern)
- DTOs (Data Transfer Objects)
- Mappers (MapStruct)

#### 3. Infrastructure Layer (`infrastructure/`)

Implements technical details and external integrations.

```java
// Repository implementation with R2DBC
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

**Contains:**
- Spring configuration (Security, OpenAPI)
- Repository implementations (R2DBC)
- Security services (JWT)
- Exception handling

#### 4. Web Layer (`web/`)

Handles HTTP interactions.

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

**Contains:**
- REST controllers
- Request/Response DTOs
- Input validation

### CQRS Pattern

The application implements a **simplified CQRS** pattern (Command Query Responsibility Segregation):

```
┌─────────────────────────────────────────────────────────────────┐
│                         USE CASES                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Commands (Write)              Queries (Read)                  │
│   ├── CreateArticleCommand      ├── GetArticleByIdQuery         │
│   ├── DeleteArticleCommand      ├── GetArticleListQuery         │
│   ├── CreateUserCommand         ├── GetArticlesByAuthorQuery    │
│   ├── UpdateUserCommand         └── LoginQuery                  │
│   └── AddCommentCommand                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Dependency Flow

```
Web (Controller) ──▶ Application (Facade) ──▶ Domain (Model)
                              │                    ▲
                              ▼                    │
                    Infrastructure (Repository) ───┘
```

**Fundamental rule:** Dependencies always point inward (toward the domain).

### Architecture Benefits

| Benefit | Description |
|---------|-------------|
| **Testability** | Each layer can be tested independently |
| **Maintainability** | Isolated modifications without side effects |
| **Flexibility** | Easy to change framework or database |
| **Readability** | Clear and predictable structure |
| **Scalability** | Add features without major refactoring |

---

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Node.js 20+ (for frontend development)
- Java 21+ (for backend development)
- Maven 3.9+

### Launch with Docker Compose

```bash
# From the apps/ folder
docker-compose up -d

# The application will be available at:
# Frontend: http://localhost
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Local Development

**Backend:**
```bash
cd spring-api
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend-ui
npm install
npm run dev
```

### Environment Variables

**Backend (`application.yml`):**
```yaml
DB_HOST: localhost
DB_PORT: 5432
DB_NAME: blogdb
DB_USER: postgres
DB_PASSWORD: postgres
JWT_SECRET: your-secret-key
```

**Frontend (`.env`):**
```env
VITE_API_URL=/api
```

---

## Folder Structure

```
apps/
├── docker-compose.yml          # Local orchestration
├── README.md                   # French documentation
├── README.en.md                # English documentation (this file)
│
├── frontend-ui/                # React application
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
└── spring-api/                 # Spring Boot application
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

## Author

Built with Clean Architecture for a modern, scalable, and maintainable application.
