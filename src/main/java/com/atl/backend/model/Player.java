package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Integer playerId;

    /*
     * @Column(name = "account_id", unique = true, nullable = false)
     * private Integer accountId;
     */

    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(nullable = false)
    private Integer points = 0;
}
