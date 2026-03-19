package com.atl.backend.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "football_match_event")
public class FootballMatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "match_id", nullable = false)
     * private Integer matchId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private FootballMatch match;

    /*
     * @Column(name = "team_id")
     * private Integer teamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    /*
     * @Column(name = "player_id")
     * private Integer playerId;
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
    @Column(name = "modified_table", columnDefinition = "jsonb")
    private String modifiedTable;
}
