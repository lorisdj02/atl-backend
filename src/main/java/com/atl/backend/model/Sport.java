package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sport")
public class Sport {
    @Id
    private Integer id;

    @Column(nullable = false, length = 31)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    @Column(name = "min_players_per_team", nullable = false)
    private Short minPlayersPerTeam;

    @Column(name = "max_players_per_team", nullable = false)
    private Short maxPlayersPerTeam;

    @Column(name = "minutes_per_round", nullable = false)
    private Short minutesPerRound;

    @Column(name = "minutes_per_pause", nullable = false)
    private Short minutesPerPause;

    @Column(name = "min_referee", nullable = false)
    private Short minReferee;
}
