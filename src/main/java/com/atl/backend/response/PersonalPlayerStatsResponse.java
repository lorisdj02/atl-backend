package com.atl.backend.response;

import java.util.Map;

import lombok.Builder;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.FootballTeamStatDTO;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalPlayerStatsResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.footballStats)
    private FootballTeamStatDTO footballStats;

    @NotNull
    @JsonProperty(ApiJsonKeys.numTournamentPerCategory)
    private Map<String, Integer> numTournamentPerCategory;

    @NotNull
    @JsonProperty(ApiJsonKeys.numPrivateBookingPerCategory)
    private Map<String, Integer> numPrivateBookingPerCategory;

    @NotNull
    @JsonProperty(ApiJsonKeys.numLobbyJoinedPerCategory)
    private Map<String, Integer> numLobbyJoinedPerCategory;

    /*
     * final FootballTeamStats footballStats;
     * final Map<SportCategory, int> numTournamentPerCategory;
     * final Map<SportCategory, int> numPrivateBookingPerCategory;
     * final Map<SportCategory, int> numLobbyJoinedPerCategory;
     */
}
