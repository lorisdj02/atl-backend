```mermaid
erDiagram
    %% Core Entities
    ACCOUNT {
        BIGINT id PK
        varchar username
        varchar email
        varchar role "PLAYER, COORDINATOR, REFEREE, ORG"
        varchar status
    }
    
    PLAYER {
        BIGINT player_id PK
        BIGINT account_id FK
        int points
    }
    
    TEAM {
        BIGINT id PK
        varchar name
        varchar sport_category
        BIGINT captain_player_id FK
        int rank_pts
    }
    
    TOURNAMENT {
        BIGINT id PK
        varchar name
        varchar status "CREATED, GROUP_STAGE, etc."
        numeric fee
        BIGINT coordinator_id FK
        BIGINT field_id FK
    }
    
    MATCH {
        BIGINT id PK
        BIGINT tournament_id FK
        timestamptz scheduled_start
        varchar sport_category
    }
    
    FOOTBALL_MATCH {
        BIGINT id PK
        BIGINT home_team_id FK
        BIGINT away_team_id FK
        smallint score_home
        smallint score_away
    }
    
    FIELD {
        BIGINT id PK
        varchar name
        varchar address
        BIGINT organization_id FK
    }

    WALLET {
        BIGINT id PK
        BIGINT account_id FK
    }
    
    TRANSACTION {
        BIGINT id PK
        BIGINT account_id FK
        varchar type
        numeric amount
        varchar status
    }

    %% Relationships
    ACCOUNT ||--o| PLAYER : "acts as"
    ACCOUNT ||--|| WALLET : "owns"
    WALLET ||--o{ TRANSACTION : "logs"
    
    PLAYER ||--o{ TEAM : "captains"
    TEAM ||--o{ TOURNAMENT : "participates (via tournament_participation)"
    
    TOURNAMENT }|--|| FIELD : "hosted at"
    TOURNAMENT ||--o{ MATCH : "schedules"
    
    MATCH ||--o| FOOTBALL_MATCH : "specializes into"
    FOOTBALL_MATCH }o--|| TEAM : "home / away"
