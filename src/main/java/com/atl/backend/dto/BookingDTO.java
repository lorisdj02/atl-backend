package com.atl.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    @NotNull
    @JsonProperty("id")
    private Integer id;

        @NotNull
    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @NotNull
    @JsonProperty("finishTime")
    private LocalDateTime finishTime;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @NotNull
    @JsonProperty("fieldId")
    private Integer fieldId;

    @NotNull
    @JsonProperty("bookingType")
    private String bookingType;
}
