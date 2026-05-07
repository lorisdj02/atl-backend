package com.atl.backend.request;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignTournamentPrizesRequest {
    @NotNull
    @JsonProperty(ApiJsonKeys.tournamentId)
    private Long tournamentId;

    @NotNull
    @JsonProperty(ApiJsonKeys.teamId)
    private Long teamId;

    @NotNull
    @JsonProperty(ApiJsonKeys.prizePerPlayer)
    private Map<String, BigDecimal> prizePerPlayer; // playerId -> prizeAmount
}
