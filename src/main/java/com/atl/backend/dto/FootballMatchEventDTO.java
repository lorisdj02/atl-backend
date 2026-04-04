package com.atl.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.Match;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballMatchEventDTO {
    @NotNull
    @JsonProperty("id")
    private Integer id;

    @NotNull
    @JsonProperty("matchId")
    private Integer matchId;

    @JsonProperty("teamId")
    private Integer teamId;

    @JsonProperty("playerId")
    private Integer playerId;

    @NotNull
    @JsonProperty("type")
    private String type;

    @NotNull
    @JsonProperty("minute")
    private Short minute;

    @NotNull
    @JsonProperty("additionalMinute")
    private Short additionalMinute;

    public static FootballMatchEventDTO fromFootballMatchEvent(FootballMatchEvent event) {
        if (event == null) {
            return null;
        }

        FootballMatchEventDTO dto = new FootballMatchEventDTO();
        dto.setId(event.getId());
        dto.setMatchId(event.getMatch().getId());
        dto.setTeamId(event.getTeam() != null ? event.getTeam().getId() : null);
        dto.setPlayerId(event.getPlayer() != null ? event.getPlayer().getPlayerId() : null);
        dto.setType(event.getType().toString());
        dto.setMinute(event.getMinute());
        dto.setAdditionalMinute(event.getAdditionalMinute());
        return dto;
    }
}
