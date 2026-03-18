package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballTeamStatId.class)
@Table(name = "football_team_stat")
public class FootballTeamStat {
    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @Column(nullable = false)
    private Integer goals = 0;

    @Column(nullable = false)
    private Integer motm = 0;

    @Column(name = "red_cards", nullable = false)
    private Integer redCards = 0;

    @Column(name = "yellow_cards", nullable = false)
    private Integer yellowCards = 0;
}
