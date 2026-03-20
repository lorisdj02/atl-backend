package com.atl.backend.model;

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
    private java.math.BigDecimal cost;
}
