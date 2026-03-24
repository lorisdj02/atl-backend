package com.atl.backend.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PalmaresDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractTeamResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.team)
    private TeamDTO team;

    @NotNull
    @JsonProperty(ApiJsonKeys.players)
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.palmares)
    private List<PalmaresDTO> palmares = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.tournaments)
    private List<TournamentDTO> tournaments = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.coordinatorsCache)
    private List<CoordinatorDTO> coordinatorsCache = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private List<FieldDTO> fieldsCache = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.footballPlayerStatsCache)
    private List<FootballTeamStatDTO> footballPlayerStatsCache = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.cityNamePerField)
    private Map<Integer, String> cityNamePerField = new HashMap<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.historicalPlayers)
    private List<PlayerDTO> historicalPlayers = new ArrayList<>();
}
