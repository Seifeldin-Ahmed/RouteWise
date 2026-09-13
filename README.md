<h1 align="center">RouteWise</h1>

<p align="center">
  <strong>Every payment takes the smartest route.</strong><br />
  A smart payment routing system that picks the cheapest or fastest gateway for each payment,
  splits amounts too large to go through in one piece, records every transaction, and tracks each
  biller's daily allowance per gateway.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/java-26-brightgreen" alt="Java 26" />
  <img src="https://img.shields.io/badge/spring_boot-4.1.1-brightgreen" alt="Spring Boot 4.1.1" />
  <img src="https://img.shields.io/badge/angular-21-red" alt="Angular 21" />
  <img src="https://img.shields.io/badge/postgresql-18-blue" alt="PostgreSQL 18" />
  <img src="https://img.shields.io/badge/maven-wrapper-blue" alt="Maven" />
  <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="MIT" />
</p>

<img width="1706" height="781" alt="image" src="https://github.com/user-attachments/assets/7424d47f-7e7c-4a08-980a-d3be613972e0" />

---

## 📚 Contents

1. [✨ Features](#-features)
2. [🛠️ Built with](#️-built-with)
3. [💻 Prerequisites](#-prerequisites)
4. [⚡ Setup & run](#-setup--run)
5. [🔑 Test credentials](#-test-credentials)
6. [🌐 API](#-api)
7. [🧭 How the routing works](#-how-the-routing-works)
8. [🏗️ Backend architecture](#️-backend-architecture)
9. [🎨 Frontend architecture](#-frontend-architecture)
10. [📌 Decisions worth knowing](#-decisions-worth-knowing)

---

## ✨ Features

### 👤 Biller

- **Route a payment** — enter an amount and say whether it can wait; RouteWise picks the gateway
  and records the payment in one call.
- **Cheapest or fastest** — one flag on the request decides which way the ranking leans.
- **Automatic splitting** — an amount too large for any single transaction is chunked and sent
  anyway, without a second button press.
- **See the runners-up** — every gateway that qualified, ranked, with its commission and speed.
- **Daily allowance tracking** — per biller, per gateway, per calendar day, shown as you spend it.
- **Transaction history** — filter by date and gateway, with daily totals and a per-gateway
  breakdown.

### 👑 Admin

- **Gateway CRUD** — add gateways, retune fees, limits, operating days and hours.
- **Retire without deleting** — take a gateway out of routing while keeping its history intact.
- **Route on behalf of any biller** — admins pass the ownership check for every biller.
- **See every biller's history**.

### 🎛️ Interactive landing page

The landing page is not a screenshot. It runs ports of the same four filters and the same ranking
the service uses — against your own clock — so dragging the amount slider genuinely re-routes the
payment, and gateways drop out for the real reasons, live.

---

## 🛠️ Built with

- **Backend:** Java 26, Spring Boot 4.1.1, Spring Data JPA, Spring Security (JWT), Liquibase,
  Lombok, Maven
- **Frontend:** Angular 21 (standalone, zoneless, signals), TypeScript, CSS
- **Database:** PostgreSQL 18

---

## 💻 Prerequisites

Before you start, make sure you have:

- **Java 26 (JDK)**
- **Maven** — or just use the bundled `./mvnw` wrapper
- **Node.js 20+ and npm**
- **PostgreSQL 18**

---

## ⚡ Setup & run

### 1. Database

```sql
CREATE DATABASE payment_routing;
```

Point the backend at it in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_routing
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Liquibase creates every table and loads the seed data on first boot — there is nothing to run by
hand. Hibernate is set to `ddl-auto=validate`, so the migrations own the schema and the
application refuses to start if an entity and its table have drifted apart.

> **Re-running an older build?** Drop and recreate the database first. `001-init-schema.sql` was
> edited in place when the transaction log was flattened, and Liquibase refuses to run a changeset
> whose checksum no longer matches the one it recorded.

> **Lombok note.** Since JDK 23, `javac` no longer discovers annotation processors from the
> classpath, so `maven-compiler-plugin` names Lombok explicitly in `<annotationProcessorPaths>`.
> Without it Lombok silently does nothing and every generated getter goes missing. That block also
> re-declares `<parameters>true</parameters>`, which the `@PreAuthorize` expressions depend on to
> resolve `#billerId` by name. If your IDE reports missing getters, enable annotation processing
> there too. Lombok prints a `sun.misc.Unsafe` warning on JDK 26; it is harmless.

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

Serves on **`http://localhost:8080`**.

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

Serves on **`http://localhost:4200`**. The backend allows any origin, so no CORS setup is needed.

### 4. Postman

Import `postman/Fawry-Payment-Routing.postman_collection.json`. Run **Login (biller)** or
**Login (admin)** first — a test script stores the token and biller id in collection variables, so
every other request is authenticated automatically.

---

## 🔑 Test credentials

Seeded by `002-seed-data.sql`:

| 🏷️ Role | 📧 Email address | 🔐 Password |
|---|---|---|
| 👑 **Admin** | `admin@fawry.com` | `Admin@123` |
| 👤 **Biller** (id 2) | `biller@fawry.com` | `User@123` |
| 👤 **Biller** (id 3) | `water@fawry.com` | `User@123` |

`POST /signup` always creates a USER. Admin accounts come from the migration.

### 🏦 Seeded gateways

The three sample configurations from the requirements, verbatim and nothing else:

| Name | Commission | Daily limit | Processing | Availability | Min | Max |
|---|---|---|---|---|---|---|
| Gateway 1 | 2 EGP + 1.5% | 50,000 | Instant | 24/7 | 10 | 5,000 |
| Gateway 2 | 5 EGP + 0.8% | 200,000 | 24 hours | Sun–Thu 09:00–17:00 | 100 | **No limit** |
| Gateway 3 | 0 EGP + 2.5% | 100,000 | 2 hours | 24/7 | 50 | 10,000 |

Add your own from the admin screen — overnight windows (`availableFrom` later than `availableTo`)
and restricted day ranges are supported, they are just not seeded.

---

## 🌐 API

| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/login` | — | Email + password in JSON, returns a JWT |
| POST | `/signup` | — | Register a biller |
| GET | `/api/gateways` | ADMIN | List configurations (`?includeInactive=`) |
| GET | `/api/gateways/{id}` | ADMIN | One configuration |
| POST | `/api/gateways` | ADMIN | Create (200, no body) |
| PUT | `/api/gateways/{id}` | ADMIN | Update (200, no body) |
| PATCH | `/api/gateways/{id}/active?active=` | ADMIN | Take in or out of routing (200, no body) |
| DELETE | `/api/gateways/{id}` | ADMIN | Delete the gateway and its history (200, no body) |
| POST | `/api/payments/recommend` | USER, ADMIN | Choose a gateway and record the payment |
| POST | `/api/payments/split` | USER, ADMIN | Chunk plan for an oversized amount |
| GET | `/api/billers/{billerId}/transactions` | USER, ADMIN | History (`?date=&gatewayId=&page=`, 10 rows a page) |

### 🔒 Authorization

The JWT carries `email`, `id` and `roles`. **The `id` claim is the biller identity**, and
`JwtAuthFilter` makes it the `Authentication` **principal** — so the caller id travels with the
request on its own, readable as `@AuthenticationPrincipal Integer` in a controller or
`authentication.principal` in a SpEL expression.

Role checks are declared per endpoint with `@PreAuthorize`. Anything addressed to a specific
biller is checked again **on the service method**:

```java
@PreAuthorize("hasRole('ADMIN') or #billerId == authentication.principal")
public TransactionHistoryResponse history(Integer billerId, LocalDate date, Integer gatewayId, int page)
```

Roles alone would not be enough — a biller could otherwise read another biller's history by
editing the id in the URL, or route a payment as someone else by editing `billerId` in the body.
ADMIN passes for any biller. Because the rule sits on the service rather than the controller, it
holds for every caller of the method.

### ⚠️ Errors

Every failure comes back in the same envelope the security filters use:

```json
{ "status": "error", "message": "..." }
```

Bean-validation failures use the same envelope, with the offending fields folded into `message` as
`field: what is wrong | field: what is wrong` — one complaint per field, the first one wins.

| Status | Means |
|---|---|
| `400` | A broken business rule or an invalid payload |
| `401` | Missing or expired token |
| `403` | Wrong role, or the wrong biller |
| `404` | Missing id |
| `409` | Duplicate email, or a payment that lost a write race |

---

## 🧭 How the routing works

### 🔍 Filtering

A gateway is a candidate when **all** of these hold:

1. **Active** — soft-deleted gateways are never routable.
2. **Open right now** — today falls inside the gateway's operating day range **and** the current
   Africa/Cairo time is inside its window. The day range is inclusive at both ends and may wrap
   the week (`SUNDAY → THURSDAY` closes at the weekend, `MONDAY → SUNDAY` is every day) — unlike
   the time window, whose end is exclusive, because "Sun–Thu" includes Thursday while "9AM–5PM"
   excludes 5PM. A window where the start is later than the end crosses midnight
   (`22:00 → 06:00`); start equal to end means it never closes. An overnight session belongs to
   the day it *opened*, so a gateway running Sun–Thu 22:00–06:00 is open at Friday 02:00
   (Thursday's session) but closed at Sunday 02:00.
3. **The amount clears the minimum** — `amount ≥ min`. Splitting only ever makes a chunk *smaller*
   than the whole, so an amount under the minimum is unpayable on either endpoint.
4. **Quota left** — the biller's unused daily allowance on that gateway covers the amount. Charged
   on the whole payment, so chunking it frees nothing.

Those three are all `RoutingService.findValidGateways` decides, because they are the three whose
answer is the same for both endpoints. It returns `GatewayCandidate`s carrying only the gateway and
the biller's remaining quota on it — `chunks` and `totalCommission` come back **null**, because
what a payment costs depends on how it is chunked and that is the one thing the two endpoints
decide differently. Each caller fills both in for the plan it chose. Null rather than zero
deliberately: zero is a plausible commission here (gateway 3 charges no fixed fee), so a caller
that forgot to re-price would silently rank every gateway as free, whereas null fails at once.

The quota it carries is the figure the amount was *measured against*, which is one payment out of
date by the time the response is built, since both endpoints record the payment before answering.
`PaymentService.persist` therefore overwrites `remainingQuota` on the gateway it chose with what
`QuotaService.consume` left behind. The alternatives keep the pre-payment figure, which is correct
for them: nothing was consumed on those.

**The per-transaction maximum is where they part company**, so it is applied by the caller rather
than in the shared pass:

- **`/recommend`** treats it as a hard filter — `amount ≤ max` (a gateway with **no maximum**
  always passes). A recommendation is always a single transaction, so `/recommend` never consults
  `SplitCalculator`. When a gateway clears everything else and is turned away on its ceiling alone,
  the response comes back with no gateway and `requiresSplitting: true`, which is the caller's cue
  to try `/split`.
- **`/split`** hands it to `SplitCalculator`, which decides whether a valid chunk plan exists. A
  gateway drops out only when no plan exists for it at all.

### 🏁 Ranking

| Urgency | 1st | 2nd | 3rd |
|---|---|---|---|
| `INSTANT` | processing time ↑ | commission ↑ | gateway id ↑ |
| `CAN_WAIT` | commission ↑ | processing time ↑ | gateway id ↑ |

Processing time is stored as **whole hours**, where **`0` means Instant** — the vocabulary the
requirements use (Instant / 2 Hours / 24 Hours).

Gateway id as the last tie-break makes the ordering total, so the same request always gives the
same answer.

Cost is compared as the commission for the **whole** payment, splitting included — a gateway with a
low fee that needs four chunks charges its flat fee four times, and the ranking should see that.
Which is why pricing happens *after* the chunk plan is chosen, not in the shared pass: each
endpoint settles on its plan — a single chunk for `/recommend`, whatever `SplitCalculator` returned
for `/split` — and prices the gateway on that one plan, so every candidate is ranked on a figure
that is already exact and nothing is priced twice.

### 💰 Commission

```
commission = fixedCommission + amount × percentageCommission / 100
```

Rounded to **2 decimals, HALF_UP** (nearest piaster). For a split, each chunk is rounded
individually and the rounded values are summed, so the per-chunk figures shown to the caller always
add up to the reported total.

### ✂️ Splitting

A gateway with **no maximum** never splits: the amount travels whole, however large.

Otherwise `SplitCalculator` uses the fewest chunks it can — `n = ceil(amount / max)` — fills the
first `n-1` to `max`, and gives the remainder to the last.

When that remainder lands below `min`, the shortfall is **borrowed backwards**: each earlier chunk
gives up only what it can spare while staying at or above `min`, and the borrowing cascades to the
chunk before it when one donor cannot cover the whole gap. That is what stops the naive fix of
"take it all from the previous chunk", which would push that chunk under the minimum instead.

Before any plan is returned it is **verified**: every chunk inside `[min, max]`, and the chunks
summing to exactly the original amount. A plan that fails is discarded and the gateway drops out of
the running rather than producing an invalid split.

A split exists at all only when `amount ≥ min` and `ceil(amount / max) × min ≤ amount`. More chunks
would only raise the minimum the plan has to cover and fewer would breach `max`, so when that fails
no chunk count works. There is also a ceiling of 1000 chunks per payment, so a gateway configured
with a tiny maximum cannot produce an unbounded plan.

### 📊 Quota

Per biller, per gateway, per **Africa/Cairo** calendar date, in `biller_quota_usage`.

There is no reset job. A new date simply has no row yet, which means zero consumed — the midnight
reset is exact and stateless.

**The limit is checked once, at selection.** `RoutingService.findValidGateways` drops any gateway
whose remaining allowance is short of the amount, so a gateway that reaches `QuotaService.consume`
has already been measured against it and `used + amount` cannot pass the limit. `consume` does not
re-check; it adds and saves.

**Charging it is a read-modify-write, protected by isolation rather than a lock.** Both payment
endpoints run their transaction at **SERIALIZABLE**, so two concurrent payments cannot both read
the same `used_amount` and jointly overshoot the limit. PostgreSQL enforces that optimistically
(Serializable Snapshot Isolation): instead of making the second payment wait, it lets both run and
aborts whichever it cannot serialize, with SQL state **40001**.

**There is no server-side retry, by choice.** An aborted transaction reaches
`GlobalExceptionHandler` and comes back as **409** with an invitation to try again — nothing was
recorded and no quota was charged, so the identical request is safe to resend. 409 rather than 500
because the request was never at fault, it only lost a race. A retry could not have lived in the
service anyway: a PostgreSQL transaction that has hit an error accepts no further statements, so
retrying would have to wrap the call from outside the transaction entirely. Leaving it to the
caller keeps that out of the server.

Two rejections mean the same thing and get the same 409. **40001** is the read-modify-write
conflict above. **23505 on `uq_biller_quota`** is the other: two payments that are both the first
of the day on that gateway each find no usage row and each insert one — PostgreSQL raises a plain
unique violation for that rather than a serialization failure, so it is named explicitly in the
handler's constraint map next to `gateways_name_key`. The unique index is the real guard.

Different billers do not contend: SSI only aborts on a read-write dependency cycle, and two billers
touch different usage rows. The unique index on `(biller_id, gateway_id, usage_date)` is what keeps
the usage lookup an index scan — a sequential scan would have SSI take a relation-wide predicate
lock and start failing unrelated payments against each other.

---

## 🏗️ Backend architecture

```
com.fawry.routing
 ├── entity/         JPA entities only — Gateway, User, Transaction, BillerQuotaUsage,
 │                   and the Role/Urgency enums
 ├── repository/     Spring Data JPA, one per entity
 ├── dto/
 │    ├── request/   PaymentRequest (both payment endpoints), GatewayRequest, SignupRequest
 │    └── response/  RecommendResponse, SplitResponse, TransactionHistoryResponse, ...
 ├── controller/     REST only: validate → call a service → return a DTO
 ├── service/        Concrete @Service classes — business logic and authorization rules
 ├── util/           Pure, Spring-free math: CommissionCalculator, SplitCalculator,
 │                   AvailabilityChecker, GatewayRanker, MoneyUtils
 ├── mapper/         Hand-written entity ↔ DTO translation
 ├── exception/      Custom exceptions + GlobalExceptionHandler
 ├── config/         CORS, security
 ├── security/       JWT filter, login filter, SecurityConfig, MyUserDetails
 └── validation/     @PasswordMatches
```

**Why it is shaped this way**

- **Controllers stay thin.** Each one validates its input, calls a service and returns a DTO. No
  routing decision, no arithmetic, no persistence.
- **Entities never leave the controller layer.** Everything crossing the HTTP boundary is a DTO, so
  a schema change cannot silently reshape the public API, and lazy JPA associations cannot be
  serialized by accident.
- **All the money and time arithmetic lives in `util/`.** `CommissionCalculator`,
  `SplitCalculator`, `AvailabilityChecker` and `GatewayRanker` are `final` classes with static
  methods and no Spring dependencies at all, so the rules that are easiest to get wrong stay
  independently reasonable about — and they are what the frontend's landing page re-implements.
- **Services are concrete classes, no interfaces.** Each has exactly one implementation and nothing
  swaps it at runtime, so an interface plus an `Impl` would have been a second file per service
  earning nothing. Spring still proxies them for `@Transactional` and `@PreAuthorize`, and they are
  still injected by type, so composition is unchanged.
- **Gateway selection is shared, but only as far as it genuinely is.** `/recommend` and `/split`
  both call `RoutingService.findValidGateways`, so availability, quota, the minimum and the ranking
  have exactly one definition rather than two that can drift. The two things that really do differ
  are left to the caller instead of being folded in behind a flag, so the shared method has no mode
  parameters and neither endpoint carries the other's logic: the **per-transaction maximum**, which
  one treats as a wall and the other chunks around, and **commission**, which cannot be worked out
  until the chunk plan is known. So the shared pass hands back unpriced candidates and each
  endpoint prices one for its own plan.
- **Lombok carries the boilerplate.** `@RequiredArgsConstructor` on every service, controller and
  config replaces the hand-written injection constructors; `@Getter @Setter` on entities and
  request DTOs replaces the accessors; `@Slf4j` replaces the logger field. One deliberate
  exception: **no `@Data` on entities**, because its generated `equals`/`hashCode`/`toString` walk
  lazy JPA associations — extra queries, `LazyInitializationException` outside a transaction, and
  identity that shifts as fields are set. Response DTOs are `record`s, so Lombok does not apply.
- **Authorization lives on the service, declaratively.** The ownership rule is a `@PreAuthorize` on
  the service method comparing `billerId` against the principal, so there is no identity-holder
  object to pass around and no way for a controller to forget the check.

---

## 🎨 Frontend architecture

```
src/app
 ├── core/
 │    ├── models/         Interfaces mirroring the backend DTOs
 │    ├── services/       AuthService, PaymentService, GatewayService, TransactionService
 │    ├── interceptors/   Attaches the JWT, clears the session on 401
 │    ├── guards/         authGuard, adminGuard
 │    ├── api.config.ts   Backend base URL
 │    └── error-message.ts
 ├── features/
 │    ├── landing/                Interactive routing playground (public)
 │    ├── auth/login, auth/signup
 │    ├── payments/recommend      Routing form, recommendation, split, alternatives
 │    ├── transactions/history    Filters, daily summary, per-gateway breakdown
 │    └── admin/gateways          Gateway CRUD (admin only)
 └── app.ts / app.routes.ts / app.config.ts
```

Every HTTP call lives in a service under `core/services`; no component touches `HttpClient`.
Feature routes are lazy-loaded, and components hold their state in signals.

**The theme.** Indigo shading into violet for the brand, with **teal reserved for the route a
payment actually took** — on the landing board, and again on the chosen-gateway panel of the
recommend screen. Tokens live in `src/styles.css`; component sheets carry almost no colour of
their own.

Dark is the default and light is the same identity lit from the other side, declared once as
`:root[data-theme='light']`. The attribute is set by an inline script in `index.html` before first
paint — from `localStorage`, falling back to the operating system's preference — so the page never
flashes the wrong theme, and the toggle in the header is just a signal writing that attribute back.

**The auto-split flow.** The recommend screen calls `PaymentService.recommendWithSplit()`, which
issues `POST /api/payments/recommend` and — only when the response comes back with
`requiresSplitting: true` — chains straight into `POST /api/payments/split`. The user presses one
button and sees the chunk breakdown; the component never orchestrates the second request itself.

That flag means "no gateway takes this amount whole", so the recommendation selected no gateway and
recorded nothing. The follow-up split is therefore the only payment made, and nothing has to be
carried over from the recommendation for it.

Because of that, the screen does **not** report a failed recommendation as a failure when
`requiresSplitting` is set — the payment has not been decided yet at that point. It shows that the
amount was too large for one transaction and lets the split's own outcome say whether anything was
recorded. "No gateway available" is reserved for the dead ends, where no split was attempted.

**The landing page re-implements the rules.** `features/landing/landing.ts` contains ports of
`AvailabilityChecker`, `SplitCalculator` (borrowing cascade included), `CommissionCalculator` and
`GatewayRanker`, applied in `RoutingService`'s filter order against invented gateway figures and
the visitor's own clock. It calls no API. If a rule changes in the backend, this is the one place
in the frontend that has to follow it by hand.

`adminGuard` hides admin routes, but it is convenience, not security: the backend refuses non-admin
calls to the gateway endpoints regardless of what the router allowed.

---

## 📌 Decisions worth knowing

**`/recommend` and `/split` are processing steps, not previews.** Choosing a gateway writes a
`transactions` row and consumes quota immediately. There is no separate execute call.

**Every call is its own payment.** `/split` selects its own gateway, writes its own row and
consumes its own quota; it never attaches itself to something `/recommend` created. It does not
need to, because the two never both record for one amount: a recommendation that comes back with
`requiresSplitting: true` selected no gateway and recorded nothing, so the split that follows it is
the only payment there is.

**When nothing qualifies, `/recommend` still answers 200** with `recommendedGateway: null`, an
empty `alternatives` array and a message explaining what blocked it. Nothing is recorded.
`requiresSplitting` separates the one recoverable case — a gateway rejected on its per-transaction
ceiling alone, so `/split` may still carry the amount — from the dead ends that chunking cannot
reach: nothing open, no quota left, or an amount below every minimum.

**Quota is a hard filter on both endpoints.** It is charged on the whole payment, so splitting an
amount frees none of it — a gateway without the quota is out for `/recommend` and `/split` alike,
and a selected gateway always has the quota it needs. When nothing qualifies, `/split` answers 200
with `selectedGateway: null`, an empty `splits` array and a message; nothing is recorded.

**`/split` carries no `requiresSplitting` flag.** The flag exists so a caller of `/recommend` knows
a split is worth trying; on the split itself it would only repeat `splits.length > 1`, which the
caller can already see.

**The transaction log is flat: one row per chunk.** A payment split into four chunks is four rows
in `transactions` — same gateway, same business date, one row per amount the gateway actually
processed — and counts as four in the history's `transactionCount`. There is no parent row and
nothing ties the four together, because a chunk *is* the transaction from the gateway's point of
view, and the biller's daily totals come out the same either way: the four chunk amounts add back
up to the payment, and the four chunk commissions to its commission. Quota is still charged once,
on the whole payment.

**The history is two queries: the rows, and the totals.** `GET /api/billers/{id}/transactions`
fetches the transaction rows for its `transactions` array, then asks the database for the
per-gateway totals with a `GROUP BY` rather than summing the rows in Java. Every aggregate in the
response — `totalTransactions`, `totalAmountProcessed`, `totalCommissionCharged` and each
`gatewayBreakdown` entry — comes out of that one aggregate query, so they cannot disagree with each
other, and they do not depend on the row array being complete. That last part is what makes paging
possible: `transactions` comes back 10 rows at a time (`?page=`, zero-based), while every figure
beside it still describes the whole filter. Summing the rows in Java would have reported 10.

The page length is the server's, not the caller's — there is no `size` parameter, so no request
can ask for the whole table back and undo the paging. The client chooses the page and nothing else.

The rows are fetched as a `List<Transaction>` taking a `Pageable`, not as a `Page<Transaction>`.
A `Page` would run its own COUNT to work out `totalPages`, and the aggregate query has already
counted — `totalPages` is `totalTransactions` divided by the page size.

It groups by **`g.id` alone** — the gateway's primary key, reached through an explicit
`JOIN t.gateway g`. That join is what keeps the GROUP BY to one column: grouped on the primary key
of `gateways`, PostgreSQL lets the query select `g.name` and `g.dailyLimitPerBiller` by functional
dependency. Grouped on `transactions.gateway_id` instead — which is what `t.gateway.id` compiles
to, no join needed — that rule does not apply, and both columns would have to be repeated in the
GROUP BY purely to satisfy SQL.

Not grouped by `business_date`, because `GatewayBreakdownResponse` has no date field: a multi-day
query would come back with several rows per gateway that Java would have to merge, aggregating
twice. Not by `biller_id` either — the WHERE clause already pins it to one value, so it cannot
split a group. Both optional filters are applied in SQL, so the grouped query has the same four
variants the row query does (separate methods rather than nullable binds — see
`TransactionRepository`).

**Quota used is not sent by the history.** On a single date it is exactly `totalAmount`, so the
endpoint would be repeating itself. The frontend shows "Quota left" as
`dailyLimitPerBiller - totalAmount`, and blanks the column when the query names no date, because a
running total across several days would not mean anything against a daily limit.

**A gateway may have no maximum.** `max_transaction_amount` is nullable, and `NULL` means "No
Limit" — Gateway 2 in the requirements. Such a gateway never splits and reports
`maxTransactionLimit: null`, which the UI renders as *No limit*.

**Availability is days plus a window, not just a window.** "Sun–Thu 9AM–5PM" needs both, so
`available_day_from` / `available_day_to` sit alongside `available_from` / `available_to`, in the
same shape. That covers every schedule in the requirements and keeps the model to two extra enum
columns; the trade is that a non-contiguous schedule ("Mon, Wed, Fri") cannot be expressed.

**Writes answer with no body.** `POST /signup` and the four gateway mutations return an empty
response — 201 for a create, 200 otherwise. The caller already knows what it sent, and echoing the
saved row back only invites a client to trust its copy instead of re-reading. The two payment
endpoints are the deliberate exception: the chosen gateway and the chunk plan are the whole point
of the call, so those POSTs keep their body.

**Retiring a gateway and deleting one are separate operations.**
`PATCH /api/gateways/{id}/active?active=false` sets `active = false`: the gateway stops being a
routing candidate, but the row stays, so historical transactions still resolve to a real gateway.
Passing `active=true` undoes it. `DELETE /api/gateways/{id}` drops the row instead, and takes the
gateway's history with it: every transaction recorded against it and every biller's quota usage on
it are deleted in the same transaction. Nothing referencing `gateways` declares an `ON DELETE`
clause, so the service clears the children itself or the database would refuse the delete outright.

That makes it the one genuinely destructive operation in the API, so the admin screen says so
before it fires — the confirmation spells out that existing transactions will go too, and points
at deactivation as the way to stop routing while keeping the history.

**Roles are a single column on `users`.** One role per user, stored directly on the row — no join
table. In the JWT the role has no `ROLE_` prefix; the filter adds it back when reading the token so
`hasRole` and `@PreAuthorize` work.

**`@PreAuthorize` denials need their own exception handler.** Method security throws
`AccessDeniedException` from inside the handler invocation, so it reaches the
`@RestControllerAdvice` rather than the security filter chain. Without an explicit
`@ExceptionHandler` for it, the catch-all `Exception` handler would answer **500 instead of 403**
on every wrong-role call. Same for `AuthenticationException`, which method security raises when
there is no `Authentication` at all.

**`created_at` / `updated_at` belong to the database.** Both columns carry `DEFAULT NOW()`, and the
entities mark them `@Generated` so Hibernate leaves them out of INSERT/UPDATE and reads back
whatever Postgres wrote. `updated_at` is maintained by the `trg_gateways_updated_at` trigger,
because Postgres has no `ON UPDATE CURRENT_TIMESTAMP`. One clock (the database's) for every writer,
including migrations and psql.

> ⚠️ **Known gap.** The JWT signing secret is still a constant in `JwtUtils`. Move it to
> configuration and out of source control before this goes anywhere real.

---

<p align="center">
  Built as the Fawry smart payment routing assignment.
</p>
