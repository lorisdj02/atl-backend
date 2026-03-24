package com.atl.backend.dto;

import lombok.Data;

import com.atl.backend.model.Palmares;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PalmaresDTO {
    @NotNull
    @JsonProperty("tournamentId")
    private Integer tournamentId;

    @NotNull
    @JsonProperty("position")
    private String position;

    public static PalmaresDTO fromPalmares(Palmares palmares) {
        if (palmares == null) {
            return null;
        }
        PalmaresDTO dto = new PalmaresDTO();
        dto.setTournamentId(palmares.getTournament().getId());
        dto.setPosition(palmares.getPosition().toString());
        return dto;
    }

}
