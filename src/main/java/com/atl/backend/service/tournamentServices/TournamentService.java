package com.atl.backend.service.tournamentServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.BookingDTO;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Booking;
import com.atl.backend.model.Callup;
import com.atl.backend.model.City;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.FavoriteTournament;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.Match;
import com.atl.backend.model.Player;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.enums.FootballMatchStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.model.Field;
import com.atl.backend.repository.CoordinatorRepository;
import com.atl.backend.repository.FavoriteTournamentRepository;
import com.atl.backend.repository.FinalStageBracketRepository;
import com.atl.backend.repository.GrowthStepRepository;
import com.atl.backend.repository.RefereeRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.response.ApplyTournamentFiltersResponse;
import com.atl.backend.response.ScheduleMatchResponse;
import com.atl.backend.response.TournamentParametersResponse;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.matchServices.MatchCombinationService;
import com.atl.backend.service.matchServices.MatchService;

@Service
public class TournamentService {

    @Autowired
    private TournamentStandingService tournamentStandingService;
    @Autowired
    private TournamentStatService tournamentStatService;
    @Autowired
    private TournamentParticipantService tournamentParticipantService;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private FavoriteTournamentRepository favoriteTournamentRepository;
    @Autowired
    private FinalStageBracketRepository finalStageBracketRepository;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private SportService sportService;

    @Autowired
    private MatchService matchService;
    @Autowired
    private MatchCombinationService matchGenerationService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private CoordinatorRepository coordinatorRepository;
    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private GrowthStepRepository growthStepRepository;


    public void deleteAllTournaments() {
        tournamentRepository.deleteAll();
        tournamentStandingService.deleteAllFootballStandings();
        tournamentParticipantService.deleteAllTournamentParticipations();
        tournamentParticipantService.deleteAllCallups();
        tournamentStatService.deleteAllFootballTournamentStats();
        matchService.deleteAllMatches();
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Optional<Tournament> getTournamentById(Integer id) {
        return tournamentRepository.findById(id);
    }

    public Tournament saveTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public void deleteTournament(Integer id) {
        tournamentRepository.deleteById(id);
    }

    private Coordinator getExistingCoordinatorByCoordinatorId(Integer coordinatorId) {
        Optional<Coordinator> coordinatorOpt = coordinatorRepository.findById(coordinatorId);
        if (coordinatorOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
        }
        return coordinatorOpt.get();
    }

    public List<Tournament> getActiveTournaments() {
        return tournamentRepository.findActiveTournaments();
    }

    public List<Tournament> getActiveTournamentsBySportCategory(SportCategory sportCategory) {
        return tournamentRepository.findActiveTournamentsBySportCategory(sportCategory);
    }

    public List<Tournament> getActiveTournamentsByCoordinatorIdAndSportCategory(Integer coordinatorId,
            SportCategory sportCategory) {
        return tournamentRepository.findActiveTournamentsByCoordinator_CoordinatorIdAndSportCategory(coordinatorId,
                sportCategory);
    }

    public String getTournamentName(Integer tournamentId) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return tournamentOpt.get().getName();
    }

    public List<FinalStageBracket> getFinalStageBracketsByTournamentId(Integer tournamentId) {
        return finalStageBracketRepository.findByTournament_Id(tournamentId);
    }

    public List<Tournament> getActiveTournamentsByTeamId(Integer teamId) {
        teamService.checkTeamExists(teamId);
        List<TournamentParticipation> participations = tournamentParticipantService
                .getTournamentParticipationsByTeamId(teamId);
        List<Tournament> result = new ArrayList<>();

        for (TournamentParticipation participation : participations) {
            if (participation.getTournament().isTournamentActive() &&
                    matchService.isTeamStillPlayingInTournamentByTeamIdAndTournamentIdAndPhase(
                            participation.getTeam().getId(),
                            participation.getTournament().getId(), participation.getTournament().getPhase())) {
                result.add(participation.getTournament());
            }
            if (participation.getTournament().getStatus() == TournamentStatus.PENDING_PAYMENTS
                    && matchService.hasTeamPrizesByTeamIdAndTournamentId(participation.getTeam().getId(),
                            participation.getTournament().getId())) {
                result.add(participation.getTournament());
            }
        }
        return result;
    }

