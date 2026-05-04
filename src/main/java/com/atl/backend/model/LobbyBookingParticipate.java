package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "lobby_booking_participate")
public class LobbyBookingParticipate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "player_id", nullable = false)
     * private Long playerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /*
     * @Column(name = "lobby_booking_id", nullable = false)
     * private Long lobbyBookingId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_booking_id", nullable = false)
    private LobbyBooking lobbyBooking;

    @Column(name = "players_paid", nullable = false)
    private Short playersPaid = 1;

    @Column(nullable = false)
    private BigDecimal cost;

    /*
     * @Column(name = "transaction_group", nullable = false)
     * private Long transactionGroup;
     */

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
