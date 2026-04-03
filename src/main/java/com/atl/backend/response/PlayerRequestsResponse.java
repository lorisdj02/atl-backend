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
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamRequestDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerRequestsResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.teamRequests)
    private List<TeamRequestDTO> teamRequests;

    @NotNull
    @JsonProperty(ApiJsonKeys.playersCache)
    private Map<Integer, PlayerDTO> playersCache;
}
