package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@IdClass(TournamentParticipationId.class)
@Table(name = "tournament_participation")
public class TournamentParticipation {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "to_pay", nullable = false)
    private BigDecimal toPay;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
