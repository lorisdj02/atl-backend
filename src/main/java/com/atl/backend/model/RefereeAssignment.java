package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "referee_assignment")
public class RefereeAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "match_id", nullable = false)
    private Integer matchId;

    @Column(name = "referee_id", nullable = false)
    private Integer refereeId;

    @Column(nullable = false, length = 15)
    private String role;

    @Column(nullable = false)
    private BigDecimal pay;

    @Column(nullable = false, length = 15)
    private String status = "ASSIGNED";
}
