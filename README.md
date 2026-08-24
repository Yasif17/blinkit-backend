# Blinkit Clone — Full Stack (React + Tailwind + Spring Boot)

A full-stack quick-commerce app built to learn and practice real-world system design — modeled after Blinkit's hyperlocal delivery architecture. Built incrementally, module by module, starting with database design and authentication.

## Tech Stack

**Frontend**
- **React** — UI library
- **Tailwind CSS** — utility-first styling

**Backend**
- **Java + Spring Boot** — core framework
- **Spring Security + JWT** — stateless authentication
- **PostgreSQL** — primary relational database
- **Redis** — cart storage, token blacklist (logout)
- **Lombok** — boilerplate reduction (`@Data`, `@Builder`, etc.)
- **Bean Validation (Jakarta)** — request validation
- **Swagger / OpenAPI** — interactive API documentation

## Project Approach

Rather than jumping straight into code, this project started with **database design** for the full system — mapping out how each module's entities relate — before implementing anything. The reasoning: getting relationships right up front (e.g. Product vs. Inventory, Cart vs. Order) avoids painful schema migrations later.

## Modules — All Complete ✅

| Module | Purpose |
|---|---|
| User Auth | Registration, login, logout, JWT-based access |
| Catalog (Category/Product) | What products exist and how they're organized |
| Dark Store & Inventory | Per-store stock levels |
| Cart | Redis-backed, store-scoped cart with persistence |
| Wishlist | Persistent saved items per user |
| Order Management | Checkout, order history, status tracking |
| Admin | Product/category management, user management, order status updates |

## Database Design

```
User (app_user)
 ├── id, name, email (unique), password (BCrypt hash), role (CUSTOMER|ADMIN), createdAt

Category
 ├── id, name, slug, active

Product
 ├── id, categoryId (FK), name, slug, description, image, mrp, sellingPrice, unit, active

DarkStore
 ├── id, storeName, latitude, longitude, active

Inventory
 ├── id, productId (FK), darkStoreId (FK), quantity   [unique: productId + darkStoreId]

WishlistItem
 ├── id, userId (FK), productId (FK), addedAt         [unique: userId + productId]

Order
 ├── id, userId (FK), storeId (FK), status, subtotal, deliveryFee, totalAmount, deliveryAddress, placedAt

OrderItem
 ├── id, orderId (FK), productId (FK), quantity, priceAtOrder   [price snapshotted at purchase time]
```

**Cart** lives entirely in Redis (`cart:{userId}` → JSON), not Postgres — it's written on nearly every tap and most carts are abandoned, so keeping it out of the relational DB avoids unnecessary write load. It survives refresh and re-login since it's keyed by user ID server-side, with a 30-day TTL.

**Design principle carried through every module:** Catalog (Product/Category) is read-heavy and near-static; Inventory is write-heavy and changes on every order. Keeping them as separate entities lets each scale independently later.

---

## Module 1: User Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create account (always `CUSTOMER` role) |
| POST | `/api/auth/login` | Public | Authenticate, receive JWT |
| POST | `/api/auth/logout` | Authenticated | Blacklist current token |

**Flow:** Register hashes the password with BCrypt and returns a JWT (auto-login). Login verifies credentials and issues a fresh JWT. Every protected request passes `Authorization: Bearer <token>`, validated by a custom `JwtAuthFilter`. Since JWT is stateless, logout adds the token to a **Redis blacklist** with a TTL matching its remaining validity — it self-expires, no cleanup needed.

**Security:** BCrypt (slow, auto-salted) for passwords. Login returns the same generic error for "wrong password" and "user doesn't exist," preventing user enumeration. Registration ignores any client-supplied `role` field — self-assigned admin access is a privilege escalation risk. The first admin is created via a `CommandLineRunner` seeder on startup, solving the bootstrap problem (promoting to admin itself requires admin access).

## Module 2: Catalog (Category & Product)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/categories` | Public | List active categories |
| POST | `/api/categories` | Admin | Create category |
| PUT | `/api/categories/{id}` | Admin | Update category |
| DELETE | `/api/categories/{id}` | Admin | Deactivate (soft delete) |
| GET | `/api/products` | Public | Search/filter/browse, paginated |
| GET | `/api/products/{slug}` | Public | Product detail page |
| POST | `/api/products` | Admin | Create product |
| PUT | `/api/products/{id}` | Admin | Update product |
| DELETE | `/api/products/{id}` | Admin | Deactivate (soft delete) |

**Search** supports category filtering, keyword search, and min/max price range in a single query, with pagination via `Pageable`. Products are store-aware — listing/detail responses join against `Inventory` scoped to the caller's `storeId`, so `inStock`/`availableQty` reflect real availability, not global stock. Out-of-stock products still appear in listings (greyed out), matching how Blinkit itself handles it. Validation includes an `@AssertTrue` cross-field check ensuring `sellingPrice` never exceeds `mrp`.

## Module 3: Dark Store & Inventory

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/stores` | Public | List active dark stores |
| POST | `/api/stores` | Admin | Create dark store |
| POST | `/api/admin/inventory` | Admin | Set/update stock for a product at a store |
| GET | `/api/admin/inventory/{storeId}` | Admin | View a store's full inventory |

Stock updates use `findByProductIdAndDarkStoreId(...).orElseGet(Inventory::new)` so the same endpoint handles both first-time stocking and restocking without creating duplicate rows.

## Module 4: Cart

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/cart` | Authenticated | View current cart |
| POST | `/api/cart/items` | Authenticated | Add item (validates stock) |
| PUT | `/api/cart/items/{productId}` | Authenticated | Set quantity (0 removes item) |
| DELETE | `/api/cart/items/{productId}` | Authenticated | Remove item |
| DELETE | `/api/cart` | Authenticated | Clear cart |

