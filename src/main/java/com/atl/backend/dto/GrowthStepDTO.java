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
    private Short x;

    @JsonProperty("minRatingOverLastXMatches")
    private Double minRatingOverLastXMatches;

    @JsonProperty("minRefereedMatches")
    private Short minRefereedMatches;

    @JsonProperty("minRatingOverLastXTournaments")
    private Double minRatingOverLastXTournaments;

    @JsonProperty("minCoordinatedTournaments")
    private Short minCoordinatedTournaments;

    @JsonProperty("coordinatorPayPerMatch")
    private BigDecimal coordinatorPayPerMatch;

    public static GrowthStepDTO fromGrowthStep(GrowthStep growthStep) {
        if (growthStep == null) {
            return null;
        }

        GrowthStepDTO dto = new GrowthStepDTO();
        dto.setTitle(growthStep.getTitle());
        dto.setLevel(growthStep.getLevel());
        dto.setX(growthStep.getX());
        dto.setMinRatingOverLastXMatches(growthStep.getMinRatingOverLastXMatches() != null ? growthStep.getMinRatingOverLastXMatches().doubleValue() : null);
        dto.setMinRefereedMatches(growthStep.getMinRefereedMatches());
        dto.setMinRatingOverLastXTournaments(growthStep.getMinRatingOverLastXTournaments() != null ? growthStep.getMinRatingOverLastXTournaments().doubleValue() : null);
        dto.setMinCoordinatedTournaments(growthStep.getMinCoordinatedTournaments());
        dto.setCoordinatorPayPerMatch(growthStep.getCoordinatorPayPerMatch());
        return dto;
    }
}
