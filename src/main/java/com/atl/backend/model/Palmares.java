package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(PalmaresId.class)
@Table(name = "palmares")
public class Palmares {
    @Id
    @Column(name = "tournament_id", nullable = false)
    private Integer tournamentId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, insertable = false, updatable = false)
    private Tournament tournament;

    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, insertable = false, updatable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private PalmaresEnum position = PalmaresEnum.GROUP_STAGE;
}
