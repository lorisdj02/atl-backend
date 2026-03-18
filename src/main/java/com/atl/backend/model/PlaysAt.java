package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(PlaysAtId.class)
@Table(name = "plays_at")
public class PlaysAt {
    @Id
    @Column(name = "team_id", nullable = false)
    private Integer teamId;

    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;
}
