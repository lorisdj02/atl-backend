package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "private_booking")
public class PrivateBooking {
    @Id
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(nullable = false)
    private java.math.BigDecimal cost;
}
