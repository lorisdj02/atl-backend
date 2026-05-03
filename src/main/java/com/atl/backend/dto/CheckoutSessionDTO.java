package com.atl.backend.dto;

import java.math.BigDecimal;

import com.atl.backend.model.CheckoutSession;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutSessionDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotNull
    @JsonProperty("type")
    private String checkoutType;

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotNull
    @JsonProperty("payload")
    private CheckoutPayload payload;

    public static CheckoutSessionDTO fromCheckoutSession(CheckoutSession session) {
        if (session == null) {
            return null;
        }

        CheckoutSessionDTO dto = new CheckoutSessionDTO();
        dto.setId(session.getId());
        dto.setAccountId(session.getAccount().getId());
        dto.setCheckoutType(session.getType().toString());
        dto.setAmount(session.getAmount());
        dto.setPayload(session.getPayload());
        return dto;
    }
}
