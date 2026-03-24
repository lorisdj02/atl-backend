package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;

import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.CityDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentDTO;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JoinLobbyResponse {
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.usernamePerBooking)
    private Map<Integer, String> usernamePerBooking = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.lobbies)
    private List<LobbyBookingDTO> lobbies = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.maxPlayersPerLobby)
    private Map<Integer, Short> maxPlayersPerLobby = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.currentPlayersPerLobby)
    private Map<Integer, Short> currentPlayersPerLobby = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.playersPaidPerBooking)
    private Map<Integer, Short> playersPaidPerBooking = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private Map<Integer, FieldDTO> fieldsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.completeAddressPerField)
    private Map<Integer, String> completeAddressPerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cities)
    private List<CityDTO> cities = new ArrayList<>();
}
