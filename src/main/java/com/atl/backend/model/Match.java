package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "coordinator_status", nullable = false)
    private PresenceStatus coordinatorStatus = PresenceStatus.ASSIGNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;
}
