package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tournament")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "coordinator_id", nullable = false)
    private Integer coordinatorId;

    @Column(name = "field_id", nullable = false)
    private Integer fieldId;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "sport_id", nullable = false)
    private Integer sportId;

    @Column(name = "\"public\"", nullable = false)
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

    @Column(name = "team_qualified", nullable = false)
    private Short teamQualified = 4;

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
}
