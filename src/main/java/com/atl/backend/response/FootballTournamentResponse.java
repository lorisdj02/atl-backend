package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.FinalStageBracketDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballStandingDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballTournamentResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.matches)
    private List<FootballMatchDTO> matches = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.standings)
    private List<FootballStandingDTO> standings = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.players)
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Long, TeamDTO> teamsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballPlayerStatsCache)
    private Map<Long, FootballTeamStatDTO> footballPlayerStatsCache = new HashMap<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.minutesPerRound)
    private Short minutesPerRound;

    @NotNull
    @JsonProperty(ApiJsonKeys.minutesPerPause)
    private Short minutesPerPause;

    @NotNull
    @JsonProperty(ApiJsonKeys.finalStageBrackets)
    private List<FinalStageBracketDTO> finalStageBrackets;
}
