package com.atl.backend.dto;

import com.atl.backend.model.TeamRequest;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamRequestDTO {
    @NotNull
    @JsonProperty("teamId")
    private Long teamId;

    @NotNull
    @JsonProperty("playerId")
    private Long playerId;

    static public TeamRequestDTO fromTeamRequest(TeamRequest teamRequest) {
        TeamRequestDTO dto = new TeamRequestDTO();
        dto.setTeamId(teamRequest.getTeam().getId());
        dto.setPlayerId(teamRequest.getPlayer().getPlayerId());
        return dto;
    }
}
