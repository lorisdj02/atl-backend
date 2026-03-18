package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "city_id")
    private Integer cityId;

    @Column(name = "captain_id", nullable = false)
    private Integer captainId;

    @Column(name = "vice1_id")
    private Integer vice1Id;

    @Column(name = "vice2_id")
    private Integer vice2Id;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false, length = 31)
    private String name;

    @Column(nullable = false, length = 3)
    private String alias;

    @Column(length = 511)
    private String description;

    @Column(nullable = false)
    private Integer players = 1;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 24;

    @Column(name = "rank_pts", nullable = false)
    private Integer rankPts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    private Boolean vip = false;
}
