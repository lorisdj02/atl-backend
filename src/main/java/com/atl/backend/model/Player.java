package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Column(name = "account_id", unique = true, nullable = false)
    private Integer accountId;

    @Column(nullable = false)
    private Integer points = 0;
}
