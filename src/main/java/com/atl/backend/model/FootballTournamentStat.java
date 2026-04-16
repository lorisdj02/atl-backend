package com.atl.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballTournamentStatsId.class)
@Table(name = "football_tournament_stat")
public class FootballTournamentStat {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /*
    @Column(name = "team_id", nullable = false)
    private Integer teamId;
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Short goals = 0;

    @Column(nullable = false)
    private Short motm = 0;

    @Column(name = "yellow_cards", nullable = false)
    private Short yellowCards = 0;

    @Column(name = "red_cards", nullable = false)
    private Short redCards = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
