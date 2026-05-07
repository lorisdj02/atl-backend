package com.atl.backend.dto;

import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.FootballTournamentStat;

import lombok.Data;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FootballTeamStatDTO {
    @NotNull
    @JsonProperty("teamId")
    private Long teamId;

    @NotNull
    @JsonProperty("playerId")
    private Long playerId;

    @NotNull
    @JsonProperty("goals")
    private Short goals;

    @NotNull
    @JsonProperty("motm")
    private Short motm;

    @NotNull
    @JsonProperty("yellowCards")
    private Short yellowCards;

    @NotNull
    @JsonProperty("redCards")
    private Short redCards;

    public static FootballTeamStatDTO fromFootballTournamentStat(FootballTournamentStat footballTournamentStat) {
        if (footballTournamentStat == null) {
            return null;
        }

        FootballTeamStatDTO dto = new FootballTeamStatDTO();
        dto.setTeamId(footballTournamentStat.getTeam().getId());
        dto.setPlayerId(footballTournamentStat.getPlayer().getPlayerId());
        dto.setGoals(footballTournamentStat.getGoals());
        dto.setMotm(footballTournamentStat.getMotm());
        dto.setYellowCards(footballTournamentStat.getYellowCards());
        dto.setRedCards(footballTournamentStat.getRedCards());
        return dto;
    }

    public static FootballTeamStatDTO fromFootballTeamStat(FootballTeamStat footballTeamStat) {
        if (footballTeamStat == null) {
            return null;
        }

        FootballTeamStatDTO dto = new FootballTeamStatDTO();
        dto.setTeamId(footballTeamStat.getTeam().getId());
        dto.setPlayerId(footballTeamStat.getPlayer().getPlayerId());
        dto.setGoals(footballTeamStat.getGoals());
        dto.setMotm(footballTeamStat.getMotm());
        dto.setYellowCards(footballTeamStat.getYellowCards());
        dto.setRedCards(footballTeamStat.getRedCards());
        return dto;
    }
}
