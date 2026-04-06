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
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.SportDTO;
import com.atl.backend.dto.TournamentDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentSummaryResponse {


    @NotNull
    @JsonProperty(ApiJsonKeys.tournament)
    private TournamentDTO tournament;

    @NotNull
    @JsonProperty(ApiJsonKeys.sport)
    private SportDTO sport;

    @NotNull
    @JsonProperty(ApiJsonKeys.field)
    private FieldDTO field;

    @NotNull
    @JsonProperty(ApiJsonKeys.cityName)
    private String cityName;

}
