# Blinkit Clone — Full Stack (React + Tailwind + Spring Boot + Postgres)

A full-stack quick-commerce app built to learn and practice real-world system design — modeled after Blinkit's hyperlocal delivery architecture. This is being built incrementally, module by module, starting with database design and authentication.

## Tech Stack

**Frontend**
- **React** — UI library
- **Tailwind CSS** — utility-first styling

**Backend**
- **Java + Spring Boot** — core framework
- **Spring Security + JWT** — stateless authentication
- **PostgreSQL** — primary relational database
- **Redis** — token blacklist (logout) and planned caching/session layer
- **Lombok** — boilerplate reduction (`@Data`, `@Builder`, etc.)
- **Bean Validation (Jakarta)** — request validation
- **Swagger / OpenAPI** — interactive API documentation

## Project Approach

Rather than jumping straight into code, this project started with **database design** for the full system — mapping out how each module's entities relate — before implementing anything. The reasoning: getting relationships right up front (e.g. Product vs. Inventory, Cart vs. Order) avoids painful schema migrations later.

### Planned Modules (Full System)

| Module | Status | Purpose |
|---|---|---|
| **User Auth** | ✅ Implemented | Registration, login, logout, role-based access |
| Catalog (Category/Product) | 📋 Designed | What products exist and how they're organized |
| Inventory | 📋 Designed | Per-store stock levels (scoped by dark store) |
| Cart | 📋 Designed | Temporary, store-scoped selections before checkout |
| Wishlist | 📋 Designed | Persistent saved items, not store-scoped |
| Order Management | 📋 Designed | Order lifecycle, payment, delivery tracking |
| Admin | 🔶 Partial | Role promotion implemented; store/product management pending |

This README documents **Step 1: Database design + Auth module**, which is complete.

## Database Design

### Auth-related tables

```
User (app_user)
 ├── id            PK
 ├── name
 ├── email          (unique)
 ├── password       (BCrypt hash — never stored in plain text)
 ├── role           (CUSTOMER | ADMIN)
 └── createdAt
```

### Key relationships across the full schema (design phase)

- `Category (1) ──── (N) Product` — a category groups many products
- `Product (1) ──── (N) Inventory` — one product has a different stock row per store
- `Store (1) ──── (N) Inventory` — a store holds many products' stock
- `User (1) ──── (1) Cart` — one active cart per user, scoped to a single store
- `Cart (1) ──── (N) CartItem` — cart holds multiple line items
- `User (1) ──── (N) Order` — order history per user
- `Order (1) ──── (N) OrderItem` — items are snapshotted with `priceAtOrder`, so later price changes don't affect past orders
- `Order (1) ──── (1) Payment`, `Order (1) ──── (1) Delivery`

**Design principle:** Catalog (Product/Category) is read-heavy and near-static. Inventory is write-heavy and changes on every order. Keeping them as separate entities — even in a monolith — lets each be optimized (cached, indexed, scaled) independently later.

## Module 1: User Authentication

### API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a new account (always `CUSTOMER` role) |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| POST | `/api/auth/logout` | Authenticated | Blacklist the current token |
| PUT | `/api/admin/users/{id}/role` | Admin only | Promote/change a user's role |

### Authentication Flow

1. **Register** — validates input → checks for duplicate email → hashes password with BCrypt → saves user → returns a JWT (auto-login on signup).
2. **Login** — validates credentials against the stored hash → returns a fresh JWT on success.
3. **Authenticated requests** — client sends `Authorization: Bearer <token>` on every request. A custom `JwtAuthFilter` validates the token and checks it isn't blacklisted before Spring Security authorizes the request.
4. **Logout** — since JWT is stateless (no server-side session to delete), logout works by adding the token to a **Redis blacklist** with a TTL equal to the token's remaining validity. It self-expires — no manual cleanup needed.

### Role-Based Access & Admin Promotion

- Every public signup defaults to `CUSTOMER`. The registration endpoint **intentionally ignores** any `role` field a client might send — allowing self-assigned roles at signup would let anyone grant themselves admin access (a privilege escalation vulnerability).
- The **first admin account** is created via a startup seeder (`CommandLineRunner`), not through the API, since promoting a user to admin itself requires admin access — solving the bootstrap problem.
- Once an admin exists, they can promote other users via `PUT /api/admin/users/{id}/role`, which is itself protected by `hasRole("ADMIN")` in the security config.

### Validation

Applied at the DTO level using Jakarta Bean Validation, so invalid requests are rejected before reaching business logic:

| Field | Rules |
|---|---|
| `name` | Required, non-blank |
| `email` | Required, must be valid email format |
| `password` | Minimum 8 characters, must contain at least one uppercase letter and one digit |

### Error Handling

A centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) converts exceptions into consistent JSON error responses instead of leaking stack traces:

```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2026-08-24T10:00:00",
  "fieldErrors": { "email": "Email format is invalid" }
}
```

| Exception | HTTP Status | When it fires |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Bean Validation fails on request body |
| `UserAlreadyExistsException` | 409 | Email is already registered |
| `InvalidCredentialsException` | 401 | Wrong email or password (same message for both — prevents user enumeration) |
| `UserNotFoundException` | 404 | Admin tries to promote a non-existent user ID |
| `Exception` (catch-all) | 500 | Anything unexpected — never exposes internal details to the client |

### Security Notes

- Passwords are hashed with **BCrypt**, which is deliberately slow and auto-salted — resistant to brute-force and rainbow-table attacks, unlike fast hashes like MD5/SHA-256.
- Login returns the **same generic error** for "user doesn't exist" and "wrong password," so attackers can't determine which registered emails exist in the system.
- JWT secret is a 256-bit random key (HS256), stored in `application.properties` and excluded from version control.

## Running Locally

**Prerequisites:** PostgreSQL running, Redis running (`docker run -d -p 6379:6379 redis`).

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blinkit
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=<your-256-bit-base64-secret>
jwt.expiration-ms=86400000

admin.seed.email=admin@blinkit.com
admin.seed.password=Admin@1234
```

```bash
./mvnw spring-boot:run
```

API docs available at: `http://localhost:8080/swagger-ui/index.html`

## Testing

Tested manually via Postman and Swagger UI:
- Full register → login → authenticated request → logout flow
- Validation errors on malformed input
- Duplicate email rejection
- Wrong password rejection
- Token blacklist verified by reusing a token after logout (confirmed it's rejected)
- Admin promotion flow, gated by role-based access control

## Roadmap

- [ ] Catalog module — Category & Product CRUD
- [ ] Inventory module — per-store stock, with race-condition-safe stock reservation
- [ ] Cart module — Redis-backed, store-scoped cart
- [ ] Wishlist module — persistent saved items
- [ ] Order module — checkout flow, order status lifecycle
- [ ] Nearest-store resolution — geospatial lookup by user location
- [ ] Refresh tokens — avoid forcing re-login every 24 hours
- [ ] Automated integration tests (`@SpringBootTest` + `MockMvc`)

---
