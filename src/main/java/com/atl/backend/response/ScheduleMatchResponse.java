package com.atl.backend.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.BookingDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleMatchResponse {
    
    @NotNull
    @JsonProperty(ApiJsonKeys.minutesPerRound)
    private Short minutesPerRound;

    @NotNull
    @JsonProperty(ApiJsonKeys.minutesPerPause)
    private Short minutesPerPause;

    @NotNull
    @JsonProperty(ApiJsonKeys.bookings)
    private List<BookingDTO> bookings;
}
