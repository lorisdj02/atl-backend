package com.atl.backend.dto;

import com.atl.backend.model.Purchase;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotNull
    @JsonProperty("objectId")
    private Long objectId;

    @NotNull
    @JsonProperty("type")
    private String type;

    @JsonProperty("detail")
    private String detail;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotNull
    @JsonProperty("createdAt")
    private String createdAt;

    public static PurchaseDTO fromPurchase(Purchase purchase) {
        if (purchase == null) {
            return null;
        }

        PurchaseDTO dto = new PurchaseDTO();
        dto.setId(purchase.getId());
        dto.setAccountId(purchase.getAccount().getId());
        dto.setObjectId(purchase.getObjectId());
        dto.setType(purchase.getType().toString());
        dto.setDetail(purchase.getDetail().toString());
        dto.setStatus(purchase.getStatus().toString());
        dto.setAmount(purchase.getAmount());
        dto.setCreatedAt(purchase.getCreatedAt().toString());
        return dto;
    }
}
