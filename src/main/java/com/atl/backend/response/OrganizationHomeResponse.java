package com.atl.backend.response;

import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.BookingDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.MatchBookingDTO;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.dto.TeamDTO;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationHomeResponse {

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fields)
    private List<FieldDTO> fields = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.generalBookings)
    private List<BookingDTO> generalBookings = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.cityNamePerField)
    private Map<Integer, String> cityNamePerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.lobbyBookingsCache)
    private Map<Integer, LobbyBookingDTO> lobbyBookingsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.privateBookingsCache)
    private Map<Integer, PrivateBookingDTO> privateBookingsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.matchBookingsCache)
    private Map<Integer, MatchBookingDTO> matchBookingsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballMatchPerBooking)
    private Map<Integer, FootballMatchDTO> footballMatchPerBooking = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Integer, TeamDTO> teamsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.usernamePerBooking)
    private Map<Integer, String> usernamePerBooking = new HashMap<>();

}
