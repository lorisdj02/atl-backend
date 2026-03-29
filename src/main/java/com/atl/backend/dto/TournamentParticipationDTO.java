package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

import com.atl.backend.model.TournamentParticipation;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentParticipationDTO {
    @NotNull
    @JsonProperty("tournamentId")
    private Integer tournamentId;

    @NotNull
    @JsonProperty("teamId")
    private Integer teamId;
    
    @NotNull
    @JsonProperty("toPay")
    private String toPay;


    public static TournamentParticipationDTO fromTournamentParticipation(TournamentParticipation tournamentParticipation) {
        TournamentParticipationDTO dto = new TournamentParticipationDTO();
        dto.setTournamentId(tournamentParticipation.getTournament().getId());
        dto.setTeamId(tournamentParticipation.getTeam().getId());
        dto.setToPay(tournamentParticipation.getToPay().toPlainString());
        return dto;
    }
}
