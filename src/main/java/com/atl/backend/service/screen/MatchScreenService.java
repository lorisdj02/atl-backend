package com.atl.backend.service.screen;

import com.atl.backend.service.TournamentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.Match;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeAssignment;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.FootballMatchResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.MatchService;
import com.atl.backend.service.SportService;
import com.atl.backend.exception.*;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchScreenService {
    @Autowired
    private MatchService matchService;
    @Autowired
    private SportService sportService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private AccountService accountService;

    @Transactional(readOnly = true)
    public AvailableMatchesResponse getAvailableMatchesScreen(SportCategory sportCategory) {
        AccountContext.isReferee();
        List<Sport> sports = sportService.getSportsBySportCategory(sportCategory);
        Map<Integer, String> sportNames = new HashMap<>();
        Map<Integer, Team> teamsCache = new HashMap<>();
        Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
        Map<Integer, Short> minutesPausePerTournament = new HashMap<>();

        switch (sportCategory) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = matchService.getScheduledFootballMatches();
                Map<Integer, List<FootballMatch>> matchesPerSport = new HashMap<>();
                for (Sport sport : sports) {
                    sportNames.put(sport.getId(), sport.getName());
                    matchesPerSport.put(sport.getId(), footballMatches.stream()
                            .filter(m -> m.getMatch().getTournament().getSport().getId().equals(sport.getId()))
                            .toList());
                }
                for (FootballMatch match : footballMatches) {
                    Tournament tournament = match.getMatch().getTournament();
                    Sport sport = tournament.getSport();
                    if (match.getHomeTeam() != null) {
                        teamsCache.put(match.getHomeTeam().getId(), match.getHomeTeam());
                    }
                    if (match.getAwayTeam() != null) {
                        teamsCache.put(match.getAwayTeam().getId(), match.getAwayTeam());
                    }
                    if (!minutesRoundPerTournament.containsKey(tournament.getId())) {
                        minutesRoundPerTournament.put(tournament.getId(),
                                sport.getMinutesPerRound());
                    }
                    if (!minutesPausePerTournament.containsKey(tournament.getId())) {
                        minutesPausePerTournament.put(tournament.getId(),
                                sport.getMinutesPerPause());
                    }
                }

                return AvailableMatchesResponse.builder()
                        .footballMatches(footballMatches.stream().map(FootballMatchDTO::fromFootballMatch).toList())
                        .teamsCache(teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> TeamDTO.fromTeam(e.getValue()))))
                        .minutesRoundPerTournament(minutesRoundPerTournament)
                        .minutesPausePerTournament(minutesPausePerTournament)
                        .footballMatchesPerSport(matchesPerSport.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        e -> e.getValue().stream().map(FootballMatchDTO::fromFootballMatch).toList())))
                        .sportNames(sportNames)
                        .build();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(readOnly = true)
    public PastAssignedMatchesResponse getPastAssignedMatchesScreen(SportCategory sportCategory) {
        Referee referee = AccountContext.getActiveReferee();
        Map<Integer, Team> teamsCache = new HashMap<>();
        Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
        Map<Integer, Short> minutesPausePerTournament = new HashMap<>();
        switch (sportCategory) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = matchService
                        .getAssignedFootballMatchesByRefereeId(referee.getRefereeId());

                for (FootballMatch match : footballMatches) {
                    Sport sport = match.getMatch().getTournament().getSport();
                    Tournament tournament = match.getMatch().getTournament();
                    if (match.getHomeTeam() != null) {
                        teamsCache.put(match.getHomeTeam().getId(), match.getHomeTeam());
                    }
                    if (match.getAwayTeam() != null) {
                        teamsCache.put(match.getAwayTeam().getId(), match.getAwayTeam());
                    }
                    if (!minutesRoundPerTournament.containsKey(tournament.getId())) {
                        minutesRoundPerTournament.put(tournament.getId(),
                                sport.getMinutesPerRound());
                    }
                    if (!minutesPausePerTournament.containsKey(tournament.getId())) {
                        minutesPausePerTournament.put(tournament.getId(),
                                sport.getMinutesPerPause());
                    }
                }

                return PastAssignedMatchesResponse.builder()
                        .footballMatches(footballMatches.stream().map(FootballMatchDTO::fromFootballMatch).toList())
                        .teamsCache(teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> TeamDTO.fromTeam(e.getValue()))))
                        .minutesRoundPerTournament(minutesRoundPerTournament)
                        .minutesPausePerTournament(minutesPausePerTournament)
                        .build();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

    }

    public Optional<?> getMatchScreen(Integer matchId) {
        Optional<Match> matchOpt = matchService.getMatchById(matchId);
        if (matchOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = matchOpt.get();
        SportCategory sportCategory = match.getTournament().getSport().getSportCategory();

        switch (sportCategory) {
            case FOOTBALL -> {
                return Optional.of(getFootballMatchScreen(matchId));
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public FootballMatchResponse getFootballMatchScreen(Integer matchId) {
        Optional<FootballMatch> footballMatchOpt = matchService.getFootballMatchById(matchId);
        if (footballMatchOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        FootballMatch footballMatch = footballMatchOpt.get();
        Tournament tournament = footballMatch.getMatch().getTournament();
        List<String> requiredRoles = new ArrayList<>();
        Integer sportId = footballMatch.getMatch().getTournament().getSport().getId();
        if (sportId == 1 || sportId == 2 || sportId == 3) {
            requiredRoles.add("MAIN");
        } else if (sportId == 4) {
            requiredRoles.add("MAIN");
            requiredRoles.add("ASSISTANT1");
            requiredRoles.add("ASSISTANT2");
        }
        List<RefereeAssignment> refereeAssignments = matchService
                .getRefereeAssignmentsForMatch(footballMatch.getMatch().getId());
        List<String> assignedRoles = refereeAssignments.stream()
                .map(RefereeAssignment::getRole)
                .toList();
        List<String> availableRoles = requiredRoles.stream()
                .filter(r -> !assignedRoles.contains(r))
                .toList();

        Map<String, String> assignmentPerRefereeNameSurname = new HashMap<>();
        for (RefereeAssignment refereeAssignment : refereeAssignments) {
            assignmentPerRefereeNameSurname.put(
                    refereeAssignment.getReferee().getAccount().getName() + " "
                            + refereeAssignment.getReferee().getAccount().getSurname(),
                    refereeAssignment.getRole());
        }

        Map<Integer, List<PlayerDTO>> playersPerTeam = new HashMap<>();
        if (footballMatch.getHomeTeam() != null) {
            Team homeTeam = footballMatch.getHomeTeam();
            List<Integer> calluppedPlayerIds = tournamentService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournament.getId(),
                    homeTeam.getId());
            List<PlayerDTO> calluppedPlayers = new ArrayList<>();
            for (Integer playerId : calluppedPlayerIds) {
                Optional<Player> optionalPlayer = accountService.getPlayerByPlayerId(playerId);
                if (optionalPlayer.isEmpty()) {
                    throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
                }
                Player player = optionalPlayer.get();
                calluppedPlayers.add(PlayerDTO.fromPlayer(player));
            }
            playersPerTeam.put(homeTeam.getId(),
                    calluppedPlayers);
        }
        if (footballMatch.getAwayTeam() != null) {
            Team awayTeam = footballMatch.getAwayTeam();
            List<Integer> calluppedPlayerIds = tournamentService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournament.getId(),
                    awayTeam.getId());
            List<PlayerDTO> calluppedPlayers = new ArrayList<>();
            for (Integer playerId : calluppedPlayerIds) {
                Optional<Player> optionalPlayer = accountService.getPlayerByPlayerId(playerId);
                if (optionalPlayer.isEmpty()) {
                    throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
                }
                Player player = optionalPlayer.get();
                calluppedPlayers.add(PlayerDTO.fromPlayer(player));
            }
            playersPerTeam.put(awayTeam.getId(),
                    calluppedPlayers);
        }

        Boolean canMatchFinish = matchService.canMatchFinish(footballMatch.getId());
        // at least one referee assigned and all required roles covered
        Boolean refereeCheck = availableRoles.isEmpty() && assignmentPerRefereeNameSurname.size() > 0;
        Boolean hasRefereeLicense = false;
        Map<String, BigDecimal> payPerRole = new HashMap<>();
        if (AccountContext.getAccountRole() == AccountRole.REFEREE) {
            Referee referee = AccountContext.getActiveReferee();
            hasRefereeLicense = sportService.getRefereeLicensesByRefereeIdAndSportId(referee.getRefereeId(), sportId)
                    .isPresent();
            List<RefereeRole> refereeRoles = sportService.getRefereeRolesBySportId(sportId);
            if (hasRefereeLicense) {
                for (String role : requiredRoles) {
                    BigDecimal pay = refereeRoles.stream()
                            .filter(rr -> rr.getName().equals(role) && rr.getLevel() == referee.getLevel())
                            .findFirst()
                            .map(RefereeRole::getPayPerMatch)
                            .orElse(BigDecimal.ZERO);
                    payPerRole.put(role, pay);
                }
            }
        }

        List<FootballMatchEvent> events = matchService
                .getFootballMatchEventsByMatchId(footballMatch.getMatch().getId());

        return FootballMatchResponse.builder()
                .match(FootballMatchDTO.fromFootballMatch(footballMatch))
                .homeTeam(footballMatch.getHomeTeam() != null ? TeamDTO.fromTeam(footballMatch.getHomeTeam()) : null)
                .awayTeam(footballMatch.getAwayTeam() != null ? TeamDTO.fromTeam(footballMatch.getAwayTeam()) : null)
                .events(events.stream().map(e -> FootballMatchEventDTO.fromFootballMatchEvent(e)).toList())
                .minutesPerRound(tournament.getSport().getMinutesPerRound())
                .minutesPerPause(tournament.getSport().getMinutesPerPause())
                .availableRefereeRoles(availableRoles)
                .playersPerTeam(playersPerTeam)
                .assignmentPerRefereeNameSurname(assignmentPerRefereeNameSurname)
                .field(FieldDTO.fromField(tournament.getField()))
                .cityName(tournament.getField().getCity().getName())
                .coordinatorId(tournament.getCoordinator().getCoordinatorId())
                .refereeCheck(refereeCheck)
                .canMatchFinish(canMatchFinish)
                .hasRefereeLicense(hasRefereeLicense)
                .payPerRole(payPerRole)
                .build();
    }
}
