package com.atl.backend.model;

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

    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @Column(nullable = false, length = 31)
    private String position = "GROUP_STAGE";
}
