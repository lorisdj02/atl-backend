package com.atl.backend.dto;

import java.math.BigDecimal;

import com.atl.backend.model.GrowthStep;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrowthStepDTO {
    @NotNull
    @JsonProperty("title")
    private String title;

    @NotNull
    @JsonProperty("level")
    private Short level;

    @NotNull
    @JsonProperty("x")
    private Integer x;

    @JsonProperty("minRatingOverLastXGames")
    private Double minRatingOverLastXGames;

    @JsonProperty("minRefereedGames")
    private Short minRefereedGames;

    @JsonProperty("minRatingOverLastXTournaments")
    private Double minRatingOverLastXTournaments;

    @JsonProperty("minCoordinatedTournaments")
    private Short minCoordinatedTournaments;

    @JsonProperty("coordinatorPayPerGame")
    private BigDecimal coordinatorPayPerGame;

    public static GrowthStepDTO fromGrowthStep(GrowthStep growthStep) {
        if (growthStep == null) {
            return null;
        }

        GrowthStepDTO dto = new GrowthStepDTO();
        dto.setTitle(growthStep.getTitle());
        dto.setLevel(growthStep.getLevel());
        dto.setX(growthStep.getX());
        dto.setMinRatingOverLastXGames(growthStep.getMinRatingOverLastXGames() != null ? growthStep.getMinRatingOverLastXGames().doubleValue() : null);
        dto.setMinRefereedGames(growthStep.getMinRefereedGames());
        dto.setMinRatingOverLastXTournaments(growthStep.getMinRatingOverLastXTournaments() != null ? growthStep.getMinRatingOverLastXTournaments().doubleValue() : null);
        dto.setMinCoordinatedTournaments(growthStep.getMinCoordinatedTournaments());
        dto.setCoordinatorPayPerGame(growthStep.getCoordinatorPayPerGame());
        return dto;
    }
}
