package com.atl.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "private_booking")
public class PrivateBooking {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Booking booking;

    /*
     * @Column(name = "player_id", nullable = false)
     * private Integer playerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private BigDecimal cost;

    /*
     * @Column(name = "transaction_group_id", nullable = false)
     * private Integer transactionGroupId;
     */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
