package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sports")
public class Sport {
    @Id
    private Integer id;

    @Column(nullable = false, length = 31)
    private String name;

    @Column(name = "sport_category", nullable = false, length = 15)
    private String sportCategory;

    @Column(name = "min_players_per_team", nullable = false)
    private Integer minPlayersPerTeam;

    @Column(name = "max_players_per_team", nullable = false)
    private Integer maxPlayersPerTeam;

    @Column(name = "minutes_per_round", nullable = false)
    private Integer minutesPerRound;

    @Column(name = "minutes_per_pause", nullable = false)
    private Integer minutesPerPause;

    @Column(name = "min_referee", nullable = false)
    private Integer minReferee;
}
