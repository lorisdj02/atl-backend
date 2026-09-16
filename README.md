# ATL - Core Backend API

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-626CD9?style=for-the-badge&logo=Stripe&logoColor=white)

> A robust, near-production stage backend platform powering complex sports tournament management, coordinating team registrations, dynamic scheduling, and secure financial processing.

---

📱 **Frontend Application:** This backend powers the ATL mobile app. [View the Flutter repository here](https://github.com/lorisdj02/atl_app)

---

## 🏛 Architecture & Core Pattern

The project follows a **Layered Architecture** utilizing the **MVC (Model-View-Controller)** pattern. Business logic is strictly decoupled from presentation, employing specialized **Orchestrators** (e.g., `PurchasesOrchestrator`, `ReviewsOrchestrator`) to elegantly manage complex transactional flows and cross-domain operations (such as completing a payment and enrolling a team in a tournament simultaneously).

---

## ⚙️ Key Features & System Flows

The backend handles highly articulated domain logic to ensure the entire tournament ecosystem is seamless, from enrollment to prize distribution and reviews.

### 1. Match Generation and Pairings
The pairing logic is dynamically managed based on the tournament phase (e.g., round-robin or knockout). The `MatchCombinationService` handles fair generation of matchups, coordinating team availability and automatically assigning available `Field`s.

```mermaid
sequenceDiagram
    participant Org as Organizer
    participant T_Service as TournamentPhaseService
    participant M_Service as MatchCombinationService
    participant DB as Database
    
    Org->>T_Service: Move to next phase
    T_Service->>M_Service: Request match pairings generation
    M_Service->>DB: Fetch qualified teams and fields
    M_Service-->>M_Service: Execute algorithm (Round-Robin / Knockout)
    M_Service->>DB: Save new Matches
    M_Service-->>T_Service: Calendar generated
    T_Service-->>Org: Success
```

### 2. Real-Time Standings Update
During a match, every significant event (goals, penalties) is promptly handled. At the end of the match, the `TournamentStandingService` dynamically recalculates the standings, updating points, goal differences, and overall statistics, ensuring data consistency.

```mermaid
sequenceDiagram
    participant Ref as Referee
    participant Match_S as MatchService
    participant Stand_S as StandingService
    participant DB as Database

    Ref->>Match_S: Register match end (Final result)
    Match_S->>DB: Save Match statistics
    Match_S->>Stand_S: Trigger standings update
    Stand_S->>DB: Load current phase standings
    Stand_S-->>Stand_S: Recalculate Points & Goal Difference
    Stand_S->>DB: Persist new standings (FootballStanding)
    Stand_S-->>Match_S: Update completed
```

### 3. Reviews Orchestration
The `ReviewsOrchestrator` acts as a vital layer at the end of an event, ensuring that reviews for organizers and referees are enabled only when a tournament concludes. It atomically updates global average ratings and distributes the evaluation outcomes.

```mermaid
flowchart LR
    A[Tournament Ends] --> B{Did the user<br/>participate?}
    B -- Yes --> C[Unlock Review Form]
    B -- No --> D[Review Denied]
    C --> E[Submit Review (Score 1-5)]
    E --> F[ReviewsOrchestrator]
    F --> G[(Database: Save Review)]
    F --> H[(Database: Update Average Rating)]
```

---

## 💾 Data Model (ER Diagram)

Below is a consolidated Entity-Relationship diagram highlighting the core entities across the major bounded contexts (Accounts, Tournaments, and Finances).

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
    }
    WALLET {
        BIGINT id PK
        BIGINT account_id FK
    }
    TRANSACTION {
        BIGINT id PK
        varchar type
        numeric amount
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
```

---

## 🔌 API Overview

The REST API exposes structured endpoints based on core controllers. 

| Method | Endpoint | Description | Domain |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/accounts/login` | Authenticates the user and returns context with various roles. | Accounts |
| **GET** | `/api/v1/tournaments/{sportCategory}/filters` | Advanced tournament search (filters by city, fee, team size, prize pool). | Tournaments |
| **POST** | `/api/v1/tournaments/tournament/{id}/confirm-callups` | Official confirmation of called-up players (Call-ups) for an enrolled team. | Teams / Tournaments |
| **POST** | `/api/v1/payments/checkout-session/create` | Initializes a secure checkout session (e.g. for entry fee). | Financial |
| **PATCH** | `/api/v1/payments/checkout-session/{id}/pay` | Executes payment by deducting Wallet funds or confirming Stripe intent. | Financial |
| **PATCH** | `/api/v1/accounts/review/commit` | Confirms and persists a post-tournament review via Orchestrator. | Accounts |

---

## 🚀 Known Limitations & Future Roadmap

The system is currently in a **near-production stage**. While the core architecture is highly robust, there are a few final features required before a full public release:

1. **WebSockets for Real-Time Updates:** Currently, match events and standings are updated synchronously via REST. Integrating WebSockets (e.g., STOMP over WebSocket) is needed to push automatic live updates to clients when a match event (e.g., a goal) is added.
2. **Advanced Security & Restriction Checks:** Further fine-tuning of Spring Security is required to enforce strict role-based constraints, payload validation, and abuse prevention (rate limiting) across all endpoints.
3. **Asynchronous Processing:** Integration of a Message Broker (e.g., RabbitMQ or Apache Kafka) to decouple computationally heavy tasks, such as complex round-robin bracket generation or asynchronous Stripe webhooks.
4. **Cloud-Native Storage Migration:** Transitioning from local file storage (`.storage`) to a Cloud Object Storage (Amazon S3 or GCS) coupled with a CDN for fast, distributed asset delivery.

---

## 🛠 Local Setup

### Prerequisites
* Java 25
* PostgreSQL 16+

### Database Configuration
Verify that your local PostgreSQL instance is running with the default credentials (found in `application.properties`):
* **URL:** `jdbc:postgresql://localhost:5432/atl_database`
* **Username:** `postgres`
* **Password:** `postgres`

### Running the Application
The project uses Maven Wrapper. To boot the development server locally:

```bash
./mvnw spring-boot:run
```

The REST service will be listening on `http://localhost:8081`.
