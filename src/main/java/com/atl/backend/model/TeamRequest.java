package com.atl.backend.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(TeamRequestId.class)
@Table(name = "team_request")
public class TeamRequest {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
