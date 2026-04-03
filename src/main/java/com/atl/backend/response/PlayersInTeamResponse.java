package com.atl.backend.response;
import java.util.List;
import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;

import com.atl.backend.dto.PlayerDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayersInTeamResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.players)
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.historicalPlayers)
    @Builder.Default
    private List<PlayerDTO> historicalPlayers = new ArrayList<>();
}
