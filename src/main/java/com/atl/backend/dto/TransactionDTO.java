package com.atl.backend.dto;

import java.math.BigDecimal;

import com.atl.backend.model.Transaction;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDTO {
    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("type")
    private String type;

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotNull
    @JsonProperty("currency")
    private String currency;

    @NotNull
    @JsonProperty("createdAt")
    private String createdAt;

    @NotNull
    @JsonProperty("source")
    private String source;

    public static TransactionDTO fromTransaction(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAccountId(transaction.getAccount().getId());
        dto.setStatus(transaction.getStatus().name());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setCreatedAt(transaction.getCreatedAt().toString());
        dto.setSource(transaction.getSource().name());
        return dto;
    }
}
