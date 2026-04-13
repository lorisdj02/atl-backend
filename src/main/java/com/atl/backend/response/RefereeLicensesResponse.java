package com.atl.backend.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.RefereeLicenseDTO;
import com.atl.backend.dto.SportDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefereeLicensesResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.refereeLicenses)
    private List<RefereeLicenseDTO> refereeLicenses;

    @NotNull
    @JsonProperty(ApiJsonKeys.sports)
    private List<SportDTO> sports;
}
