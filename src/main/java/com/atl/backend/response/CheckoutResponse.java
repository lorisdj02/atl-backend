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
import com.atl.backend.dto.CheckoutSessionDTO;
import com.atl.backend.dto.WalletDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.checkoutSession)
    private CheckoutSessionDTO checkoutSession;

    @NotNull
    @JsonProperty(ApiJsonKeys.wallet)
    private WalletDTO wallet;

    @JsonProperty(ApiJsonKeys.tournamentName)
    private String tournamentName;

    @JsonProperty(ApiJsonKeys.bookingUsername)
    private String bookingUsername;

    @JsonProperty(ApiJsonKeys.cityName)
    private String cityName;

    @JsonProperty(ApiJsonKeys.fieldName)
    private String fieldName;

    @JsonProperty(ApiJsonKeys.fieldAddress)
    private String fieldAddress;

    @JsonProperty(ApiJsonKeys.startTime)
    private String startTime;

    @JsonProperty(ApiJsonKeys.finishTime)
    private String finishTime;

    @JsonProperty(ApiJsonKeys.maxPlayers)
    private Integer maxPlayers;

    @JsonProperty(ApiJsonKeys.playersToPay)
    private Integer playersToPay;

    /*
     * final CheckoutSession checkoutSession;
     * final Wallet wallet;
     * final String? tournamentName;
     * final String? bookingUsername;
     * final String? cityName;
     * final String? fieldName;
     * final String? fieldAddress;
     * final DateTime? startTime;
     * final DateTime? endTime;
     * final int? maxPlayers;
     * final int? playersToPay;
     */
}
