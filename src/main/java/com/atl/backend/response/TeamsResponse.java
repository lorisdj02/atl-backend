package com.atl.backend.response;

import java.util.HashMap;
import java.util.Map;

import com.atl.backend.dto.TeamDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamsResponse {
    @NotNull
    @Builder.Default
    @JsonProperty("teamsCache")
    private Map<Integer, TeamDTO> teamsCache = new HashMap<>();


    @NotNull
    @Builder.Default
    @JsonProperty("captainNamePerTeam")
    private Map<Integer, String> captainNamePerTeam = new HashMap<>();
}
