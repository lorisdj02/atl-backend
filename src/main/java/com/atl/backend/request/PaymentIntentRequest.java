package com.atl.backend.request;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentIntentRequest {
    @NotNull
    @JsonProperty(ApiJsonKeys.paymentIntentId)
    private String paymentIntentId;
}
