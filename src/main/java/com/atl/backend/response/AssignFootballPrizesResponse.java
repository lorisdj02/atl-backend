package com.atl.backend.response;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.FootballStandingDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignFootballPrizesResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.tournament)
    private TournamentDTO tournament;

    @NotNull
    @JsonProperty(ApiJsonKeys.team)
    private TeamDTO teamName;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.playersCache)
    private Map<Long, PlayerDTO> playersCache = new HashMap<>(); // playerId -> PlayerDTO

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballPlayerStatsCache)
    private Map<Long, FootballTeamStatDTO> footballPlayerStatsCache = new HashMap<>(); // playerId -> Football

    @NotNull
    @JsonProperty(ApiJsonKeys.isWinner)
    private Boolean isWinner;

}
