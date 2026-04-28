package com.atl.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "team", uniqueConstraints = { @UniqueConstraint(columnNames = { "captain_player_id", "sport_category" }),
        @UniqueConstraint(columnNames = { "vice1_player_id", "sport_category" }),
        @UniqueConstraint(columnNames = { "vice2_player_id", "sport_category" }),
        @UniqueConstraint(columnNames = { "name", "sport_category" }) })
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "city_id")
     * private Long cityId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    /*
     * @Column(name = "captain_player_id")
     * private Long captainPlayerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_player_id")
    private Player captainPlayer;

    /*
     * @Column(name = "vice1_player_id")
     * private Long vice1PlayerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vice1_player_id")
    private Player vice1Player;

    /*
     * @Column(name = "vice2_player_id")
     * private Long vice2PlayerId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vice2_player_id")
    private Player vice2Player;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false, length = 31)
    private String name;

    @Column(nullable = false, length = 3)
    private String alias;

    @Column(length = 511)
    private String description;

    @Column(name = "num_players", nullable = false)
    private Short numPlayers = 1;

    @Column(name = "max_players", nullable = false)
    private Short maxPlayers = 24;

    @Column(name = "rank_pts", nullable = false)
    private Integer rankPts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    @Column(name = "vip", nullable = false)
    private Boolean vip = false;

    @Column(name = "visible")
    private Boolean visible = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public List<Player> getViceCaptainPlayers() {
        List<Player> viceCaptains = new ArrayList<>();
        if (vice1Player != null) {
            viceCaptains.add(vice1Player);
        }
        if (vice2Player != null) {
            viceCaptains.add(vice2Player);
        }
        return viceCaptains;
    }
}
