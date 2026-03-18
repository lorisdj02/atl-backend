package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FavoriteTournamentId.class)
@Table(name = "favorite_tournament")
public class FavoriteTournament {
    @Id
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Id
    @Column(name = "tournament_id", nullable = false)
    private Integer tournamentId;
}
