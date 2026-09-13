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
7. [🏗️ Backend architecture](#️-backend-architecture)
8. [🎨 Frontend architecture](#-frontend-architecture)

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

---

## 🛠️ Built with

- **Backend:** Java 26, Spring Boot 4.1.1, Spring Data JPA, Spring Security (JWT), Liquibase,
  Lombok, Maven
- **Frontend:** Angular 21, TypeScript, CSS
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
hand.

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

Serves on **`http://localhost:4200`**. 

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
 ├── exception/      Custom exceptions + GlobalExceptionHandler
 ├── config/         CORS, security
 ├── security/       JWT filter, login filter, SecurityConfig, MyUserDetails
 └── validation/     @PasswordMatches
```

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
