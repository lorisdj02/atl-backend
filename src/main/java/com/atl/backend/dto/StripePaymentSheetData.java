package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripePaymentSheetData {

    @NotNull
    @JsonProperty("paymentIntentId")
    private String paymentIntentId;

    @NotNull
    @JsonProperty("clientSecret")
    private String clientSecret;
    
    @NotNull
    @JsonProperty("customerId")
    private String customerId;
    
    @NotNull
    @JsonProperty("ephemeralKey")
    private String ephemeralKey;
    
    @NotNull
    @JsonProperty("publishableKey")
    private String publishableKey;
}
