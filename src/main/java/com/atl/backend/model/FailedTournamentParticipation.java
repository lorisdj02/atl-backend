package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "failed_tournament_participation")
public class FailedTournamentParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tournament_id")
    private Integer tournamentId;

    @Column(name = "team_id")
    private Integer teamId;
}
