package com.atl.backend.model;

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

    @MapsId("tournamentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", insertable = false, updatable = false)
    private Tournament tournament;

    @Id
    @Column(name = "team_id")
    private Integer teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @Column(name = "group_name", length = 3)
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

    @Column(name = "rank_pos", nullable = false)
    private Short rankPos;

    @Column(nullable = false)
    private Boolean qualified;
}
