package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "football_match")
public class FootballMatch {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Match match;

    /*
     * @Column(name = "home_team_id")
     * private Long homeTeamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    /*
     * @Column(name = "away_team_id")
     * private Long awayTeamId;
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
    private Instant halftime;

    @Column(name = "start_half")
    private Instant startHalf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private FootballMatchStatus status = FootballMatchStatus.NOT_SCHEDULED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public List<Team> getTeams() {
        return List.of(homeTeam, awayTeam);
    }

    public Boolean isHomeWinningByOneGoal() {
        return scoreHome != null && scoreAway != null && scoreHome.equals((short) (scoreAway + 1));
    }

    public Boolean isAwayWinningByOneGoal() {
        return scoreHome != null && scoreAway != null && scoreAway.equals((short) (scoreHome + 1));
    }

    public Boolean isDraw() {
        return scoreHome != null && scoreAway != null && scoreHome.equals(scoreAway);
    }

    public Boolean isFinished(){
        return match.getFinishMatch() != null;
    }

    public Boolean isStarted(){
        return match.getStartMatch() != null;
    }

    public Boolean isHalftime(){
        return halftime != null && !isSecondHalfStarted();
    }

    public Boolean isSecondHalfStarted(){
        return startHalf != null;
    }

    public Boolean isPenaltyShootOut(){
        return status == FootballMatchStatus.PENALTY_SHOOT_OUT;
    }
}
