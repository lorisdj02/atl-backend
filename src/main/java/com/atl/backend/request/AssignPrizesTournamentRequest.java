package com.atl.backend.request;

import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignPrizesTournamentRequest {
    @NotNull
    @JsonProperty(ApiJsonKeys.tournamentId)
    private Long tournamentId;

    @NotNull
    @JsonProperty(ApiJsonKeys.teamId)
    private Long teamId;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.prizePerPlayer)
    private Map<Long, BigDecimal> prizePerPlayer = new HashMap<>(); // playerId -> prizeAmount
}
