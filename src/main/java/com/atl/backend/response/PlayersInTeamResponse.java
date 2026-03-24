package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.atl.backend.dto.PlayerDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayersInTeamResponse {
    @NotNull
    @JsonProperty("players")
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @JsonProperty("historicalPlayers")
    private List<PlayerDTO> historicalPlayers = new ArrayList<>();
}
