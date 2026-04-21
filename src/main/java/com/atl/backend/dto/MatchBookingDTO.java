package com.atl.backend.dto;

import lombok.Data;

import com.atl.backend.model.MatchBooking;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchBookingDTO extends BookingDTO {

    @NotNull
    @JsonProperty("coordinatorId")
    private Long coordinatorId;

    @NotNull
    @JsonProperty("matchId")
    private Long matchId;

    public static MatchBookingDTO fromMatchBooking(MatchBooking matchBooking) {
        if (matchBooking == null) {
            return null;
        }

        MatchBookingDTO dto = new MatchBookingDTO();
        dto.setId(matchBooking.getId());
        dto.setFieldId(matchBooking.getBooking().getField().getId());
        dto.setStartTime(matchBooking.getBooking().getStartTime().toString());
        dto.setFinishTime(matchBooking.getBooking().getFinishTime().toString());
        dto.setStatus(matchBooking.getBooking().getStatus().toString());
        dto.setSportCategory(matchBooking.getBooking().getSportCategory().toString());
        dto.setBookingType(matchBooking.getBooking().getBookingType().toString());
        dto.setCoordinatorId(matchBooking.getCoordinator().getCoordinatorId());
        dto.setMatchId(matchBooking.getMatch().getId());
        return dto;
    }
}
