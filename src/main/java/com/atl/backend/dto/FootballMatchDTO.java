package com.atl.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.atl.backend.model.FootballMatch;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballMatchDTO extends MatchDTO {

    @NotNull
    @JsonProperty("homeTeamId")
    private Integer homeTeamId;

    @NotNull
    @JsonProperty("awayTeamId")
    private Integer awayTeamId;

    @NotNull
    @JsonProperty("scoreHome")
    private Short scoreHome;

    @NotNull
    @JsonProperty("scoreAway")
    private Short penaltyScoreHome;

    @NotNull
    @JsonProperty("missedPenaltyHome")
    private Short missedPenaltyHome;

    @NotNull
    @JsonProperty("scoreAway")
    private Short scoreAway;

    @NotNull
    @JsonProperty("penaltyScoreAway")
    private Short penaltyScoreAway;

    @NotNull
    @JsonProperty("missedPenaltyAway")
    private Short missedPenaltyAway;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("halftime")
    private LocalDateTime halftime;

    @NotNull
    @JsonProperty("startHalf")
    private LocalDateTime startHalf;

    @NotNull
    @JsonProperty("legOrder")
    private String legOrder;

    public static FootballMatchDTO fromFootballMatch(FootballMatch footballMatch) {
        if (footballMatch == null) {
            return null;
        }

        FootballMatchDTO dto = new FootballMatchDTO();
        dto.setId(footballMatch.getId());
        dto.setHomeTeamId(footballMatch.getHomeTeam().getId());
        dto.setAwayTeamId(footballMatch.getAwayTeam().getId());
        dto.setScoreHome(footballMatch.getScoreHome());
        dto.setPenaltyScoreHome(footballMatch.getPenaltyScoreHome());
        dto.setMissedPenaltyHome(footballMatch.getMissedPenaltyHome());
        dto.setScoreAway(footballMatch.getScoreAway());
        dto.setPenaltyScoreAway(footballMatch.getPenaltyScoreAway());
        dto.setMissedPenaltyAway(footballMatch.getMissedPenaltyAway());
        dto.setStatus(footballMatch.getStatus().toString());
        dto.setHalftime(footballMatch.getHalftime());
        dto.setStartHalf(footballMatch.getStartHalf());
        dto.setLegOrder(footballMatch.getMatch().getLegOrder().toString());
        return dto;
    }
}
