package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.Instant;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tournament")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "coordinator_id", nullable = false)
     * private Long coordinatorId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id", nullable = false)
    private Coordinator coordinator;

    /*
     * @Column(name = "field_id", nullable = false)
     * private Long fieldId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    /*
     * @Column(name = "city_id", nullable = false)
     * private Long cityId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    /*
     * @Column(name = "sport_id", nullable = false)
     * private Long sportId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(nullable = false)
    private Boolean profit = true;

    @Column(name = "pay_coordinator", nullable = false)
    private BigDecimal payCoordinator;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private BigDecimal prize1;

    @Column(nullable = false)
    private BigDecimal prize2 = BigDecimal.ZERO;

    @Column(name = "num_teams", nullable = false)
    private Short numTeams = 8;

    @Column(name = "num_groups", nullable = false)
    private Short numGroups = 2;

    @Column(name = "round2_group_stage", nullable = false)
    private Boolean round2GroupStage = false;

    @Column(name = "round2_final_stage", nullable = false)
    private Boolean round2FinalStage = false;

    @Column(name = "num_teams_qualified", nullable = false)
    private Short numTeamsQualified = 4;

    @Column(length = 31)
    private String name;

    @Column(nullable = false, length = 511)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private TournamentStatus status = TournamentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private TournamentPhase phase = TournamentPhase.GROUP;

    @Column(name = "paid_teams", nullable = false)
    private Short paidTeams = 0;

    @Column(name = "atl_event", nullable = false)
    private Boolean atlEvent = false;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Boolean isTournamentActive() {
        return (this.getStatus() != TournamentStatus.CANCELED &&
                    this.getStatus() != TournamentStatus.FINISHED &&
                    this.getStatus() != TournamentStatus.PENDING_PAYMENTS);
    }
}
