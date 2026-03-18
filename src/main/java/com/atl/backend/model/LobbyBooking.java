package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lobby_booking")
public class LobbyBooking {
    @Id
    private Integer id;

    @Column(name = "owner_player_id", nullable = false)
    private Integer ownerPlayerId;

    @Column(name = "players_to_find", nullable = false)
    private Short playersToFind;

    @Column(name = "total_players", nullable = false)
    private Short totalPlayers;

    @Column(name = "fee_per_player", nullable = false)
    private java.math.BigDecimal feePerPlayer;
}