Cart is scoped to a single dark store — adding an item from a different store starts a fresh cart, since one delivery trip can't span two stores. Every write re-validates against live inventory and recalculates `subtotal`/`deliveryFee`/`total` (free delivery over ₹199, a flat fee otherwise).

## Module 5: Wishlist

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/wishlist` | Authenticated | View saved products |
| POST | `/api/wishlist/{productId}` | Authenticated | Add product |
| DELETE | `/api/wishlist/{productId}` | Authenticated | Remove product |

Unlike Cart, Wishlist is Postgres-backed and persists indefinitely — it isn't store-scoped or time-limited. A unique constraint on `(userId, productId)` prevents duplicate saves.

## Module 6: Order Management

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/orders/checkout` | Authenticated | Place order from current cart |
| GET | `/api/orders` | Authenticated | Order history (own orders only) |
| GET | `/api/orders/{orderId}` | Authenticated | Order detail |

**Checkout flow (all in one `@Transactional` method):**
1. Load cart — reject if empty.
2. Atomically decrement stock per item via a single `UPDATE ... WHERE quantity >= :qty` query — this makes stock reservation race-condition-safe: if two customers check out the last unit simultaneously, only one `UPDATE` succeeds, the other rolls back the whole order.
3. Create the `Order` + `OrderItem` rows, snapshotting `priceAtOrder` so future price changes don't retroactively affect past orders.
4. Clear the cart — done **last**, after the DB transaction succeeds, since Redis isn't covered by `@Transactional` rollback.

Order confirmation is simply the `201` response from checkout — no separate endpoint. `getOrderDetail` scopes by `findByIdAndUserId(...)`, so one customer can never view another's order (returns 404, not the data).

## Module 7: Admin

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/admin/users` | Admin | View all users |
| PUT | `/api/admin/users/{id}/role` | Admin | Promote/change a user's role |
| GET | `/api/admin/orders` | Admin | View all orders (across all users) |
| PUT | `/api/admin/orders/{id}/status` | Admin | Update order status |

Plus the admin-only write endpoints from Catalog (Module 2) and Inventory (Module 3) above. All `/api/admin/**` routes are gated by `hasRole("ADMIN")` in `SecurityConfig`.

---

## Validation

Applied at the DTO level with Jakarta Bean Validation — invalid requests are rejected before reaching business logic. Key rules:

| Field | Rule |
|---|---|
| `email` | Required, valid format |
| `password` | Min 8 chars, at least one uppercase + one digit |
| `mrp` / `sellingPrice` | Positive; sellingPrice ≤ mrp (`@AssertTrue`) |
| `quantity` | Positive or zero |
| `latitude` / `longitude` | Range-checked (-90 to 90 / -180 to 180) |

## Error Handling

A centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) converts every exception into a consistent JSON shape instead of leaking stack traces:

```json
{
  "message": "Only 3 left in stock",
  "status": 400,
  "timestamp": "2026-08-25T10:00:00",
  "fieldErrors": null
}
```

| Exception | Status | Fires when |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Bean Validation fails |
| `HttpMessageNotReadableException` | 400 | Malformed JSON or invalid enum value |
| `UserAlreadyExistsException` | 409 | Duplicate email at registration |
| `InvalidCredentialsException` | 401 | Wrong email/password (generic message, prevents enumeration) |
| `UserNotFoundException` | 404 | Referenced user doesn't exist |
| `CategoryNotFoundException` / `CategoryAlreadyExistsException` | 404 / 409 | Category lookup/create conflicts |
| `ProductNotFoundException` | 404 | Referenced product doesn't exist |
| `DarkStoreNotFoundException` | 404 | Referenced store doesn't exist |
| `InsufficientStockException` | 400 | Requested quantity exceeds available stock |
| `EmptyCartException` | 400 | Checkout attempted with an empty cart |
| `ProductAlreadyInWishlistException` | 409 | Duplicate wishlist save |
| `WishlistItemNotFoundException` | 404 | Removing an item not in the wishlist |
| `OrderNotFoundException` | 404 | Order doesn't exist or belongs to another user |
| `Exception` (catch-all) | 500 | Anything unexpected — never exposes internals |

## Security Notes

- BCrypt for password hashing — deliberately slow, auto-salted, resistant to brute-force/rainbow-table attacks.
- Stateless JWT (HS256, 256-bit secret) — no server-side session storage.
- Logout via Redis blacklist with self-expiring TTL — standard pattern for stateless JWT systems industry-wide.
- Role-based access control (`hasRole("ADMIN")`) on all write/admin endpoints; public `GET`s stay open for browsing.
- Stock decrement uses an atomic conditional `UPDATE`, not read-then-write — closes the race condition on the last unit of a product.
- DB-level unique constraints (email, wishlist item, inventory per store) back up application-level checks.

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

API docs: `http://localhost:8080/swagger-ui/index.html`

## Testing

Every module was manually verified end-to-end via Swagger, including:
- Full auth flow: register → login → authenticated request → logout → token-reuse rejection
- Validation and duplicate/conflict handling on every write endpoint
- Store-scoped product search, filtering, and pagination
- Cart persistence across sessions, stock validation, store-switching behavior
- Checkout's atomic stock decrement under a simulated race condition
- Order history/detail ownership scoping (can't view another user's order)
- Admin-only access enforced on all management endpoints (403 for non-admins)

## Roadmap

- [ ] Nearest-store resolution — geospatial lookup by user location (Haversine/Redis GEO)
- [ ] Refresh tokens — avoid forcing re-login every 24 hours
- [ ] Order cancellation with inventory restoration
- [ ] Payment integration
- [ ] Automated integration tests (`@SpringBootTest` + `MockMvc`)
- [ ] React + Tailwind frontend build-out

---
