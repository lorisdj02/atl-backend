package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@IdClass(CallupId.class)
@Table(name = "callup")
public class Callup {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;


    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /*
     * @Column(name = "team_id")
     * private Long teamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "prizeToRedeem")
    private BigDecimal prizeToRedeem;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
