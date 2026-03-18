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

    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private PalmaresEnum position = PalmaresEnum.GROUP_STAGE;
}
