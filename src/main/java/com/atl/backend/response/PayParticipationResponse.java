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
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.dto.TournamentParticipationDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayParticipationResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.tournamentsCache)
    private Map<Integer, TournamentDTO> tournamentsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.tournamentParticipations)
    private List<TournamentParticipationDTO> tournamentParticipations = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.isCalledUpPerTournament)
    private Map<Integer, Boolean> isCalledUpPerTournament = new HashMap<>();
}
