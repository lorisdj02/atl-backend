package com.atl.backend.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FavoriteTournamentId.class)
@Table(name = "favorite_tournament")
public class FavoriteTournament {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
