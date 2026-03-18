package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "football_match_event")
public class FootballMatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "match_id", nullable = false)
    private Integer matchId;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "player_id")
    private Integer playerId;

    @Column(nullable = false, length = 31)
    private String type;

    @Column(nullable = false)
    private Short minute;

    @Column(name = "additional_minute", nullable = false)
    private Short additionalMinute;

    @Column(name = "modified_table", columnDefinition = "jsonb")
    private String modifiedTable;
}
