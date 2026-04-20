# 🛒 sb-ecom — E-Commerce REST API

> 🚧 **This project is actively under development.** Core backend features are complete and AWS deployment is coming soon.

A production-structured e-commerce backend built with **Spring Boot 4** and **Java 21**, featuring stateless JWT authentication, role-based access control, product catalog management, cart operations, and address handling — all exposed via a clean RESTful API.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Environment Setup](#-environment-setup)
- [API Reference](#-api-reference)
- [Security](#-security)
- [Database Schema](#-database-schema)

---

## ✨ Features

- **JWT Authentication** — Stateless auth with tokens stored in HTTP-only cookies; supports sign-in, sign-up, and sign-out
- **Role-Based Access Control** — Three roles: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN` — enforced at route level via Spring Security filter chain
- **Product Catalog** — Full CRUD, image upload, keyword search, and category-based filtering
- **Pagination & Sorting** — Configurable page size, sort field, and sort order on all list endpoints
- **Cart Management** — Add, update, and remove items with real-time stock validation
- **Address Management** — User address creation tied to authenticated context
- **Global Exception Handling** — Centralized `@RestControllerAdvice` with structured error responses
- **Input Validation** — Bean Validation (`@Valid`) on all request bodies
- **API Documentation** — Swagger UI available at `/swagger-ui/index.html`

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| Security | Spring Security + JWT (jjwt 0.13) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Mapping | ModelMapper 3.2.4 |
| Boilerplate | Lombok |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Docs | SpringDoc OpenAPI (Swagger UI) |

---

## 📁 Project Structure

```
src/main/java/com/ecommerce/project/
│
├── Config/                  # App-wide constants and beans (AppConstants, AppConfig)
│
├── controller/              # REST controllers
│   ├── AuthenticationController.java
│   ├── CategoryController.java
│   ├── ProductController.java
│   ├── CartController.java
│   └── AddressController.java
│
├── model/                   # JPA entities
│   ├── User.java
│   ├── Role.java
│   ├── Product.java
│   ├── Category.java
│   ├── Cart.java
│   ├── CartItem.java
│   └── Address.java
│
├── payload/                 # DTOs (request/response)
│
├── Repository/              # Spring Data JPA repositories
│
├── service/                 # Service interfaces + implementations
│   └── impl/
│
├── security/                # Security layer
│   ├── WebSecurityConfig.java
│   ├── jwt/                 # JwtUtils, AuthTokenFilter, AuthEntryPointJwt
│   ├── services/            # UserDetailsImpl, UserDetailServiceImpl
│   ├── request/             # LoginRequest, SignupRequest
│   └── response/            # UserInfoResponse, MessageResponse
│
├── exception/               # Custom exceptions + global handler
│   ├── MyGlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── APIException.java
│
└── util/                    # AuthUtils (logged-in user context)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository

```bash
git clone https://github.com/your-username/sb-ecom.git
cd sb-ecom
```

### 2. Create the database

```sql
CREATE DATABASE ecommerce;
```

### 3. Configure environment

Create `src/main/resources/application-local.properties`:

```properties
spring.datasource.password=your_db_password
spring.app.jwtSecret=your_base64_encoded_secret_key
```

> Generate a strong JWT secret: `openssl rand -base64 64`

### 4. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The server starts at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## ⚙️ Environment Setup

The application uses environment-aware configuration. Sensitive values are never hardcoded.

| Property | Description | Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/ecommerce` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | *(required)* |
| `JWT_SECRET` | Base64-encoded HMAC signing key | *(required)* |

Set these as environment variables in production, or override them in `application-local.properties` during local development (this file is git-ignored).

---

## 📡 API Reference

### Auth — `/api/auth`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Public | Register a new user |
| `POST` | `/api/auth/signin` | Public | Sign in, receive JWT cookie |
| `POST` | `/api/auth/signout` | Public | Clear the JWT cookie |
| `GET` | `/api/auth/user` | Authenticated | Get current user details |
| `GET` | `/api/auth/username` | Authenticated | Get current username |

**Signup Request Body:**
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "secret123",
  "role": ["user"]
}
```

> Valid role values: `user`, `seller`, `admin`

---

### Categories — `/api`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/public/categories` | Public | Get all categories (paginated) |
| `POST` | `/api/public/category` | Public | Create a new category |
| `PUT` | `/api/public/categories/{categoryId}` | Public | Update a category |
| `DELETE` | `/api/admin/categories/{categoryId}` | Admin | Delete a category |

**Pagination params:** `pageNumber`, `pageSize`, `sortBy`, `sortOrder`

---

### Products — `/api`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/public/products` | Public | Get all products (paginated) |
| `GET` | `/api/public/categories/{categoryId}/products` | Public | Get products by category |
| `GET` | `/api/public/products/keyword/{keyword}` | Public | Search products by keyword |
| `POST` | `/api/admin/categories/{categoryId}/product` | Admin | Add a new product |
| `PUT` | `/api/admin/products/{productId}` | Admin | Update a product |
| `DELETE` | `/api/admin/products/{productId}` | Admin | Delete a product |
| `PUT` | `/api/products/{productId}/image` | Authenticated | Upload product image |

---

### Cart — `/api`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/carts/products/{productId}/quantity/{quantity}` | Authenticated | Add product to cart |
| `GET` | `/api/carts/users/cart` | Authenticated | Get current user's cart |
| `GET` | `/api/carts` | Authenticated | Get all carts |
| `PUT` | `/api/cart/products/{productId}/quantity/{operation}` | Authenticated | Update item quantity (`add`/`delete`) |
| `DELETE` | `/api/carts/{cartId}/product/{productId}` | Authenticated | Remove item from cart |

---

### Address — `/api`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/addresses` | Authenticated | Create a new address |

---

## 🔐 Security

Authentication is handled via **JWT stored in an HTTP-only cookie** (`springBootEcom`).

**Flow:**
1. Client calls `POST /api/auth/signin` with credentials
2. Server validates credentials, generates a signed JWT, and sets it as a cookie in the response
3. All subsequent requests automatically send the cookie — no manual `Authorization` header needed
4. On `POST /api/auth/signout`, the server clears the cookie

**Password hashing:** BCrypt via Spring Security's `PasswordEncoder`

**Route protection:**

| Pattern | Access |
|---|---|
| `/api/auth/**` | Public |
| `/api/public/**` | Public |
| `/api/admin/**` | `ROLE_ADMIN` only |
| All other routes | Any authenticated user |

**Default seeded users** (auto-created on first startup):

| Username | Password | Role |
|---|---|---|
| `user1` | `password1` | ROLE_USER |
| `seller1` | `password2` | ROLE_SELLER |
| `admin` | `password3` | ROLE_ADMIN |

> Change these credentials immediately in any non-local environment.

---

## 🗄️ Database Schema

Core entities and their relationships:

```
users
 ├── user_role  (join table) → roles
 ├── addresses  (one-to-many)
 ├── cart       (one-to-one)
 └── products   (one-to-many, as seller)

products
 ├── category   (many-to-one)
 ├── user       (many-to-one, seller)
 └── cart_items (one-to-many)

cart
 └── cart_items (one-to-many) → products

roles
 └── (ROLE_USER | ROLE_SELLER | ROLE_ADMIN)
```

> Schema is auto-managed by Hibernate. Set `spring.jpa.hibernate.ddl-auto=update` for persistent data across restarts.

---

## 🚀 Deployment

This project is actively under development and will soon be deployed on **AWS**.
