package com.atl.backend.service.screen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.*;
import com.atl.backend.exception.*;
import com.atl.backend.model.*;

import com.atl.backend.model.enums.*;

import com.atl.backend.response.*;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.MatchService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.TournamentService;

@Service
public class HomeScreenService {
        @Autowired
        private FieldService fieldService;
        @Autowired
        private MatchService matchService;
        @Autowired
        private TeamService teamService;
        @Autowired
        private TournamentService tournamentService;
        @Autowired
        private BookingService bookingService;

        @Transactional(readOnly = true)
        public PlayerHomeResponse getPlayerHomeScreen(SportCategory sportCategory) {
                Integer playerId = AccountContext.getPlayerId();

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
                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                        entry -> TeamDTO.fromTeam(entry.getValue())));

                        fieldsCacheDTO = fieldsCache.entrySet().stream()
                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                        entry -> FieldDTO.fromField(entry.getValue())));

                        footballMatchesPerTournamentDTO = footballMatchesPerTournament
                                        .entrySet()
                                        .stream()
                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                        entry -> entry.getValue().stream()
                                                                        .map(FootballMatchDTO::fromFootballMatch)
                                                                        .toList()));

                        footballStandingsPerTournamentDTO = footballStandingsPerTournament
                                        .entrySet()
                                        .stream()
                                        .collect(Collectors.toMap(Map.Entry::getKey,
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

        @Transactional(readOnly = true)
        public CoordinatorHomeResponse getCoordinatorHomeScreen(SportCategory sportCategory) {
                Integer coordinatorId = AccountContext.getCoordinatorId();
                List<Tournament> activeTournaments = new ArrayList<>();
                Map<Integer, List<FootballMatch>> footballMatchesPerTournament = new HashMap<>();
                Map<Integer, Team> teamsCache = new HashMap<>();
                Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Integer, Short> minutesPausePerTournament = new HashMap<>();

                activeTournaments = tournamentService.getActiveTournamentsByCoordinatorIdAndSportCategory(coordinatorId,
                                sportCategory);

                for (Tournament tournament : activeTournaments) {
                        switch (sportCategory) {
                                case FOOTBALL:
                                        List<FootballMatch> footballMatches = matchService
                                                        .getFootballMatchesByTournamentId(tournament.getId());
                                        footballMatchesPerTournament.put(tournament.getId(), footballMatches);

                                        break;
                                default:
                                        break;
                        }
                        List<Integer> teamIds = tournamentService
                                        .getTeamIdsParticipatingInTournament(tournament.getId());
                        for (Integer teamId : teamIds) {
                                teamsCache.put(teamId, teamService.getTeamById(teamId).get());
                        }

                        minutesRoundPerTournament.put(tournament.getId(),
                                        tournament.getSport().getMinutesPerRound());

                        minutesPausePerTournament.put(tournament.getId(),
                                        tournament.getSport().getMinutesPerPause());
                }

                Map<Integer, TeamDTO> taemsCacheDTO = teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> TeamDTO.fromTeam(entry.getValue())));
                Map<Integer, List<FootballMatchDTO>> footballMatchesPerTournamentDTO = footballMatchesPerTournament
                                .entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> entry.getValue().stream()
                                                                .map(FootballMatchDTO::fromFootballMatch)
                                                                .toList()));

                return CoordinatorHomeResponse.builder()
                                .activeTournaments(
                                                activeTournaments.stream().map(TournamentDTO::fromTournament)
                                                                .toList())
                                .footballMatchesPerTournament(footballMatchesPerTournamentDTO)
                                .teamsCache(taemsCacheDTO)
                                .minutesRoundPerTournament(minutesRoundPerTournament)
                                .minutesPausePerTournament(minutesPausePerTournament)
                                .build();
        }

        @Transactional(readOnly = true)
        public RefereeHomeResponse getRefereeHomeScreen(SportCategory sportCategory) {
                Integer refereeId = AccountContext.getRefereeId();
                List<FootballMatch> assignedFootballMatches = new ArrayList<>();
                Map<Integer, Team> teamsCache = new HashMap<>();
                Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Integer, Short> minutesPausePerTournament = new HashMap<>();

                switch (sportCategory) {
                        case FOOTBALL:
                                assignedFootballMatches = matchService.getAssignedFootballMatchesByRefereeId(refereeId);
                                for (FootballMatch footballMatch : assignedFootballMatches) {
                                        List<Team> teams = matchService
                                                        .getTeamsByMatchId(footballMatch.getMatch().getId());
                                        teamsCache.putAll(teams.stream()
                                                        .collect(Collectors.toMap(Team::getId, team -> team)));
                                        minutesRoundPerTournament.put(footballMatch.getMatch().getTournament().getId(),
                                                        footballMatch.getMatch().getTournament().getSport()
                                                                        .getMinutesPerRound());
                                        minutesPausePerTournament.put(footballMatch.getMatch().getTournament().getId(),
                                                        footballMatch.getMatch().getTournament().getSport()
                                                                        .getMinutesPerPause());
                                }
                                break;
                        default:
                                break;
                }

                Map<Integer, TeamDTO> teamsCacheDTO = teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> TeamDTO.fromTeam(entry.getValue())));
                List<FootballMatchDTO> assignedFootballMatchesDTO = assignedFootballMatches.stream()
                                .map(FootballMatchDTO::fromFootballMatch).toList();

                return RefereeHomeResponse.builder()
                                .assignedFootballMatches(assignedFootballMatchesDTO)
                                .teamsCache(teamsCacheDTO)
                                .minutesRoundPerTournament(minutesRoundPerTournament)
                                .minutesPausePerTournament(minutesPausePerTournament)
                                .build();

        }

        @Transactional(readOnly = true)
        public OrganizationHomeResponse getOrganizationHomeScreen(SportCategory sportCategory) {
                Integer organizationId = AccountContext.getOrganizationId();

                List<Field> fields = new ArrayList<>();
                List<Booking> generalBookings = new ArrayList<>();
                Map<Integer, String> cityNamePerField = new HashMap<>();
                Map<Integer, LobbyBooking> lobbyBookingsCache = new HashMap<>();
                Map<Integer, PrivateBooking> privateBookingsCache = new HashMap<>();
                Map<Integer, MatchBooking> matchBookingsCache = new HashMap<>();
                Map<Integer, FootballMatch> footballMatchPerBooking = new HashMap<>();
                Map<Integer, Team> teamsCache = new HashMap<>();
                Map<Integer, String> usernamePerBooking = new HashMap<>();

                fields = fieldService.getFieldsByOrganizationId(organizationId);

                for (Field field : fields) {
                        if (cityNamePerField.get(field.getId()) == null) {
                                cityNamePerField.put(field.getId(), field.getCity().getName());
                        }
                        List<Booking> fieldBookings = bookingService.getBookingsByFieldId(field.getId());
                        generalBookings.addAll(fieldBookings);
                        String username;
                        Account usernameAccount;
                        for (Booking booking : fieldBookings) {
                                switch (booking.getBookingType()) {
                                        case LOBBY:
                                                Optional<LobbyBooking> optionalLobbyBooking = bookingService
                                                                .getLobbyBookingByBookingId(booking.getId());
                                                if (optionalLobbyBooking.isEmpty()) {
                                                        throw new NotFoundException(ErrorsString.BOOKING_NOT_FOUND);
                                                }

                                                LobbyBooking lobbyBooking = optionalLobbyBooking.get();
                                                usernameAccount = lobbyBooking.getOwnerPlayer().getAccount();

                                                username = usernameAccount.getUsername()
                                                                + "("
                                                                + usernameAccount.getName()
                                                                + " " + usernameAccount
                                                                                .getSurname()
                                                                + ")";
                                                lobbyBookingsCache.put(booking.getId(), lobbyBooking);
                                                usernamePerBooking.put(booking.getId(), username);
                                                break;
                                        case PRIVATE:
                                                Optional<PrivateBooking> optionalPrivateBooking = bookingService
                                                                .getPrivateBookingByBookingId(booking.getId());
                                                if (optionalPrivateBooking.isEmpty()) {
                                                        throw new NotFoundException(ErrorsString.BOOKING_NOT_FOUND);
                                                }

                                                PrivateBooking privateBooking = optionalPrivateBooking.get();
                                                usernameAccount = privateBooking.getPlayer().getAccount();
                                                username = usernameAccount.getUsername() + "("
                                                                + usernameAccount.getName() + " "
                                                                + usernameAccount.getSurname() + ")";
                                                privateBookingsCache.put(booking.getId(), privateBooking);
                                                usernamePerBooking.put(booking.getId(), username);
                                                break;
                                        case MATCH:
                                                Optional<MatchBooking> optionalMatchBooking = bookingService
                                                                .getMatchBookingByBookingId(booking.getId());
                                                if (optionalMatchBooking.isEmpty()) {
                                                        throw new NotFoundException(ErrorsString.BOOKING_NOT_FOUND);
                                                }

                                                MatchBooking matchBooking = optionalMatchBooking.get();
                                                matchBookingsCache.put(booking.getId(), matchBooking);
                                                Match match = matchBooking.getMatch();
                                                switch (sportCategory) {
                                                        case FOOTBALL:
                                                                Optional<FootballMatch> optionalFootballMatch = matchService
                                                                                .getFootballMatchById(match.getId());
                                                                if (optionalFootballMatch.isEmpty()) {
                                                                        throw new NotFoundException(
                                                                                        ErrorsString.MATCH_NOT_FOUND);
                                                                }

                                                                FootballMatch footballMatch = optionalFootballMatch
                                                                                .get();
                                                                usernameAccount = footballMatch.getMatch()
                                                                                .getTournament().getCoordinator()
                                                                                .getAccount();
                                                                username = usernameAccount.getUsername() + "("
                                                                                + usernameAccount.getName() + " "
                                                                                + usernameAccount.getSurname() + ")";
                                                                footballMatchPerBooking.put(booking.getId(),
                                                                                footballMatch);
                                                                break;
                                                        default:
                                                                throw new BadRequestException(
                                                                                ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                                                }
                                        default:
                                                throw new BadRequestException(
                                                                ErrorsString.UNSUPPORTED_BOOKING_TYPE);
                                }
                        }
                }
                List<FieldDTO> fieldsDTO = fields.stream().map(FieldDTO::fromField).toList();
                List<BookingDTO> generalBookingsDTO = generalBookings.stream().map(BookingDTO::fromBooking)
                                .toList();
                Map<Integer, LobbyBookingDTO> lobbyBookingsCacheDTO = lobbyBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> LobbyBookingDTO.fromLobbyBooking(entry.getValue())));
                Map<Integer, PrivateBookingDTO> privateBookingsCacheDTO = privateBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> PrivateBookingDTO.fromPrivateBooking(entry.getValue())));
                Map<Integer, MatchBookingDTO> matchBookingsCacheDTO = matchBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> MatchBookingDTO.fromMatchBooking(entry.getValue())));
                Map<Integer, FootballMatchDTO> footballMatchPerBookingDTO = footballMatchPerBooking.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> FootballMatchDTO.fromFootballMatch(entry.getValue())));
                Map<Integer, TeamDTO> teamsCacheDTO = teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> TeamDTO.fromTeam(entry.getValue())));

                return OrganizationHomeResponse.builder().fields(fieldsDTO).generalBookings(generalBookingsDTO)
                                .cityNamePerField(cityNamePerField)
                                .lobbyBookingsCache(lobbyBookingsCacheDTO)
                                .privateBookingsCache(privateBookingsCacheDTO)
                                .matchBookingsCache(matchBookingsCacheDTO)
                                .footballMatchPerBooking(footballMatchPerBookingDTO)
                                .teamsCache(teamsCacheDTO)
                                .usernamePerBooking(usernamePerBooking).build();
        }
}
