package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.SportDTO;
import com.atl.backend.dto.TournamentDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallupsResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.players)
    private List<PlayerDTO> players = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.sport)
    private SportDTO sport;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.calledUpPlayerIds)
    private List<Integer> calledUpPlayerIds = new ArrayList<>();

    @NotNull
    @JsonProperty(ApiJsonKeys.tournament)
    private TournamentDTO tournament;


    /*
     * final List<Player> players;
     * final Sport sport;
     * final List<int> calledUpPlayerIds;
     * final Tournament tournament;
     */
}
