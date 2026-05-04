package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@IdClass(RefereeAssignmentId.class)
@Table(name = "referee_assignment")
public class RefereeAssignment {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id", nullable = false)
    private Referee referee;

    @Column(nullable = false, length = 15)
    private String role;

    @Column(nullable = false)
    private BigDecimal pay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PresenceStatus status = PresenceStatus.ASSIGNED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
