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
import com.atl.backend.dto.FootballMatchEventDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballMatchEventsResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.events)
    private List<FootballMatchEventDTO> events = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.canMatchFinish)
    private Boolean canMatchFinish = false;
}
