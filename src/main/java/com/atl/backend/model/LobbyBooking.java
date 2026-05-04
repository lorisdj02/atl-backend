package com.atl.backend.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lobby_booking")
public class LobbyBooking {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Booking booking;

    /*
     * @Column(name = "owner_player_id", nullable = false)
     * private Long ownerPlayerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_player_id", nullable = false)
    private Player ownerPlayer;

    @Column(name = "players_to_find", nullable = false)
    private Short playersToFind;

    @Column(name = "total_players", nullable = false)
    private Short totalPlayers;

    @Column(name = "fee_per_player", nullable = false)
    private BigDecimal feePerPlayer;

    public short getCurrentPlayers() {
        return (short) (totalPlayers - playersToFind);
    }

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
