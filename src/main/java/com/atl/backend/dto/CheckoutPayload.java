package com.atl.backend.dto;


import java.time.LocalDateTime;

import com.atl.backend.model.Booking;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutPayload {

    @JsonProperty("tournamentId")
    private Integer tournamentId;

    @JsonProperty("lobbyId")
    private Integer lobbyId;

    @JsonProperty("playersPaid")
    private Short playersPaid;

    @JsonProperty("ownerPlayerId")
    private Integer ownerPlayerId;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("finishTime")
    private String finishTime;

    @JsonProperty("totalPlayers")
    private Short totalPlayers;

    @JsonProperty("playersToFind")
    private Short playersToFind;

    @JsonProperty("fieldId")
    private Integer fieldId;

    @JsonProperty("sportCategory")
    private String sportCategory;


    /*
     * final int? tournamentId;
     * final int? lobbyId;
     * final int? playersPaid;
     * 
     * final int? ownerPlayerId;
     * final DateTime? startDateTime;
     * final DateTime? endDateTime;
     * final int? maxPlayers;
     * final int? playersToFind;
     * 
     * final int? fieldId;
     */
}
