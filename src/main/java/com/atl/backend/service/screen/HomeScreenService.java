package com.atl.backend.service.screen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
import com.atl.backend.service.FieldService;
import com.atl.backend.service.bookingServices.BookingService;
import com.atl.backend.service.bookingServices.LobbyBookingService;
import com.atl.backend.service.bookingServices.MatchBookingService;
import com.atl.backend.service.bookingServices.PrivateBookingService;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;
import com.atl.backend.service.tournamentServices.TournamentStandingService;

@Service
public class HomeScreenService {
        @Autowired
        private TournamentService tournamentService;
        @Autowired
        private TournamentStandingService tournamentStandingService;
        @Autowired
        private TournamentParticipantService tournamentParticipantService;

        @Autowired
        private FieldService fieldService;
        @Autowired
        private MatchService matchService;
        @Autowired
        private MatchRefereeService matchRefereeService;
        @Autowired
        private TeamService teamService;

        @Autowired
        private BookingService bookingService;
        @Autowired
        private LobbyBookingService lobbyBookingService;
        @Autowired
        private PrivateBookingService privateBookingService;
        @Autowired
        private MatchBookingService matchBookingService;

        @Transactional(readOnly = true)
        public PlayerHomeResponse getPlayerHomeScreen(SportCategory sportCategory) {
                Long playerId = AccountContext.getPlayerId();

                List<Tournament> activeTournaments = new ArrayList<>();
                Map<Long, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Long, Short> minutesPausePerTournament = new HashMap<>();
                Map<Long, List<FootballMatch>> footballMatchesPerTournament = new HashMap<>();
                Map<Long, List<FootballStanding>> footballStandingsPerTournament = new HashMap<>();
                Map<Long, Team> teamsCache = new HashMap<>();
                Optional<Team> accountTeam = Optional.empty();
                Map<Long, String> usernamePerBooking = new HashMap<>();
                Map<Long, Field> fieldsCache = new HashMap<>();
                Map<Long, Short> maxPlayersPerLobby = new HashMap<>();
                Map<Long, Short> currentPlayersPerLobby = new HashMap<>();
                Map<Long, String> completeAddressPerField = new HashMap<>();
                Map<Long, Short> playersPaidPerBooking = new HashMap<>();

                accountTeam = teamService.getTeamByPlayerIdAndSportCategory(playerId, sportCategory);

                final List<PrivateBooking> finalBookings = privateBookingService
                                .getActivePrivateBookingsByPlayerId(playerId);
                final List<LobbyBooking> finalLobbyBookings = lobbyBookingService
                                .getActiveLobbyBookingsByPlayerId(playerId);

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
                                        lobbyBookingService.getPlayersPaidInLobbyBookingPerPlayerId(
                                                        lobbyBooking.getId(),
                                                        playerId));
                        currentPlayersPerLobby.put(lobbyBooking.getId(),
                                        Short.valueOf((short) lobbyBooking.getCurrentPlayers()));
                }

                Map<Long, TeamDTO> teamsCacheDTO = new HashMap<>();
                Map<Long, FieldDTO> fieldsCacheDTO = new HashMap<>();
                Map<Long, List<FootballMatchDTO>> footballMatchesPerTournamentDTO = new HashMap<>();
                Map<Long, List<FootballStandingDTO>> footballStandingsPerTournamentDTO = new HashMap<>();

                if (accountTeam.isPresent()) {

                        activeTournaments = tournamentService.getActiveTournamentsByTeamIdAndSportCategory(
                                        accountTeam.get().getId(),
                                        sportCategory);

                        for (Tournament tournament : activeTournaments) {
                                switch (sportCategory) {
                                        case FOOTBALL:
                                                footballStandingsPerTournament.put(tournament.getId(),
                                                                tournamentStandingService
                                                                                .getFootballStandingsByTournamentId(
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
                                final List<Long> teamIds = tournamentParticipantService
                                                .getTeamIdsParticipatingInTournament(tournament.getId());
                                for (Long teamId : teamIds) {
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
                Long coordinatorId = AccountContext.getCoordinatorId();
                List<Tournament> activeTournaments = new ArrayList<>();
                Map<Long, List<FootballMatch>> footballMatchesPerTournament = new HashMap<>();
                Map<Long, Team> teamsCache = new HashMap<>();
                Map<Long, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Long, Short> minutesPausePerTournament = new HashMap<>();

                //Long sixMonthsAgoEpoch = Instant.now().minusSeconds(6 * 30 * 24 * 3600).getEpochSecond();

                //activeTournaments = tournamentService.getRecentTournamentsByCoordinatorIdAndSportCategory(coordinatorId,
                //                sportCategory, Instant.now().minusSeconds(sixMonthsAgoEpoch)); // Last 6 months tournaments
                activeTournaments = tournamentService.getActiveTournamentsByCoordinatorIdAndSportCategory(coordinatorId,
                                sportCategory); // Active tournaments
                System.out.println("Active Tournaments: " + activeTournaments.size());

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
                        List<Long> teamIds = tournamentParticipantService
                                        .getTeamIdsParticipatingInTournament(tournament.getId());
                        for (Long teamId : teamIds) {
                                teamsCache.put(teamId, teamService.getTeamById(teamId).get());
                        }

                        minutesRoundPerTournament.put(tournament.getId(),
                                        tournament.getSport().getMinutesPerRound());

                        minutesPausePerTournament.put(tournament.getId(),
                                        tournament.getSport().getMinutesPerPause());
                }

                Map<Long, TeamDTO> taemsCacheDTO = teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> TeamDTO.fromTeam(entry.getValue())));
                Map<Long, List<FootballMatchDTO>> footballMatchesPerTournamentDTO = footballMatchesPerTournament
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
                Long refereeId = AccountContext.getRefereeId();
                List<FootballMatch> assignedFootballMatches = new ArrayList<>();
                Map<Long, Team> teamsCache = new HashMap<>();
                Map<Long, Short> minutesRoundPerTournament = new HashMap<>();
                Map<Long, Short> minutesPausePerTournament = new HashMap<>();

                switch (sportCategory) {
                        case FOOTBALL:
                                assignedFootballMatches = matchRefereeService
                                                .getAssignedFootballMatchesByRefereeId(refereeId);
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

                Map<Long, TeamDTO> teamsCacheDTO = teamsCache.entrySet().stream()
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
                Long organizationId = AccountContext.getOrganizationId();
                List<Field> fields = new ArrayList<>();
                List<Booking> generalBookings = new ArrayList<>();
                Map<Long, String> cityNamePerField = new HashMap<>();
                Map<Long, LobbyBooking> lobbyBookingsCache = new HashMap<>();
                Map<Long, PrivateBooking> privateBookingsCache = new HashMap<>();
                Map<Long, MatchBooking> matchBookingsCache = new HashMap<>();
                Map<Long, FootballMatch> footballMatchPerBooking = new HashMap<>();
                Map<Long, Team> teamsCache = new HashMap<>();
                Map<Long, String> usernamePerBooking = new HashMap<>();

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
                                                Optional<LobbyBooking> optionalLobbyBooking = lobbyBookingService
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
                                                Optional<PrivateBooking> optionalPrivateBooking = privateBookingService
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
                                                Optional<MatchBooking> optionalMatchBooking = matchBookingService
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
                                                                if (footballMatch.getHomeTeam() != null && teamsCache
                                                                                .get(footballMatch.getHomeTeam()
                                                                                                .getId()) == null) {
                                                                        teamsCache.put(footballMatch.getHomeTeam()
                                                                                        .getId(),
                                                                                        footballMatch.getHomeTeam());
                                                                }

                                                                if (footballMatch.getAwayTeam() != null && teamsCache
                                                                                .get(footballMatch.getAwayTeam()
                                                                                                .getId()) == null) {
                                                                        teamsCache.put(footballMatch.getAwayTeam()
                                                                                        .getId(),
                                                                                        footballMatch.getAwayTeam());
                                                                }

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
                                                break;
                                        default:
                                                throw new BadRequestException(
                                                                ErrorsString.UNSUPPORTED_BOOKING_TYPE);
                                }
                                usernamePerBooking.put(booking.getId(), username);
                        }
                }
                List<FieldDTO> fieldsDTO = fields.stream().map(FieldDTO::fromField).toList();

                List<BookingDTO> generalBookingsDTO = generalBookings.stream().map(BookingDTO::fromBooking)
                                .toList();

                Map<Long, LobbyBookingDTO> lobbyBookingsCacheDTO = lobbyBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> LobbyBookingDTO.fromLobbyBooking(entry.getValue())));

                Map<Long, PrivateBookingDTO> privateBookingsCacheDTO = privateBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> PrivateBookingDTO.fromPrivateBooking(entry.getValue())));

                Map<Long, MatchBookingDTO> matchBookingsCacheDTO = matchBookingsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> MatchBookingDTO.fromMatchBooking(entry.getValue())));

                Map<Long, FootballMatchDTO> footballMatchPerBookingDTO = footballMatchPerBooking.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                entry -> FootballMatchDTO.fromFootballMatch(entry.getValue())));

                Map<Long, TeamDTO> teamsCacheDTO = teamsCache.entrySet().stream()
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
