package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tournament_id", nullable = false)
    private Integer tournamentId;

    private Integer motm;

    @Column(name = "group_name", length = 3)
    private String groupName;

    @Column(name = "scheduled_start")
    private LocalDateTime scheduledStart;

    @Column(name = "start_match")
    private LocalDateTime startMatch;

    @Column(name = "finish_match")
    private LocalDateTime finishMatch;

    @Column(name = "coordinator_status", nullable = false)
    private String coordinatorStatus = "ASSIGNED";

    @Column(name = "sport_category", nullable = false, length = 15)
    private String sportCategory;
}
