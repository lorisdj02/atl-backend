package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "football_match")
public class FootballMatch {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Match match;

    /*
     * @Column(name = "home_team_id")
     * private Integer homeTeamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    /*
     * @Column(name = "away_team_id")
     * private Integer awayTeamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

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

    @Column(name = "halftime")
    private LocalDateTime halftime;

    @Column(name = "start_half")
    private LocalDateTime startHalf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private FootballMatchStatus status = FootballMatchStatus.NOT_SCHEDULED;

    public List<Team> getTeams() {
        return List.of(homeTeam, awayTeam);
    }
}
