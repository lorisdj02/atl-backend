package com.atl.backend.service.screen;

import com.atl.backend.service.TeamService;
import com.atl.backend.service.tournamentServices.TournamentService;
import com.atl.backend.service.tournamentServices.TournamentStandingService;
import com.atl.backend.service.tournamentServices.TournamentStatsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CityDTO;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FinalStageBracketDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballStandingDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.SportDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.City;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AllTournamentsResponse;
import com.atl.backend.response.CallupsResponse;
import com.atl.backend.response.FootballTournamentResponse;
import com.atl.backend.response.TournamentDetailsResponse;
import com.atl.backend.response.TournamentSummaryResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.MatchService;
import com.atl.backend.model.Field;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.FootballTournamentStats;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;

@Service
public class TournamentScreenService {
    @Autowired
    private TournamentStandingService tournamentStandingService;
    @Autowired
    private TournamentStatsService tournamentStatsService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private MatchService matchService;

    @Transactional(readOnly = true)
    public AllTournamentsResponse getAllTournamentsScreen(SportCategory sportCategory) {
        List<Tournament> tournaments = tournamentService.getActiveTournamentsBySportCategory(sportCategory);
        Map<Integer, Coordinator> coordinatorCache = new HashMap<>();
        Map<Integer, Field> fieldsCache = new HashMap<>();
        BigDecimal prizeMin = BigDecimal.ZERO;
        BigDecimal prizeMax = BigDecimal.ZERO;
        BigDecimal feeMin = BigDecimal.ZERO;
        BigDecimal feeMax = BigDecimal.ZERO;

        List<TournamentDTO> myTournaments = AccountContext.isPlayer() || AccountContext.isCoordinator()
                ? tournamentService.getMyTournaments(sportCategory)
                : new ArrayList<>();

        tournaments.removeIf(
                t -> myTournaments.stream().anyMatch(fav -> fav.getId() == t.getId()));

        List<TournamentDTO> tournamentDTOs = new ArrayList<>();
        for (Tournament tournament : tournaments) {
            tournamentDTOs.add(TournamentDTO.fromTournament(tournament));
        }
        tournamentDTOs.addAll(myTournaments);
        tournamentDTOs.sort((a, b) -> a.getName().compareTo(b.getName()));

        for (TournamentDTO tournament : tournamentDTOs) {
            if (!coordinatorCache.containsKey(tournament.getCoordinatorId())) {
                Optional<Coordinator> optionalCoordinator = accountService
                        .getCoordinatorByCoordinatorId(tournament.getCoordinatorId());
                if (optionalCoordinator.isEmpty()) {
                    throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND); // This should never happen, but
                                                                                     // just in case
                }
                Coordinator coordinator = optionalCoordinator.get();
                coordinatorCache.put(tournament.getCoordinatorId(), coordinator);
            }
            if (!fieldsCache.containsKey(tournament.getFieldId())) {
                Optional<Field> optionalField = fieldService.getFieldById(tournament.getFieldId());
                if (optionalField.isEmpty()) {
                    throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND); // This should never happen, but just in
                                                                               // case
                }
                Field field = optionalField.get();
                fieldsCache.put(tournament.getFieldId(), field);
                if (!fieldsCache.containsKey(field.getCity().getId())) {
                    fieldsCache.put(field.getCity().getId(), field);
                }
            }
        }

        if (!tournamentDTOs.isEmpty()) {
            prizeMin = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getPrize1)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            prizeMax = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getPrize1)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            feeMin = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getFee)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            feeMax = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getFee)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        } else {
            prizeMin = BigDecimal.ZERO;
            prizeMax = BigDecimal.ZERO;
            feeMin = BigDecimal.ZERO;
            feeMax = BigDecimal.ZERO;
        }

        List<City> cities = fieldService.getAllCities();

        return AllTournamentsResponse.builder()
                .tournaments(tournamentDTOs)
                .myTournaments(myTournaments)
                .coordinatorsCache(coordinatorCache.entrySet().stream().collect(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), CoordinatorDTO.fromCoordinator(e.getValue())),
                        HashMap::putAll))
                .fieldsCache(fieldsCache.entrySet().stream().collect(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), FieldDTO.fromField(e.getValue())),
                        HashMap::putAll))
                .cities(cities.stream().map(CityDTO::fromCity).toList())
                .cityNamePerField(fieldsCache.values().stream()
                        .collect(HashMap::new, (m, f) -> m.put(f.getId(), f.getCity().getName()), HashMap::putAll))
                .prizeMin(prizeMin)
                .prizeMax(prizeMax)
                .feeMin(feeMin)
                .feeMax(feeMax)
                .build();
    }

    @Transactional(readOnly = true)
    public TournamentDetailsResponse getTournamentDetailsScreen(Integer tournamentId) {
        // Semplicemente i dettagli del torneo, con eventuale team partecipante
        // dell'account e partecipazione del team al torneo
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        Tournament tournament = optionalTournament.get();

        Map<Integer, Team> teamsCache = new HashMap<>();

        if (!tournament.getAtlEvent()) {
            // Per gli eventi ATL, le squadre partecipanti rimangono nascoste fino
            // all'inizio del torneo.
            List<Integer> teams = tournamentService.getTeamIdsParticipatingInTournament(tournamentId);

            for (Integer teamId : teams) {
                Optional<Team> optionalTeam = teamService.getTeamById(teamId);
                if (optionalTeam.isEmpty()) {
                    throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
                }
                teamsCache.put(teamId, optionalTeam.get());
            }
        }

        Team accountTeam = null;
        TournamentParticipation teamParticipation = null;
        Player currentPlayer = null;

        if (AccountContext.isPlayer()) {
            currentPlayer = AccountContext.getActivePlayer();
            Optional<Team> accountTeamOptional = teamService.getTeamByPlayerIdAndSportCategory(
                    currentPlayer.getPlayerId(), tournament.getSport().getSportCategory());
            if (accountTeamOptional.isPresent()) {
                accountTeam = accountTeamOptional.get();
                Optional<TournamentParticipation> teamParticipationOptional = tournamentService
                        .getTournamentParticipationByTeamIdAndTournamentId(accountTeam.getId(), tournamentId);
                if (teamParticipationOptional.isPresent()) {
                    teamParticipation = teamParticipationOptional.get();
                }
            }
        }

        return TournamentDetailsResponse.builder()
                .field(FieldDTO.fromField(tournament.getField()))
                .cityName(tournament.getField().getCity().getName())
                .coordinator(CoordinatorDTO.fromCoordinator(tournament.getCoordinator()))
                .accountTeam(TeamDTO.fromTeam(accountTeam))
                .currentPlayer(PlayerDTO.fromPlayer(currentPlayer))
                .teamParticipation(TournamentParticipationDTO.fromTournamentParticipation(teamParticipation))
                .build();
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> getViewCallupsScreen(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        List<PlayerDTO> players = new ArrayList<>();
        Player currentPlayer = AccountContext.getActivePlayer();
        Optional<Team> accountTeam = teamService.getTeamByPlayerIdAndSportCategory(currentPlayer.getPlayerId(),
                SportCategory.FOOTBALL);
        if (accountTeam.isEmpty()) {
            throw new ConflictException(ErrorsString.NEED_TO_BE_IN_TEAM);
        }
        Team team = accountTeam.get();

        if (!teamService.isPlayerCaptainOrViceCaptain(team.getId(), currentPlayer.getPlayerId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN);
        }

        List<Integer> teamIds = tournamentService.getTeamIdsParticipatingInTournament(tournamentId);
        if (!teamIds.contains(team.getId())) {
            throw new ConflictException(ErrorsString.TEAM_MUST_PARTECIPATE_TO_TOURNAMENT);
        }

        List<Integer> calluppedPlayerIds = tournamentService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId,
                team.getId());
        for (Integer playerId : calluppedPlayerIds) {
            Optional<Player> optionalPlayer = accountService.getPlayerByPlayerId(playerId);
            if (optionalPlayer.isEmpty()) {
                throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
            }
            players.add(PlayerDTO.fromPlayer(optionalPlayer.get()));
        }
        return players;
    }

    @Transactional(readOnly = true)
    public CallupsResponse getCallupsScreen(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        Tournament tournament = optionalTournament.get();

        Optional<Team> accountTeam = teamService.getTeamByPlayerIdAndSportCategory(
                AccountContext.getActivePlayer().getPlayerId(), tournament.getSport().getSportCategory());

        if (accountTeam.isEmpty()) {
            throw new ConflictException(ErrorsString.NEED_TO_BE_IN_TEAM);
        }

        if (!teamService.isPlayerCaptainOrViceCaptain(accountTeam.get().getId(),
                AccountContext.getActivePlayer().getPlayerId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN);
        }

        List<Integer> calluppedPlayerIds = tournamentService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId,
                accountTeam.get().getId());

        List<Player> players = teamService.getPlayersByTeamId(accountTeam.get().getId());

        return CallupsResponse.builder()
                .tournament(TournamentDTO.fromTournament(tournament))
                .players(players.stream().map(PlayerDTO::fromPlayer).toList())
                .calledUpPlayerIds(calluppedPlayerIds)
                .sport(SportDTO.fromSport(tournament.getSport()))
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<?> getTournamentScreen(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        Tournament tournament = optionalTournament.get();

        return switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> Optional.of(getFootballTournamentScreen(tournamentId));
            default -> throw new ConflictException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        };
    }

    @Transactional(readOnly = true)
    public FootballTournamentResponse getFootballTournamentScreen(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        Tournament tournament = optionalTournament.get();

        List<FinalStageBracket> finalStageBrackets = tournamentService
                .getFinalStageBracketsByTournamentId(tournamentId);

        List<FootballMatch> matches = matchService.getFootballMatchesByTournamentId(tournamentId);

        List<FootballStanding> standings = tournamentStandingService.getFootballStandingsByTournamentId(tournamentId);

        List<Integer> teams = tournamentService.getTeamIdsParticipatingInTournament(tournamentId);

        Map<Integer, Team> teamsCache = new HashMap<>();
        for (Integer teamId : teams) {
            Optional<Team> optionalTeam = teamService.getTeamById(teamId);
            if (optionalTeam.isEmpty()) {
                throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
            }
            teamsCache.put(teamId, optionalTeam.get());
        }

        Map<Integer, FootballTeamStatDTO> footballPlayerStatsCache = new HashMap<>();
        List<FootballTournamentStats> footballTournamentStats = tournamentStatsService
                .getFootballTournamentStatsByTournamentId(tournamentId);

        List<Player> players = new ArrayList<>();
        for (FootballTournamentStats stat : footballTournamentStats) {
            if (stat.getGoals() == 0 && stat.getMotm() == 0) {
                continue; // Non ha senso inviare statistiche vuote, e così evitiamo di popolare la cache
                          // con dati inutili
            }
            FootballTeamStatDTO statDTO = new FootballTeamStatDTO();
            statDTO.setPlayerId(stat.getPlayer().getPlayerId());
            statDTO.setTeamId(stat.getTeam().getId());
            statDTO.setGoals(stat.getGoals());
            statDTO.setMotm(stat.getMotm());
            statDTO.setYellowCards(stat.getYellowCards());
            statDTO.setRedCards(stat.getRedCards());
            footballPlayerStatsCache.put(stat.getPlayer().getPlayerId(), statDTO);
            players.add(stat.getPlayer());
        }

        return FootballTournamentResponse.builder()
                .matches(matches.stream().map(FootballMatchDTO::fromFootballMatch).toList())
                .standings(standings.stream().map(FootballStandingDTO::fromFootballStanding).toList())
                .players(players.stream().map(PlayerDTO::fromPlayer).toList())
                .teamsCache(teamsCache.entrySet().stream().collect(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), TeamDTO.fromTeam(e.getValue())),
                        HashMap::putAll))
                .footballPlayerStatsCache(footballPlayerStatsCache)
                .minutesPerRound(tournament.getSport().getMinutesPerRound())
                .minutesPerPause(tournament.getSport().getMinutesPerPause())
                .finalStageBrackets(
                        finalStageBrackets.stream().map(FinalStageBracketDTO::fromFinalStageBracket).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public TournamentSummaryResponse getTournamentSummary(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        Tournament tournament = optionalTournament.get();

        return TournamentSummaryResponse.builder()
                .tournament(TournamentDTO.fromTournament(tournament))
                .field(FieldDTO.fromField(tournament.getField()))
                .sport(SportDTO.fromSport(tournament.getSport()))
                .cityName(tournament.getField().getCity().getName())
                .build();
    }
}
