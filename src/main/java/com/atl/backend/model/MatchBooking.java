package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match_booking")
public class MatchBooking {
    @Id
    private Integer id;

    /*
     * @Column(name = "coordinator_id", nullable = false)
     * private Integer coordinatorId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id", nullable = false)
    private Player coordinator;

    /*
     * @Column(name = "match_id", nullable = false)
     * private Integer matchId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
}
