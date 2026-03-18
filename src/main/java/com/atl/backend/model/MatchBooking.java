package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match_booking")
public class MatchBooking {
    @Id
    private Integer id;

    @Column(name = "coordinator_id", nullable = false)
    private Integer coordinatorId;

    @Column(name = "match_id", nullable = false)
    private Integer matchId;
}
