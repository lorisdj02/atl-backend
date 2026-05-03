package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import com.atl.backend.model.TournamentParticipation;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentParticipationDTO {
    @NotNull
    @JsonProperty("tournamentId")
    private Long tournamentId;

    @NotNull
    @JsonProperty("teamId")
    private Long teamId;
    
    @NotNull
    @JsonProperty("toPay")
    private BigDecimal toPay;


    public static TournamentParticipationDTO fromTournamentParticipation(TournamentParticipation tournamentParticipation) {
        TournamentParticipationDTO dto = new TournamentParticipationDTO();
        if (tournamentParticipation == null) {
            return null;
        }
        dto.setTournamentId(tournamentParticipation.getTournament().getId());
        dto.setTeamId(tournamentParticipation.getTeam().getId());
        dto.setToPay(tournamentParticipation.getToPay());
        return dto;
    }
}
