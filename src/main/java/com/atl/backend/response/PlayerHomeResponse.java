package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;

import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballStandingDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.config.ApiJsonKeys;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerHomeResponse {


    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.lobbies)
    private List<LobbyBookingDTO> lobbies = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.privateBookings)
    private List<PrivateBookingDTO> privateBookings = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.activeTournaments)
    private List<TournamentDTO> activeTournaments = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minutesRoundPerTournament)
    private Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minutesPausePerTournament)
    private Map<Integer, Short> minutesPausePerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballMatchesPerTournament)
    private Map<Integer, List<FootballMatchDTO>> footballMatchesPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballStandingsPerTournament)
    private Map<Integer, List<FootballStandingDTO>> footballStandingsPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Integer, TeamDTO> teamsCache = new HashMap<>();

    @JsonProperty(ApiJsonKeys.accountTeam)
    private TeamDTO accountTeam;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.usernamePerBooking)
    private Map<Integer, String> usernamePerBooking = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private Map<Integer, FieldDTO> fieldsCache = new HashMap<>();

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
    @JsonProperty(ApiJsonKeys.completeAddressPerField)
    private Map<Integer, String> completeAddressPerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.playersPaidPerBooking)
    private Map<Integer, Short> playersPaidPerBooking = new HashMap<>();

}
