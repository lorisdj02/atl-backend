package com.atl.backend.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentParametersResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.fee)
    private BigDecimal fee;

    @NotNull
    @JsonProperty(ApiJsonKeys.coordinatorPay)
    private BigDecimal coordinatorPay;

    @JsonProperty(ApiJsonKeys.prize1)
    private BigDecimal prize1;

    @JsonProperty(ApiJsonKeys.prize2)
    private BigDecimal prize2;

    @JsonProperty(ApiJsonKeys.numMatches)
    private Integer numMatches;

    /*
     * final double fee;
     * final double coordinatorPay;
     * final double? prize1;
     * final double? prize2;
     */
}
