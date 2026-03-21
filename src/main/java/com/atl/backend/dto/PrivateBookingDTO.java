package com.atl.backend.dto;

import com.atl.backend.model.PrivateBooking;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivateBookingDTO extends BookingDTO {

    @NotNull
    @JsonProperty("playerId")
    private Integer playerId;

    @NotNull
    @JsonProperty("cost")
    private BigDecimal cost;

    public static PrivateBookingDTO fromPrivateBooking(PrivateBooking privateBooking) {
        if (privateBooking == null) {
            return null;
        }

        PrivateBookingDTO dto = new PrivateBookingDTO();
        dto.setId(privateBooking.getId());
        dto.setFieldId(privateBooking.getBooking().getField().getId());
        dto.setStartTime(privateBooking.getBooking().getStartTime());
        dto.setFinishTime(privateBooking.getBooking().getFinishTime());
        dto.setStatus(privateBooking.getBooking().getStatus().toString());
        dto.setSportCategory(privateBooking.getBooking().getSportCategory().toString());
        dto.setBookingType(privateBooking.getBooking().getBookingType().toString());
        dto.setPlayerId(privateBooking.getPlayer().getPlayerId());
        dto.setCost(privateBooking.getCost());
        return dto;
    }
}
