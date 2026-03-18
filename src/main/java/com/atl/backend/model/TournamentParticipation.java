package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(TournamentParticipationId.class)
@Table(name = "tournament_participation")
public class TournamentParticipation {
    @Id
    @Column(name = "tournament_id")
    private Integer tournamentId;

    @Id
    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "to_pay", nullable = false)
    private BigDecimal toPay;
}
