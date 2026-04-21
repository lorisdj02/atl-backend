package com.atl.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

import com.atl.backend.model.LobbyBooking;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LobbyBookingDTO extends BookingDTO {
    @NotNull
    @JsonProperty("ownerPlayerId")
    private Long ownerPlayerId;

    @NotNull
    @JsonProperty("playersToFind")
    private Short playersToFind;

    @NotNull
    @JsonProperty("totalPlayers")
    private Short totalPlayers;

    @NotNull
    @JsonProperty("feePerPlayer")
    private BigDecimal feePerPlayer;

    public static LobbyBookingDTO fromLobbyBooking(LobbyBooking lobbyBooking) {
        if (lobbyBooking == null) {
            return null;
        }

        LobbyBookingDTO dto = new LobbyBookingDTO();
        dto.setId(lobbyBooking.getId());
        dto.setFieldId(lobbyBooking.getBooking().getField().getId());
        dto.setStartTime(lobbyBooking.getBooking().getStartTime().toString());
        dto.setFinishTime(lobbyBooking.getBooking().getFinishTime().toString());
        dto.setStatus(lobbyBooking.getBooking().getStatus().toString());
        dto.setSportCategory(lobbyBooking.getBooking().getSportCategory().toString());
        dto.setBookingType(lobbyBooking.getBooking().getBookingType().toString());
        dto.setOwnerPlayerId(lobbyBooking.getOwnerPlayer().getPlayerId());
        dto.setPlayersToFind(lobbyBooking.getPlayersToFind());
        dto.setTotalPlayers(lobbyBooking.getTotalPlayers());
        dto.setFeePerPlayer(lobbyBooking.getFeePerPlayer());
        return dto;
    }
}
