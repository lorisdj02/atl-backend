package com.atl.backend.dto;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutPayload {

    @JsonProperty("tournamentId")
    private Long tournamentId;

    @JsonProperty("lobbyId")
    private Long lobbyId;

    @JsonProperty("playersPaid")
    private Short playersPaid;

    @JsonProperty("ownerPlayerId")
    private Long ownerPlayerId;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("finishTime")
    private String finishTime;

    @JsonProperty("totalPlayers")
    private Short totalPlayers;

    @JsonProperty("playersToFind")
    private Short playersToFind;

    @JsonProperty("fieldId")
    private Long fieldId;

    @JsonProperty("sportCategory")
    private String sportCategory;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("checkoutType")
    private String checkoutType;



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
