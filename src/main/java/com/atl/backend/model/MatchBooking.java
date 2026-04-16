package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match_booking", uniqueConstraints = @UniqueConstraint(columnNames = "match_id"))
public class MatchBooking {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Booking booking;


    /*
     * @Column(name = "coordinator_id", nullable = false)
     * private Integer coordinatorId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id", nullable = false)
    private Coordinator coordinator;

    /*
     * @Column(name = "match_id", nullable = false)
     * private Integer matchId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
}
