package com.atl.backend.dto;


import com.atl.backend.model.Booking;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("startTime")
    private String startTime;

    @NotNull
    @JsonProperty("finishTime")
    private String finishTime;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @NotNull
    @JsonProperty("fieldId")
    private Long fieldId;

    @NotNull
    @JsonProperty("bookingType")
    private String bookingType;

    public static BookingDTO fromBooking(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setStartTime(booking.getStartTime().toString());
        bookingDTO.setFinishTime(booking.getFinishTime().toString());
        bookingDTO.setStatus(booking.getStatus().toString());
        bookingDTO.setSportCategory(booking.getSportCategory().toString());
        bookingDTO.setFieldId(booking.getField().getId());
        bookingDTO.setBookingType(booking.getBookingType().toString());
        return bookingDTO;
    }
}
