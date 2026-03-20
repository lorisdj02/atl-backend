package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lobby_booking_participate")
public class LobbyBookingParticipate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "player_id", nullable = false)
     * private Integer playerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /*
     * @Column(name = "lobby_booking_id", nullable = false)
     * private Integer lobbyBookingId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_booking_id", nullable = false)
    private LobbyBooking lobbyBooking;

    @Column(name = "players_paid", nullable = false)
    private Short playersPaid = 1;

    @Column(nullable = false)
    private java.math.BigDecimal cost;

    /*
     * @Column(name = "transaction_group", nullable = false)
     * private Integer transactionGroup;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_group_id", nullable = false)
    private TransactionGroup transactionGroup;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}
