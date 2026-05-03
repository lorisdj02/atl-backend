package com.atl.backend.request;

import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmCallupsRequest {
    @NotNull
    @JsonProperty(ApiJsonKeys.selectedPlayerIds)
    private List<Long> selectedPlayerIds;

    @NotNull
    @JsonProperty(ApiJsonKeys.tournamentId)
    private Long tournamentId;

    @NotNull
    @JsonProperty(ApiJsonKeys.teamId)
    private Long teamId;
}
