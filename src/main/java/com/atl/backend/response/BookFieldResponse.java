package com.atl.backend.response;
import java.util.HashMap;
import java.util.List;
import java.time.LocalTime;
import java.util.ArrayList;

import java.util.Map;

import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.CityDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookFieldResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cities)
    private List<CityDTO> cities = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minOpeningTimePerCity)
    private Map<Integer, LocalTime> minOpeningTimePerCity = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.maxClosingTimePerCity)
    private Map<Integer, LocalTime> maxClosingTimePerCity = new HashMap<>();


}
