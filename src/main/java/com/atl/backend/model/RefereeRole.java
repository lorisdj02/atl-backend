package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(RefereeRoleId.class)
@Table(name = "referee_role")
public class RefereeRole {
    @Id
    @Column(name = "sport_id", nullable = false)
    private Integer sportId;

    @MapsId("sportId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false, insertable = false, updatable = false)
    private Sport sport;

    @Id
    @Column(nullable = false, length = 15)
    private String name;

    @Id
    @Column(nullable = false)
    private Short level;

    @Column(name = "pay_per_match", nullable = false, precision = 10, scale = 2)
    private BigDecimal payPerMatch;
}
