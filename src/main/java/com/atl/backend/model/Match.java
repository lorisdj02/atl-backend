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
    private Long id;

    /*
     * @Column(name = "tournament_id", nullable = false)
     * private Long tournamentId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /*
     * @Column(name = "motm_player_id")
     * private Long motmPlayerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motm_player_id")
    private Player motmPlayer;

    @Column(name = "group_name", length = 3, nullable = false)
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

    @Column(name = "day")
    private Short day;

    /*
     * @Column(name = "final_stage_bracket_id")
     * private Long finalStageBracketId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_stage_bracket_id")
    private FinalStageBracket finalStageBracket;

    @Enumerated(EnumType.STRING)
    @Column(name = "leg_order", length = 15)
    private LegOrderType legOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
