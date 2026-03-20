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

    @MapsId("tournamentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, insertable = false, updatable = false)
    private Tournament tournament;

    @Id
    @Column(name = "team_id")
    private Integer teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, insertable = false, updatable = false)
    private Team team;

    @Column(name = "to_pay", nullable = false)
    private BigDecimal toPay;
}
