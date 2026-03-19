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

    @MapsId("tournamentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, insertable = false, updatable = false)
    private Tournament tournament;

    @Id
    @Column(name = "player_id")
    private Integer playerId;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, insertable = false, updatable = false)
    private Player player;

    /*
     * @Column(name = "team_id")
     * private Integer teamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "prizeToRedeem")
    private BigDecimal prizeToRedeem;
}
