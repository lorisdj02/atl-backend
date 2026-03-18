package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(RefereeAssignmentId.class)
@Table(name = "referee_assignment")
public class RefereeAssignment {
    @Id
    @Column(name = "match_id", nullable = false)
    private Integer matchId;

    @Id
    @Column(name = "referee_id", nullable = false)
    private Integer refereeId;

    @Column(nullable = false, length = 15)
    private String role;

    @Column(nullable = false)
    private BigDecimal pay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PresenceStatus status = PresenceStatus.ASSIGNED;
}
