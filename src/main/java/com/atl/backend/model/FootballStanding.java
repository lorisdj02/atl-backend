package com.atl.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballStandingId.class)
@Table(name = "football_standing")
public class FootballStanding {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "group_name", length = 3)
    private String groupName;

    @Column(nullable = false)
    private Short pts = 0;

    @Column(nullable = false)
    private Short won = 0;

    @Column(nullable = false)
    private Short drawn = 0;

    @Column(nullable = false)
    private Short lost = 0;

    @Column(name = "goals_scored", nullable = false)
    private Short goalsScored = 0;

    @Column(name = "goals_conceded", nullable = false)
    private Short goalsConceded = 0;

    @Column(name = "rank_pos", nullable = false)
    private Short rankPos = 1;

    @Column(nullable = false)
    private Boolean qualified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Short getGoalDifference() {
        return (short) (goalsScored - goalsConceded);
    }
}
