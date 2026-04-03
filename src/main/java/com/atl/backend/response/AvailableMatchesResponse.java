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
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.TeamDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableMatchesResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.footballMatches)
    private List<FootballMatchDTO> footballMatches;

    @NotNull
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Integer, TeamDTO> teamsCache;

    @NotNull
    @JsonProperty(ApiJsonKeys.minutesRoundPerTournament)
    private Map<Integer, Short> minutesRoundPerTournament;

    @NotNull
    @JsonProperty(ApiJsonKeys.minutesPausePerTournament)
    private Map<Integer, Short> minutesPausePerTournament;

    @NotNull
    @JsonProperty(ApiJsonKeys.footballMatchesPerSport)
    private Map<Integer, List<FootballMatchDTO>> footballMatchesPerSport;

    @NotNull
    @JsonProperty(ApiJsonKeys.sportNames)
    private Map<Integer, String> sportNames;
}
