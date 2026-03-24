package com.atl.backend.response;

import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LobbyInfoResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.lobbyBooking)
    private LobbyBookingDTO lobbyBooking;

    @NotNull
    @JsonProperty(ApiJsonKeys.usernamePerBooking)
    private String usernamePerBooking;

    @NotNull
    @JsonProperty(ApiJsonKeys.maxPlayersPerLobby)
    private Short maxPlayersPerLobby;

    @NotNull
    @JsonProperty(ApiJsonKeys.currentPlayersPerLobby)
    private Short currentPlayersPerLobby;

    @NotNull
    @JsonProperty(ApiJsonKeys.completeAddressPerField)
    private String completeAddressPerField;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.playersPaidPerBooking)
    private Short playersPaidPerBooking = 0;

    @NotNull
    @JsonProperty(ApiJsonKeys.field)
    private FieldDTO field;
}
