package com.atl.backend.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballTeamStatId.class)
@Table(name = "football_team_stat")
public class FootballTeamStat {
    @Id
    private Long playerId;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, insertable = false, updatable = false)
    private Player player;

    @Id
    private Long teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Short goals = 0;

    @Column(nullable = false)
    private Short motm = 0;

    @Column(name = "red_cards", nullable = false)
    private Short redCards = 0;

    @Column(name = "yellow_cards", nullable = false)
    private Short yellowCards = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
