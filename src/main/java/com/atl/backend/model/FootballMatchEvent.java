package com.atl.backend.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.atl.backend.dto.FootballMatchEventModifiedValues;
import com.atl.backend.model.enums.*;
import com.atl.backend.util.FootballMatchEventModifiedTablesConverter;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "football_match_event")
public class FootballMatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "match_id", nullable = false)
     * private Long matchId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private FootballMatch match;

    /*
     * @Column(name = "team_id")
     * private Long teamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    /*
     * @Column(name = "player_id")
     * private Long playerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private FootballEventType type;

    @Column(nullable = false)
    private Short minute;

    @Column(name = "additional_minute", nullable = false)
    private Short additionalMinute;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "modified_values", columnDefinition = "jsonb")
    @Convert(converter = FootballMatchEventModifiedTablesConverter.class)
    private FootballMatchEventModifiedValues modifiedValues;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
