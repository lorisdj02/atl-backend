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
import com.atl.backend.dto.TransactionDTO;
import com.atl.backend.dto.WalletDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.wallet)
    private WalletDTO wallet;

    @NotNull
    @JsonProperty(ApiJsonKeys.transactionsHistory)
    private List<TransactionDTO> transactionsHistory;
}
