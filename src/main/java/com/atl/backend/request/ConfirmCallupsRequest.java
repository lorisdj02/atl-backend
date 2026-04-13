package com.atl.backend.request;

import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmCallupsRequest {
    @NotNull
    @JsonProperty(ApiJsonKeys.selectedPlayerIds)
    private List<Integer> selectedPlayerIds;

    @NotNull
    @JsonProperty(ApiJsonKeys.tournamentId)
    private Integer tournamentId;

    @NotNull
    @JsonProperty(ApiJsonKeys.teamId)
    private Integer teamId;
}
