package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "lobby_booking_id", nullable = false)
    private Integer lobbyBookingId;

    @Column(name = "players_paid", nullable = false)
    private Short playersPaid = 1;

    @Column(nullable = false)
    private java.math.BigDecimal cost;

    @Column(name = "transaction_group", nullable = false)
    private Integer transactionGroup;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}
