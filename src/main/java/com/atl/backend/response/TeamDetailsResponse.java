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
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PalmaresDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TeamRequestDTO;
import com.atl.backend.dto.TournamentDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamDetailsResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.team)
    private TeamDTO team;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.players)
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.palmares)
    private List<PalmaresDTO> palmares = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.tournaments)
    private List<TournamentDTO> tournaments = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.coordinatorsCache)
    private List<CoordinatorDTO> coordinatorsCache = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private List<FieldDTO> fieldsCache = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballPlayerStatsCache)
    private List<FootballTeamStatDTO> footballPlayerStatsCache = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cityNamePerField)
    private Map<Long, String> cityNamePerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.historicalPlayers)
    private List<PlayerDTO> historicalPlayers = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.isPlayerInTeam)
    private Boolean isPlayerInTeam;

    @JsonProperty(ApiJsonKeys.existingRequest)
    private TeamRequestDTO existingRequest;

    /*
     * final bool isPlayerInTeam;
     * final TeamRequest? existingRequest;
     * 
     */
}
