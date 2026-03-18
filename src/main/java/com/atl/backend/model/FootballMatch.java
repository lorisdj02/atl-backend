package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "football_match")
public class FootballMatch {
    @Id
    private Integer id;

    @Column(name = "home_team_id")
    private Integer homeTeamId;

    @Column(name = "away_team_id")
    private Integer awayTeamId;

    @Column(name = "score_home")
    private Short scoreHome;

    @Column(name = "penalty_score_home")
    private Short penaltyScoreHome;

    @Column(name = "missed_penalty_home")
    private Short missedPenaltyHome;

    @Column(name = "score_away")
    private Short scoreAway;

    @Column(name = "penalty_score_away")
    private Short penaltyScoreAway;

    @Column(name = "missed_penalty_away")
    private Short missedPenaltyAway;

    private LocalDateTime halftime;

    @Column(name = "start_half")
    private LocalDateTime startHalf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private FootballMatchStatus status = FootballMatchStatus.NOT_SCHEDULED;
}
