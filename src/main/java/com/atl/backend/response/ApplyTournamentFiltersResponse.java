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
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplyTournamentFiltersResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.tournaments)
    private List<TournamentDTO> tournaments = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cityNamePerField)
    private Map<Integer, String> cityNamePerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private Map<Integer, FieldDTO> fieldsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.coordinatorsCache)
    private Map<Integer, CoordinatorDTO> coordinatorsCache = new HashMap<>();


    /*
     * List<Tournament> tournaments;
     * Map<int, String> cityNamePerField;
     * Map<int, Field> fieldsCache;
     * Map<int, Coordinator> coordinatorsCache;
     */
}
