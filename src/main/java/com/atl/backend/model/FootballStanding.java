package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FootballStandingId.class)
@Table(name = "football_standing")
public class FootballStanding {
    @Id
    @Column(name = "tournament_id")
    private Integer tournamentId;

    @Id
    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "group_name")
    private String groupName = "NA";

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

    @Column(nullable = false)
    private Short rank;

    @Column(nullable = false)
    private Boolean qualified;
}
