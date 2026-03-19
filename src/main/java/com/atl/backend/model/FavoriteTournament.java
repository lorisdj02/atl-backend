package com.atl.backend.model;

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

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, insertable = false, updatable = false)
    private Account account;

    @Id
    @Column(name = "tournament_id", nullable = false)
    private Integer tournamentId;

    @MapsId("tournamentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, insertable = false, updatable = false)
    private Tournament tournament;
}
