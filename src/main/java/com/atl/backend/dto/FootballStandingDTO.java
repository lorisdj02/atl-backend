package com.atl.backend.dto;

import lombok.Data;

import com.atl.backend.model.FootballStanding;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballStandingDTO {

    @NotNull
    @JsonProperty("tournamentId")
    private Integer tournamentId;

    @NotNull
    @JsonProperty("teamId")
    private Integer teamId;

    @NotNull
    @JsonProperty("groupName")
    private String groupName;

    @NotNull
    @JsonProperty("won")
    private Short won = 0;

    @NotNull
    @JsonProperty("drawn")
    private Short drawn = 0;

    @NotNull
    @JsonProperty("lost")
    private Short lost = 0;
    @NotNull
    @JsonProperty("goalsScored")
    private Short goalsScored = 0;
    @NotNull
    @JsonProperty("goalsConceded")
    private Short goalsConceded = 0;

    @NotNull
    @JsonProperty("rankPos")
    private Short rankPos;

    @NotNull
    @JsonProperty("qualified")
    private Boolean qualified;

    public static FootballStandingDTO fromFootballStanding(FootballStanding footballStanding) {
        if (footballStanding == null) {
            return null;
        }

        FootballStandingDTO dto = new FootballStandingDTO();
        dto.setTournamentId(footballStanding.getTournament().getId());
        dto.setTeamId(footballStanding.getTeam().getId());
        dto.setGroupName(footballStanding.getGroupName());
        dto.setWon(footballStanding.getWon());
        dto.setDrawn(footballStanding.getDrawn());
        dto.setLost(footballStanding.getLost());
        dto.setGoalsScored(footballStanding.getGoalsScored());
        dto.setGoalsConceded(footballStanding.getGoalsConceded());
        dto.setRankPos(footballStanding.getRankPos());
        dto.setQualified(footballStanding.getQualified());
        return dto;
    }
}
