package com.atl.backend.response;

import java.math.BigDecimal;

import com.atl.backend.dto.CityDTO;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;


import com.atl.backend.config.ApiJsonKeys;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllTournamentsResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.tournaments)
    private List<TournamentDTO> tournaments = new ArrayList<>();


    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.myTournaments)
    private List<TournamentDTO> myTournaments = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.coordinatorsCache)
    private Map<Long, CoordinatorDTO> coordinatorsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private Map<Long, FieldDTO> fieldsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cities)
    private List<CityDTO> cities = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cityNamePerField)
    private Map<Long, String> cityNamePerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.prizeMin)
    private BigDecimal prizeMin = BigDecimal.ZERO;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.prizeMax)
    private BigDecimal prizeMax = BigDecimal.ZERO;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.feeMin)
    private BigDecimal feeMin = BigDecimal.ZERO;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.feeMax)
    private BigDecimal feeMax = BigDecimal.ZERO;
}
