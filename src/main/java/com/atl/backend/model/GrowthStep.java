package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(GrowthStepId.class)
@Table(name = "growth_step")
public class GrowthStep {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private GrowthStepType type;

    @Id
    @Column(nullable = false, length = 31)
    private String title;

    @Id
    @Column(nullable = false)
    private Short level;

    @Column(nullable = false)
    private Integer x;

    @Column(name = "min_rating_over_last_x_games", precision = 3, scale = 2)
    private BigDecimal minRatingOverLastXGames;

    @Column(name = "min_refereed_games")
    private Short minRefereedGames;

    @Column(name = "min_rating_over_last_x_tournaments", precision = 3, scale = 2)
    private BigDecimal minRatingOverLastXTournaments;

    @Column(name = "min_coordinated_tournaments")
    private Short minCoordinatedTournaments;

    @Column(name = "coordinator_pay_per_game", precision = 10, scale = 2)
    private BigDecimal coordinatorPayPerGame;
}
