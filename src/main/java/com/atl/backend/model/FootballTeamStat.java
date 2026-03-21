package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballTeamStatId.class)
@Table(name = "football_team_stat")
public class FootballTeamStat {
    @Id
    private Integer playerId;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, insertable = false, updatable = false)
    private Player player;

    @Id
    private Integer teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Integer goals = 0;

    @Column(nullable = false)
    private Integer motm = 0;

    @Column(name = "red_cards", nullable = false)
    private Integer redCards = 0;

    @Column(name = "yellow_cards", nullable = false)
    private Integer yellowCards = 0;
}