    public List<Tournament> getActiveTournamentsByTeamIdAndSportCategory(Integer teamId, SportCategory sportCategory) {
        List<TournamentParticipation> participations = tournamentParticipantService
                .getTournamentParticipationsByTeamId(teamId);
        return participations.stream()
                .filter(tp -> tp.getTournament().isTournamentActive()
                        && tp.getTournament().getSport().getSportCategory() == sportCategory)
                .map(TournamentParticipation::getTournament)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void startTournament(Integer tournamentId) {
        checkTournamentExists(tournamentId);

        if (!AccountContext.getActiveCoordinator()
                .equals(tournamentRepository.findById(tournamentId).get().getCoordinator())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        Tournament tournament = tournamentRepository.findById(tournamentId).get();
        if (tournament.getStatus() != TournamentStatus.CREATED) {
            throw new IllegalStateException(ErrorsString.TOURNAMENT_NOT_IN_CREATED_STATUS);
        }

        if (tournament.getNumTeams() != tournament.getPaidTeams()) {
            throw new IllegalStateException(ErrorsString.NUM_TEAMS_DIFFERS_FROM_PAID_TEAMS);
        }

        tournamentParticipantService.refundAllExcludedTeamsInTournament(tournament);

        System.out.println("\n\n[DEBUG - TournamentService] - Starting tournament " + tournamentId);

        matchGenerationService.generateMatchesForTournament(tournament,
                tournamentParticipantService.getTeamIdsParticipatingInTournament(tournamentId));

        System.out.println("[DEBUG - TournamentService] - Matches generated for tournament " + tournamentId);

        tournamentStandingService.generateStandingsForTournament(tournament);

        System.out.println("[DEBUG - TournamentService] - Standings generated for tournament " + tournamentId);

        tournamentStandingService.updateTeamsQualifiedInTournament(tournament, true);

        System.out.println("[DEBUG - TournamentService] - Teams qualified for tournament " + tournamentId);

        tournamentStatService.generateTournamentStats(tournament,
                tournamentParticipantService.getTeamIdsParticipatingInTournament(tournament.getId()));

        System.out.println("[DEBUG - TournamentService] - Tournament stats generated for tournament " + tournamentId);
        tournament.setStatus(TournamentStatus.SCHEDULED);

        tournamentRepository.save(tournament);
    }

    public List<TournamentDTO> getMyTournaments(SportCategory sportCategory) {
        Integer accountId = AccountContext.getAccountId();
        if (accountId == null) {
            throw new ForbiddenException(ErrorsString.AUTHENTICATION_REQUIRED);
        }
        Account account = AccountContext.getActiveAccount();
        List<TournamentDTO> myTournaments = new ArrayList<>();
        switch (account.getRole()) {
            case PLAYER:
                Player player = AccountContext.getActivePlayer();
                List<Callup> callups = tournamentParticipantService.getCallupsByPlayerId(player.getPlayerId());
                for (Callup callup : callups) {
                    Tournament tournament = callup.getTournament();
                    if (tournament.getSport().getSportCategory() == sportCategory
                            && tournament.getStatus() != TournamentStatus.CANCELED
                            && tournament.getStatus() != TournamentStatus.FINISHED) {
                        TournamentDTO tournamentDTO = TournamentDTO.fromTournament(tournament);
                        tournamentDTO.setIsParticipating(true);
                        myTournaments.add(tournamentDTO);
                    }
                }
                break;
            case COORDINATOR:
                Coordinator coordinator = AccountContext.getActiveCoordinator();
                List<Tournament> tournaments = getActiveTournamentsByCoordinatorIdAndSportCategory(
                        coordinator.getCoordinatorId(), sportCategory);
                for (Tournament tournament : tournaments) {
                    TournamentDTO tournamentDTO = TournamentDTO.fromTournament(tournament);
                    tournamentDTO.setIsParticipating(true);
                    myTournaments.add(tournamentDTO);
                }
                break;
            default:
                throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }

        List<FavoriteTournament> favoriteTournaments = favoriteTournamentRepository
                .findByAccount_IdAndTournament_Sport_SportCategory(accountId, sportCategory);

        for (FavoriteTournament favoriteTournament : favoriteTournaments) {
            Tournament tournament = favoriteTournament.getTournament();
            if (tournament.getStatus() != TournamentStatus.CANCELED
                    && tournament.getStatus() != TournamentStatus.FINISHED) {
                TournamentDTO tournamentDTO = TournamentDTO.fromTournament(tournament);
                tournamentDTO.setIsFavorite(true);
                if (!myTournaments.contains(tournamentDTO)) {
                    myTournaments.add(tournamentDTO);
                }
            }
        }
        return myTournaments;
    }

    public void checkTournamentExists(Integer tournamentId) {
        if (tournamentRepository.findById(tournamentId).isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public ApplyTournamentFiltersResponse applyTournamentFilters(SportCategory sportCategory, Integer cityId,
            Short minTeams, Short maxTeams, BigDecimal minFee, BigDecimal maxFee, BigDecimal minPrize,
            BigDecimal maxPrize,
            String name) {
        List<Tournament> tournaments = tournamentRepository.findBySport_SportCategory(sportCategory);
        if (name != null && !name.isEmpty()) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }
        if (cityId != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getField().getCity().getId().equals(cityId))
                    .toList();
        }
        if (minTeams != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getNumTeams() >= minTeams)
                    .toList();
        }
        if (maxTeams != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getNumTeams() <= maxTeams)
                    .toList();
        }
        if (minFee != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getFee().compareTo(minFee) >= 0)
                    .toList();
        }
        if (maxFee != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getFee().compareTo(maxFee) <= 0)
                    .toList();
        }
        if (minPrize != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getPrize1().compareTo(minPrize) >= 0)
                    .toList();
        }
        if (maxPrize != null) {
            tournaments = tournaments.stream()
                    .filter(tournament -> tournament.getPrize1().compareTo(maxPrize) <= 0)
                    .toList();
        }

        Map<Integer, Field> fieldsCache = new HashMap<>();
        Map<Integer, String> cityNamePerField = new HashMap<>();
        Map<Integer, Coordinator> coordinatorsCache = new HashMap<>();

        for (Tournament tournament : tournaments) {
            if (!fieldsCache.containsKey(tournament.getField().getId())) {
                fieldsCache.put(tournament.getField().getId(), tournament.getField());
                cityNamePerField.put(tournament.getField().getId(), tournament.getField().getCity().getName());
            }
            if (!coordinatorsCache.containsKey(tournament.getCoordinator().getCoordinatorId())) {
                coordinatorsCache.put(tournament.getCoordinator().getCoordinatorId(),
                        tournament.getCoordinator());
            }
        }

        tournaments.sort((t1, t2) -> t1.getStatus().getPriority() - t2.getStatus().getPriority());

        return ApplyTournamentFiltersResponse.builder()
                .tournaments(tournaments.stream().map(TournamentDTO::fromTournament).toList())
                .fieldsCache(fieldsCache.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> FieldDTO.fromField(entry.getValue()))))
                .cityNamePerField(cityNamePerField)
                .coordinatorsCache(coordinatorsCache.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> CoordinatorDTO.fromCoordinator(entry.getValue()))))
                .build();
    }

    @Transactional(readOnly = true)
    public TournamentParametersResponse getTournamentParameters(TournamentDTO fictitiousTournament) {
        if (!AccountContext.getCoordinatorId().equals(fictitiousTournament.getCoordinatorId())) {
            throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        // TODO: Implementare in base ai parametri del torneo
        Integer numMatches = matchService.calculateMatchesInTournament(fictitiousTournament);
        Optional<Sport> optionalSport = sportService.getSportById(fictitiousTournament.getSportId());
        if (optionalSport.isEmpty()) {
            throw new NotFoundException(ErrorsString.SPORT_NOT_FOUND);
        }
        Sport sport = optionalSport.get();

        BigDecimal hoursPerMatch;

        switch (sport.getSportCategory()) {
            case FOOTBALL:
                // Calcolo approssimativo: 2 tempi da x minuti + pausa da y minuti + 15 minuti
                // per preparazione e pulizia campo, tutto arrotondato per eccesso per arrivare
                // al quarto d'ora
                int totalMinutes = sport.getMinutesPerPause() + sport.getMinutesPerRound() * 2 + 15;
                BigDecimal quarters = BigDecimal.valueOf(totalMinutes)
                        .divide(BigDecimal.valueOf(15), 10, RoundingMode.HALF_UP);
                BigDecimal roundedQuarters = quarters.setScale(0, RoundingMode.HALF_UP);
                hoursPerMatch = roundedQuarters.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP);
                break;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        BigDecimal coordinatorPayPerMatch = getCoordinatorPayPerMatchByCoordinatorId(
                fictitiousTournament.getCoordinatorId());
        // La finale vale 1.5 volte un match normale
        BigDecimal coordinatorPay = coordinatorPayPerMatch.multiply(BigDecimal.valueOf(numMatches + 0.5))
                .setScale(2, RoundingMode.HALF_UP);

        Optional<Field> field = fieldService.getFieldById(fictitiousTournament.getFieldId());
        if (field.isEmpty()) {
            throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND);
        }

        Field fieldEntity = field.get();
        BigDecimal fieldCost = fieldEntity
                .calculateTournamentCost(hoursPerMatch.multiply(BigDecimal.valueOf(numMatches)))
                .setScale(2, RoundingMode.HALF_UP);

        Short medianRefereeLevel = getMedianRefereeLevel();
        Map<String, BigDecimal> refereeRolesPayPerMatch = sportService.getRefereePayPerMatchBySportIdAndLevel(
                sport.getId(), medianRefereeLevel);

        BigDecimal totalRefereePay = BigDecimal.ZERO;
        for (String role : refereeRolesPayPerMatch.keySet()) {
            totalRefereePay = totalRefereePay
                    .add(refereeRolesPayPerMatch.get(role).multiply(BigDecimal.valueOf(numMatches)));
        }
        // TODO: Aggiungere la logica di calcolo dei premi in base ai parametri
        BigDecimal atlFee = BigDecimal.valueOf(150);
        BigDecimal prize1 = fictitiousTournament.getPrize1().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(150)
                : BigDecimal.ZERO;
        BigDecimal prize2 = fictitiousTournament.getPrize2().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100)
                : BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.valueOf(10000);

        return TournamentParametersResponse.builder()
                .numMatches(numMatches)
                .coordinatorPay(coordinatorPay)
                .prize1(prize1)
                .prize2(prize2)
                .fee(totalFee)
                .build();
    }

    @Transactional(readOnly = true)
    public ScheduleMatchResponse getScheduleData(Integer tournamentId) {
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        if (tournamentOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        Tournament tournament = tournamentOpt.get();

        if (!AccountContext.getCoordinatorId().equals(tournament.getCoordinator().getCoordinatorId())) {
            throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }

        Short minutesPerRound = tournament.getSport().getMinutesPerRound();
        Short minutesPerPause = tournament.getSport().getMinutesPerPause();
        List<Booking> existingBookings = bookingService.getBookingsByFieldIdFromNow(tournament.getField().getId());

        return ScheduleMatchResponse.builder()
                .minutesPerRound(minutesPerRound)
                .minutesPerPause(minutesPerPause)
                .bookings(existingBookings.stream().map(BookingDTO::fromBooking).toList())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public TournamentDTO createTournament(TournamentDTO tournamentDTO) {
        if (!AccountContext.getCoordinatorId().equals(tournamentDTO.getCoordinatorId())) {
            throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        sportService.checkSportExists(tournamentDTO.getSportId());
        fieldService.checkFieldExists(tournamentDTO.getFieldId());
        fieldService.checkCityExists(tournamentDTO.getCityId());
        Field field = fieldService.getFieldById(tournamentDTO.getFieldId()).get();
        City city = field.getCity();

        if (!city.getId().equals(tournamentDTO.getCityId())) {
            throw new ConflictException(ErrorsString.FIELD_NOT_IN_CITY);
        }

        Tournament tournament = TournamentDTO.toTournament(tournamentDTO);

        TournamentParametersResponse parametersResponse = getTournamentParameters(tournamentDTO);
        tournament.setFee(parametersResponse.getFee());
        tournament.setPrize1(parametersResponse.getPrize1());
        tournament.setPrize2(parametersResponse.getPrize2());
        if (parametersResponse.getFee().compareTo(BigDecimal.ZERO) == 0) {
            tournament.setProfit(false);
        } else {
            tournament.setProfit(true);
        }

        Tournament savedTournament = tournamentRepository.save(tournament);
        return TournamentDTO.fromTournament(savedTournament);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer retireFromMatch(Integer matchId) {
        Player activePlayer = AccountContext.getActivePlayer();

        Optional<Match> optionalMatch = matchService.getMatchById(matchId);
        if (optionalMatch.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = optionalMatch.get();

        Optional<Team> teamOptional = teamService.getTeamByPlayerIdAndSportCategory(activePlayer.getPlayerId(),
                match.getSportCategory());
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();
        Integer teamId = team.getId();

        if (!tournamentParticipantService.isPlayerCalledUpInTournament(activePlayer.getPlayerId(),
                match.getTournament().getId())) {
            throw new UnauthorizedException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT);
        }

        teamService.needToBeCaptainOrViceCaptain(team.getId(), activePlayer.getPlayerId());

        Tournament tournament = match.getTournament();

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                Optional<FootballMatch> footballMatchOptional = matchService.getFootballMatchById(matchId);
                if (footballMatchOptional.isEmpty()) {
                    throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                }

                FootballMatch footballMatch = footballMatchOptional.get();

                FootballMatchStatus nextStatus = null;
                Boolean isHomeTeam = null;

                if (footballMatch.getHomeTeam().getId().equals(teamId)) {
                    nextStatus = FootballMatchStatus.HOME_RETIRED;
                    isHomeTeam = true;
                } else if (footballMatch.getAwayTeam().getId().equals(teamId)) {
                    nextStatus = FootballMatchStatus.AWAY_RETIRED;
                    isHomeTeam = false;
                } else {
                    throw new ConflictException(ErrorsString.TEAM_NOT_IN_MATCH);
                }

                if (tournament.getPhase() == TournamentPhase.GROUP) {
                    Team homeTeam = footballMatch.getHomeTeam();
                    Team awayTeam = footballMatch.getAwayTeam();

                    FootballStanding winnerStanding = tournamentStandingService.updateFootballStanding(
                            tournament.getId(),
                            isHomeTeam ? homeTeam.getId()
                                    : awayTeam.getId(),
                            (short) 0, (short) 3, (short) 0,
                            (short) 0, (short) 0, (short) 1);

                    FootballStanding loserStanding = tournamentStandingService.updateFootballStanding(
                            tournament.getId(),
                            isHomeTeam ? awayTeam.getId()
                                    : homeTeam.getId(),
                            (short) 3, (short) 0, (short) 0,
                            (short) 1, (short) 0, (short) 0);

                    if (winnerStanding.getGroupName().equals(loserStanding.getGroupName())) {
                        tournamentStandingService.orderStandingsForTournament(tournament.getId());
                    } else {
                        throw new ConflictException(ErrorsString.TEAM_NOT_IN_MATCH);
                    }
                    matchService.updateFootballMatch(matchId,
                            isHomeTeam ? (short) 0 : (short) 3,
                            isHomeTeam ? (short) 3 : (short) 0,
                            null, null, null, null,
                            nextStatus);
                } else {
                    matchService.handleTeamRetireInKnockoutPhaseMatch(match.getId(), teamId);
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        match.setFinishMatch(LocalDateTime.now());
        matchService.saveMatch(match);
        return match.getId();
    }

    public Integer countActiveTournamentsByTeamIdAndPlayerId(Integer teamId, Integer playerId) {
        teamService.checkTeamExists(teamId);
        List<Tournament> tournaments = getActiveTournamentsByTeamId(teamId);

        Integer count = 0;
        for (Tournament tournament : tournaments) {
            Optional<Callup> callup = tournamentParticipantService.getCallupByPlayerIdAndTournamentId(playerId,
                    tournament.getId());
            if (callup.isPresent()) {
                count++;
            }
        }
        return count;
    }

    public BigDecimal getCoordinatorPayPerMatchByCoordinatorId(Integer coordinatorId) {
        Coordinator coordinator = getExistingCoordinatorByCoordinatorId(coordinatorId);
        Short level = coordinator.getLevel();
        if (level == null) {
            level = 1; // Default level if not set
        }

        BigDecimal pay = growthStepRepository.findCoordinatorPayPerMatchByLevel(level);
        return pay;
    }

    private Short getMedianRefereeLevel() {
        List<Short> levels = refereeRepository.findAllLevelsOrdered();
        if (levels == null || levels.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
        }
        int n = levels.size();
        int index = (n - 1) / 2; // odd -> middle; even -> lower middle
        return levels.get(index);
    }

    public void toggleTournamentFavoriteStatus(Integer tournamentId){
        Account account = AccountContext.getActiveAccount();
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));

        Optional<FavoriteTournament> favoriteOpt = favoriteTournamentRepository.findByAccount_IdAndTournament_Id(account.getId(), tournamentId);
        if (favoriteOpt.isPresent()) {
            favoriteTournamentRepository.delete(favoriteOpt.get());
        } else {
            FavoriteTournament favoriteTournament = new FavoriteTournament();
            favoriteTournament.setAccount(account);
            favoriteTournament.setTournament(tournament);
            favoriteTournamentRepository.save(favoriteTournament);
        }
    }
}