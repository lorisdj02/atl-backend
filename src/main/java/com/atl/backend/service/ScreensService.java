package com.atl.backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atl.backend.dto.*;

import com.atl.backend.model.*;

import com.atl.backend.model.enums.*;

import com.atl.backend.response.*;

@Service
public class ScreensService {

        private final MatchService matchService;
        private final TeamService teamService;
        private final TournamentService tournamentService;
        private final BookingService bookingService;

        public ScreensService(TeamService teamService, TournamentService tournamentService,
                        BookingService bookingService,
                        MatchService matchService) {
                this.teamService = teamService;
                this.tournamentService = tournamentService;
                this.bookingService = bookingService;
                this.matchService = matchService;

        }

        public PlayerHomeResponse getPlayerHomeResponse(Integer playerId, SportCategory sportCategory) {

                List<Tournament> activeTournaments = new ArrayList<>();
                Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Integer, Short> minutesPausePerTournament = new HashMap<>();
                Map<Integer, List<FootballMatch>> footballMatchesPerTournament = new HashMap<>();
                Map<Integer, List<FootballStanding>> footballStandingsPerTournament = new HashMap<>();
                Map<Integer, Team> teamsCache = new HashMap<>();
                Optional<Team> accountTeam = Optional.empty();
                Map<Integer, String> usernamePerBooking = new HashMap<>();
                Map<Integer, Field> fieldsCache = new HashMap<>();
                Map<Integer, Short> maxPlayersPerLobby = new HashMap<>();
                Map<Integer, Short> currentPlayersPerLobby = new HashMap<>();
                Map<Integer, String> completeAddressPerField = new HashMap<>();
                Map<Integer, Short> playersPaidPerBooking = new HashMap<>();

                accountTeam = teamService.getTeamByPlayerIdAndSportCategory(playerId, sportCategory);

                final List<PrivateBooking> finalBookings = bookingService.getActivePrivateBookingsByPlayerId(playerId);
                final List<LobbyBooking> finalLobbyBookings = bookingService.getActiveLobbyBookingsByPlayerId(playerId);

                for (PrivateBooking booking : finalBookings) {
                        fieldsCache.put(booking.getBooking().getField().getId(), booking.getBooking().getField());
                        completeAddressPerField.put(booking.getBooking().getField().getId(),
                                        (booking.getBooking().getField().getAddress() + " - "
                                                        + (booking.getBooking().getField().getCity().getName())));
                }

                for (LobbyBooking lobbyBooking : finalLobbyBookings) {
                        usernamePerBooking.put(lobbyBooking.getId(),
                                        lobbyBooking.getOwnerPlayer().getAccount().getUsername());
                        fieldsCache.put(lobbyBooking.getBooking().getField().getId(),
                                        lobbyBooking.getBooking().getField());
                        maxPlayersPerLobby.put(lobbyBooking.getId(), lobbyBooking.getTotalPlayers());
                        completeAddressPerField.put(lobbyBooking.getBooking().getField().getId(),
                                        (lobbyBooking.getBooking().getField().getAddress() + " - "
                                                        + (lobbyBooking.getBooking().getField().getCity().getName())));
                        playersPaidPerBooking.put(lobbyBooking.getId(),
                                        bookingService.getPlayersPaidInLobbyBookingPerPlayerId(lobbyBooking.getId(),
                                                        playerId));
                        currentPlayersPerLobby.put(lobbyBooking.getId(),
                                        Short.valueOf((short) lobbyBooking.getCurrentPlayers()));
                }

                Map<Integer, TeamDTO> teamsCacheDTO = new HashMap<>();
                Map<Integer, FieldDTO> fieldsCacheDTO = new HashMap<>();
                Map<Integer, List<FootballMatchDTO>> footballMatchesPerTournamentDTO = new HashMap<>();
                Map<Integer, List<FootballStandingDTO>> footballStandingsPerTournamentDTO = new HashMap<>();

                if (accountTeam.isPresent()) {

                        activeTournaments = tournamentService.getActiveTournamentsByTeamIdAndSportCategory(
                                        accountTeam.get().getId(),
                                        sportCategory);

                        for (Tournament tournament : activeTournaments) {
                                switch (sportCategory) {
                                        case FOOTBALL:
                                                footballStandingsPerTournament.put(tournament.getId(),
                                                                tournamentService.getFootballStandingsByTournamentId(
                                                                                tournament.getId()));
                                                footballMatchesPerTournament.put(tournament.getId(),
                                                                matchService.getFootballMatchesByTournamentId(
                                                                                tournament.getId()));
                                                break;
                                        default:
                                                break;
                                }
                                minutesRoundPerTournament.put(tournament.getId(),
                                                tournament.getSport().getMinutesPerRound());
                                minutesPausePerTournament.put(tournament.getId(),
                                                tournament.getSport().getMinutesPerPause());
                                fieldsCache.put(tournament.getField().getId(), tournament.getField());
                                final List<Integer> teamIds = tournamentService
                                                .getTeamIdsParticipatingInTournament(tournament.getId());
                                for (Integer teamId : teamIds) {
                                        teamsCache.put(teamId, teamService.getTeamById(teamId).get());
                                }
                        }

                        teamsCacheDTO = teamsCache.entrySet().stream()
                                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                                                        entry -> TeamDTO.fromTeam(entry.getValue())));

                        fieldsCacheDTO = fieldsCache.entrySet().stream()
                                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                                                        entry -> FieldDTO.fromField(entry.getValue())));

                        footballMatchesPerTournamentDTO = footballMatchesPerTournament
                                        .entrySet()
                                        .stream()
                                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                                                        entry -> entry.getValue().stream()
                                                                        .map(FootballMatchDTO::fromFootballMatch)
                                                                        .toList()));

                        footballStandingsPerTournamentDTO = footballStandingsPerTournament
                                        .entrySet()
                                        .stream()
                                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                                                        entry -> entry.getValue().stream()
                                                                        .map(FootballStandingDTO::fromFootballStanding)
                                                                        .toList()));
                }
                // Implementation for fetching player home response
                return PlayerHomeResponse.builder()
                                .lobbies(finalLobbyBookings.stream().map(LobbyBookingDTO::fromLobbyBooking).toList())
                                .privateBookings(finalBookings.stream().map(PrivateBookingDTO::fromPrivateBooking)
                                                .toList())
                                .activeTournaments(
                                                activeTournaments.stream().map(TournamentDTO::fromTournament).toList())
                                .minutesRoundPerTournament(minutesRoundPerTournament)
                                .minutesPausePerTournament(minutesPausePerTournament)
                                .footballMatchesPerTournament(footballMatchesPerTournamentDTO)
                                .footballStandingsPerTournament(footballStandingsPerTournamentDTO)
                                .teamsCache(teamsCacheDTO).accountTeam(accountTeam.isPresent()
                                                ? TeamDTO.fromTeam(accountTeam.get())
                                                : null)
                                .usernamePerBooking(usernamePerBooking).fieldsCache(fieldsCacheDTO)
                                .maxPlayersPerLobby(maxPlayersPerLobby).currentPlayersPerLobby(currentPlayersPerLobby)
                                .completeAddressPerField(completeAddressPerField)
                                .playersPaidPerBooking(playersPaidPerBooking).build();
        }

}
