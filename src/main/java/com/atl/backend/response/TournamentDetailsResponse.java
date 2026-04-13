package com.atl.backend.response;

import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.config.ApiJsonKeys;

import java.util.Map;
import java.util.HashMap;

import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.PlayerDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentDetailsResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.field)
    private FieldDTO field;

    @NotNull
    @JsonProperty(ApiJsonKeys.cityName)
    private String cityName;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Integer, TeamDTO> teamsCache = new HashMap<>();

    @JsonProperty(ApiJsonKeys.currentPlayer)
    private PlayerDTO currentPlayer;

    @JsonProperty(ApiJsonKeys.accountTeam)
    private TeamDTO accountTeam;

    @JsonProperty(ApiJsonKeys.teamParticipation)
    private TournamentParticipationDTO teamParticipation;

    @NotNull
    @JsonProperty(ApiJsonKeys.coordinator)
    private CoordinatorDTO coordinator;

}
