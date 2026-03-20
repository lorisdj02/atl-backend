package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(PlaysAtId.class)
@Table(name = "plays_at", uniqueConstraints = @UniqueConstraint(columnNames = { "player_id",
        "sport_category" }))
public class PlaysAt {
    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, insertable = false, updatable = false)
    private Team team;

    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, insertable = false, updatable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;
}
