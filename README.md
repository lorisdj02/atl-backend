# ATL - Core Backend API

![Java 25](https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot 4](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-626CD9?style=for-the-badge&logo=Stripe&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-DC382D?style=for-the-badge&logo=lombok&logoColor=white)

> A near-production stage backend platform for managing amateur sports tournaments end-to-end — from field bookings and team enrollment to live match events, dynamic standings, financial processing via Stripe, and a review-gated prize distribution system.

---

> 📱 **Frontend Application:** This backend powers the ATL mobile app. [View the Flutter repository here](https://github.com/lorisdj02/atl_app)

---

## 🏛 Architecture Overview

The project follows a **Layered Architecture** with the **MVC** pattern, organized into clearly separated packages:

| Layer | Package | Responsibility |
| :--- | :--- | :--- |
| **Presentation** | `controller` | REST endpoints, request validation |
| **Business Logic** | `service`, `service/orchestrator` | Domain rules, cross-domain transactional flows |
| **Persistence** | `repository`, `model` | JPA entities, Spring Data repositories |
| **Auth** | `auth` | `AccountContext` — thread-local identity for the authenticated user |
| **Cross-cutting** | `config`, `exception`, `dto`, `request`, `response` | Constants, error handling, data transfer |

A key design decision is the **Orchestrator pattern**: services like `PurchasesOrchestrator` and `ReviewsOrchestrator` coordinate complex, multi-step transactions that span multiple bounded contexts (e.g., paying a checkout session → creating a booking → generating a pending organization transaction — all atomically via `@Transactional`).

### Four Account Roles

Every `Account` can act as one or more of these roles, each with dedicated home screens and capabilities:

| Role | Description |
| :--- | :--- |
| **Player** | Joins teams, books fields (private or lobby), participates in tournaments, gets called up for matches |
| **Coordinator** | Creates and manages tournaments, controls match lifecycle (start, finish, phase progression), assigns prizes |
| **Referee** | Self-assigns to available matches, records live match events (goals, cards, penalties), gets paid per match |
| **Organization** | Owns physical fields, receives revenue when bookings start via a scheduled job |

---

## ⚙️ Key Technical Features

### 1. Dual Booking System — Private & Lobby

Players can book fields in two ways:
- **Private Booking**: A single player reserves an entire field for a given time slot. The price is dynamically calculated based on the field's `hourCostPrivate` rate and the booking duration.
- **Lobby Booking**: A player creates a public lobby, and other players can join by paying their share. The total field cost is split equally across `totalPlayers`, and the `feePerPlayer` is computed on creation. Other players join by purchasing `LOBBY_BOOKING_PARTICIPATE` checkout sessions.

**Concurrency control** is enforced at the service level: `BookingUtilitiesService.isFieldFree()` queries all existing bookings for that field in the requested time range, rejecting overlapping reservations with a `409 Conflict`. Opening/closing hours of each field are also validated.

**Refund policy** includes a time-based guard: bookings are only refundable if the start time is beyond a configurable threshold (`ATLCostants.getBookingRefundLimit()`), preventing last-minute abuse.

```mermaid
sequenceDiagram
    participant P as Player
    participant Orch as PurchasesOrchestrator
    participant BUtil as BookingUtilitiesService
    participant Pay as PaymentService
    participant DB as Database

    P->>Orch: Create Checkout Session
    Orch->>BUtil: isFieldFree(fieldId, start, end)?
    BUtil->>DB: Query overlapping bookings
    BUtil-->>Orch: Field available
    Orch->>Orch: Validate amount = hourCost x duration
    Orch->>Pay: Create CheckoutSession
    P->>Orch: Pay Checkout Session
    Orch->>Pay: Process payment (Wallet or Stripe)
    Pay->>DB: Save Purchase + Transactions
    Orch->>DB: Create Booking + Organization Transaction (PENDING)
    Orch-->>P: Purchase confirmed
```

### 2. Deferred Payment to Organizations (Scheduled Job)

When a booking is created, a **pending `Transaction`** is generated for the field's `Organization`. This transaction is **not confirmed** until the booking actually starts (`BookingStatus.IN_PROGRESS`). The `PurchasesOrchestrator.startBooking()` method redeems all related purchases, marks transactions as non-refundable, and confirms the organization's payout — ensuring organizations are only paid for bookings that actually take place.

### 3. Dynamic Standings & Live Match Events

The `FootballMatchEventService` handles granular in-match events (goals, cards, penalties, start/finish signals). When a `FINISH` event is inserted via the `ReviewsOrchestrator`, the system:
1. Persists the final match statistics
2. Triggers the `TournamentStandingService` to recalculate points, goal differences, and group standings
3. Automatically creates mutual review forms between the Referee and Coordinator

```mermaid
sequenceDiagram
    participant C as Coordinator
    participant Orch as ReviewsOrchestrator
    participant Event as FootballMatchEventService
    participant Stand as TournamentStandingService
    participant DB as Database

    C->>Orch: Insert match event (FINISH)
    Orch->>Event: Persist event
    Event->>DB: Save FootballMatchEvent
    Event->>Stand: Recalculate standings
    Stand->>DB: Update FootballStanding
    Orch->>Orch: finishMatchReviews()
    Orch->>DB: Create Review (Referee to Coordinator)
    Orch->>DB: Create Review (Coordinator to Referee)
```

### 4. Review-Gated Prize Distribution

After a tournament finishes, the `ReviewsOrchestrator` orchestrates a controlled prize flow:
1. **Tournament Start** → Review forms are pre-created for all called-up players (weighted: captains = `1.0`, others = `0.5`)
2. **Tournament Finish** → Reviews are unlocked (`PENDING`) for non-winning participants
3. **Prize Assignment** → The winning team's captain distributes prize money per player. Each prize creates a `PENDING` transaction linked to the player's review — **the prize is only credited to the wallet once the review is submitted**

```mermaid
flowchart TD
    A[Tournament Finishes] --> B[Reviews unlocked for losers]
    A --> C[Captain distributes prizes]
    C --> D[PENDING Transaction + Review per player]
    D --> E{Player submits review?}
    E -- Yes --> F[Transaction CONFIRMED]
    E -- No --> G[Prize stays PENDING]
```

---

## 💾 Data Model (ER Diagram)

```mermaid
erDiagram
    ACCOUNT {
        BIGINT id PK
        varchar username
        varchar email
        varchar role
    }
    PLAYER {
        BIGINT player_id PK
        BIGINT account_id FK
    }
    TEAM {
        BIGINT id PK
        varchar name
        varchar sport_category
    }
    TOURNAMENT {
        BIGINT id PK
        varchar name
        varchar status
        BIGINT field_id FK
    }
    MATCH {
        BIGINT id PK
        BIGINT tournament_id FK
    }
    FOOTBALL_MATCH {
        BIGINT id PK
        BIGINT home_team_id FK
        BIGINT away_team_id FK
    }
    FIELD {
        BIGINT id PK
        varchar name
        numeric hour_cost_private
        numeric hour_cost_tournament
    }
    WALLET {
        BIGINT id PK
        BIGINT account_id FK
    }
    TRANSACTION {
        BIGINT id PK
        varchar type
        numeric amount
        varchar status
    }
    CHECKOUT_SESSION {
        BIGINT id PK
        varchar type
        varchar status
    }
    PURCHASE {
        BIGINT id PK
        varchar status
        BIGINT transaction_group_id FK
    }
    BOOKING {
        BIGINT id PK
        BIGINT field_id FK
        varchar booking_type
        varchar status
    }
    REVIEW {
        BIGINT id PK
        BIGINT reviewer_id FK
        BIGINT reviewed_id FK
        numeric score
        varchar status
    }

    ACCOUNT ||--o| PLAYER : "acts as"
    ACCOUNT ||--|| WALLET : "owns"
    WALLET ||--o{ TRANSACTION : "logs"
    PLAYER ||--o{ TEAM : "captains"
    TEAM ||--o{ TOURNAMENT : "participates"
    TOURNAMENT }|--|| FIELD : "hosted at"
    TOURNAMENT ||--o{ MATCH : "schedules"
    MATCH ||--o| FOOTBALL_MATCH : "specializes into"
    FOOTBALL_MATCH }o--|| TEAM : "home / away"
    FIELD ||--o{ BOOKING : "reserved by"
    ACCOUNT ||--o{ CHECKOUT_SESSION : "initiates"
    CHECKOUT_SESSION ||--o| PURCHASE : "completes"
    ACCOUNT ||--o{ REVIEW : "writes"
```

---

## 🔌 API Overview

Endpoints are organized by domain across **10 REST controllers**, all prefixed with `/api/v1`.

### 🏠 Home & Profile

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/homes/player/{sportCategory}` | Player home screen with active bookings, teams, and upcoming tournaments |
| **GET** | `/homes/coordinator/{sportCategory}` | Coordinator dashboard with managed tournaments and match schedule |
| **GET** | `/homes/referee/{sportCategory}` | Referee hub with assigned and available matches |
| **GET** | `/homes/organization/{sportCategory}` | Organization overview with field bookings and revenue |
| **GET** | `/profiles/wallet` | Wallet balance and full transaction history |
| **GET** | `/profiles/purchases` | Complete purchase history across all checkout types |

### 👤 Accounts

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/accounts/account/register` | Register a new account with Wallet creation and optional invite-code reward |
| **POST** | `/accounts/login` | Authenticate and return user context with all active roles |
| **POST** | `/accounts/account/update-infos` | Update profile (supports both JSON and `multipart/form-data` for avatar upload) |
| **PATCH** | `/accounts/review/commit` | Submit a review — triggers linked transaction confirmation if applicable |

### ⚽ Teams & Tournaments

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/teams/create` | Create a new team for a given sport category |
| **POST** | `/teams/team/{id}/requests/send` | Send a join request to a team |
| **POST** | `/teams/team/{id}/manage/requests/accept` | Captain accepts player requests (single or batch) |
| **GET** | `/tournaments/{sportCategory}/filters` | Advanced tournament search (city, team size, fee range, prize range, name) |
| **POST** | `/tournaments/create` | Create and configure a new tournament |
| **POST** | `/tournaments/tournament/{id}/confirm-callups` | Confirm called-up players for a team's tournament participation |
| **PATCH** | `/tournaments/tournament/{id}/go-next-phase` | Advance tournament to the next phase (triggers match generation) |
| **PATCH** | `/tournaments/tournament/{id}/assign-prizes` | Distribute prize money to winning/finalist players |

### 🏟 Bookings & Matches

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/bookings/book-a-field/{sportCategory}` | Booking screen with available fields and cities |
| **GET** | `/bookings/free-fields/{sport}/max-players/{n}/city/{id}/start-time/{t1}/finish-time/{t2}` | Find available fields with concurrency check |
| **GET** | `/bookings/join-lobby-screen/{sport}/city/{id}` | Browse open lobbies with filters (players, time range) |
| **DELETE** | `/bookings/private-booking/{id}/refund` | Refund a private booking (enforces time-based refund policy) |
| **POST** | `/matches/match/{id}/football-event/insert` | Record a live match event (goal, card, start, finish, penalty shootout) |
| **POST** | `/matches/match/{id}/insert-referee-assignment/{role}` | Referee self-assigns to an available match |
| **GET** | `/matches/{sport}/referee/available-matches` | List unassigned matches a referee can claim |

### 💳 Payments

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/payments/checkout-session/create` | Initialize a checkout session (`TOURNAMENT_FEE`, `PRIVATE_BOOKING`, `LOBBY_BOOKING_CREATE`, `LOBBY_BOOKING_PARTICIPATE`) |
| **PATCH** | `/payments/checkout-session/{id}/pay` | Execute payment via Wallet balance and/or Stripe PaymentIntent |
| **POST** | `/payments/checkout-session/{id}/create-payment-intent` | Generate a Stripe PaymentSheet for client-side confirmation |

---

## 🚀 Project Status & Future Roadmap

The system is in a **near-production stage**. The core architecture, domain logic, and payment flows are fully implemented and functional. The following improvements are planned before public release:

1. **🔄 WebSocket Integration for Real-Time Updates**
   Currently, match events and standings updates require the client to re-fetch data via REST. Integrating WebSockets (STOMP over SockJS) would enable pushing live updates — e.g., when a goal event is inserted, all connected clients viewing that match would receive the updated score and standings instantly.

2. **🔒 Advanced Security & Restriction Enforcement**
   The current auth layer uses `AccountContext` for identity. The next step is enforcing granular role-based access control via Spring Security filters, adding rate limiting to prevent API abuse, and leveraging the existing `AccountRestriction` entity to block misbehaving users at the middleware level.

3. **⚡ Asynchronous Processing for Webhooks & Heavy Workloads**
   Integrating a message broker (RabbitMQ or Kafka) to decouple computationally intensive tasks — such as complex round-robin bracket generation, batch notification delivery, and Stripe webhook event processing — from the synchronous request/response cycle.

4. **☁️ Cloud-Native Storage Migration**
   Transitioning from the current local file storage (`.storage/`) to a CDN-backed cloud object storage (AWS S3 or GCS) for scalable and resilient media delivery (team logos, player avatars).

---

## 🛠 Local Setup

### Prerequisites
- **Java 25**
- **PostgreSQL 16+**
- **Maven** (wrapper included)

### Database Configuration
Ensure PostgreSQL is running locally with these default credentials (from `application.properties`):

| Property | Value |
| :--- | :--- |
| URL | `jdbc:postgresql://localhost:5432/atl_database` |
| Username | `postgres` |
| Password | `postgres` |

### Running the Application

```bash
./mvnw spring-boot:run
```

The REST API will be available at **`http://localhost:8081`**.
