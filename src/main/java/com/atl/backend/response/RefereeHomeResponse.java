package com.atl.backend.response;

import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.config.ApiJsonKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefereeHomeResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.assignedFootballMatches)
    private List<FootballMatchDTO> assignedFootballMatches = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Long, TeamDTO> teamsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minutesRoundPerTournament)
    private Map<Long, Short> minutesRoundPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minutesPausePerTournament)
    private Map<Long, Short> minutesPausePerTournament = new HashMap<>();

}
