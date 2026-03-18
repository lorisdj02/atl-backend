package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(CallupId.class)
@Table(name = "callup")
public class Callup {
    @Id
    @Column(name = "tournament_id")
    private Integer tournamentId;

    @Id
    @Column(name = "team_id")
    private Integer teamId;

    @Id
    @Column(name = "player_id")
    private Integer playerId;

    @Column(name = "prizeToRedeem")
    private BigDecimal prizeToRedeem;
}
