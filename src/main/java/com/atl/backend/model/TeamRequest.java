package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(TeamRequestId.class)
@Table(name = "team_request")
public class TeamRequest {
    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @MapsId("teamId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, insertable = false, updatable = false)
    private Team team;

    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, insertable = false, updatable = false)
    private Player player;
}
