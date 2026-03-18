package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;
}
