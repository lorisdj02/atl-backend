package com.atl.backend.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.PurchaseDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchasesResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.purchases)
    private List<PurchaseDTO> purchases = new ArrayList<>();
}
