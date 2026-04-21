package com.atl.backend.response;




import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballStandingDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;

import com.atl.backend.config.ApiJsonKeys;

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
    private Map<Long, Short> minutesRoundPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minutesPausePerTournament)
    private Map<Long, Short> minutesPausePerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballMatchesPerTournament)
    private Map<Long, List<FootballMatchDTO>> footballMatchesPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.footballStandingsPerTournament)
    private Map<Long, List<FootballStandingDTO>> footballStandingsPerTournament = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.teamsCache)
    private Map<Long, TeamDTO> teamsCache = new HashMap<>();

    @JsonProperty(ApiJsonKeys.accountTeam)
    private TeamDTO accountTeam;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.usernamePerBooking)
    private Map<Long, String> usernamePerBooking = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fieldsCache)
    private Map<Long, FieldDTO> fieldsCache = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.maxPlayersPerLobby)
    private Map<Long, Short> maxPlayersPerLobby = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.currentPlayersPerLobby)
    private Map<Long, Short> currentPlayersPerLobby = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.completeAddressPerField)
    private Map<Long, String> completeAddressPerField = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.playersPaidPerBooking)
    private Map<Long, Short> playersPaidPerBooking = new HashMap<>();

}
