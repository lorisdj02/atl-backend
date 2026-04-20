package com.atl.backend.service.matchServices;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.FootballEventType;
import com.atl.backend.model.enums.FootballMatchStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.FootballMatchEventRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentStandingService;
import com.atl.backend.service.tournamentServices.TournamentStatService;

@Service
public class FootballMatchEventService {
    @Autowired
    private FootballMatchEventRepository footballMatchEventRepository;
    @Autowired
    private FootballMatchRepository footballMatchRepository;
    @Autowired
    private MatchService matchService;
    @Autowired
    private TournamentStatService tournamentStatService;
    @Autowired
    private TournamentParticipantService tournamentParticipantService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private TournamentStandingService tournamentStandingService;

    private Team getPlayerTeam(Integer playerId, Integer tournamentId) {
        Team team = teamService.getTeamByPlayerIdAndSportCategory(playerId, SportCategory.FOOTBALL)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_IN_TEAM));
        Callup callup = tournamentParticipantService.getCallupByPlayerIdAndTournamentId(playerId, tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT));
        if (!callup.getTeam().getId().equals(team.getId())) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT);
        }
        return team;
    }

    public Boolean hasPlayerReceivedYellowCard(Integer matchId, Integer playerId) {
        return footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                FootballEventType.YELLOW_CARD);
    }

    public Boolean hasPlayerReceivedRedCard(Integer matchId, Integer playerId) {
        return footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                FootballEventType.RED_CARD)
                || footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                        FootballEventType.RED_YELLOW_CARD);
    }

    public FootballMatchEvent insertFootballMatchEvent(Integer matchId, FootballMatchEventDTO footballMatchEventDTO) {
        FootballMatch footballMatch = footballMatchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        FootballEventType eventType;
        try {
            eventType = FootballEventType.valueOf(footballMatchEventDTO.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorsString.INVALID_FOOTBALL_EVENT_TYPE);
        }

        Tournament tournament = footballMatch.getMatch().getTournament();
        if (!tournament.getCoordinator().getCoordinatorId().equals(AccountContext.getCoordinatorId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        FootballMatchEvent event = new FootballMatchEvent();
        EventMinuteData eventMinuteData = calculateEventMinuteData(footballMatch, eventType);

        event.setMatch(footballMatch);

        switch (eventType) {
            case START:
                matchService.startMatch(matchId);
                Team homeTeam = footballMatch.getHomeTeam();
                Team awayTeam = footballMatch.getAwayTeam();

                // All'inizio della partita, assegno i punti per il pareggio, così da aggiornare
                // subito live la classifica
                tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                        homeTeam.getId(), (short) 1, (short) 0, (short) 0, (short) 0,
                        (short) 1, (short) 0);

                tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                        awayTeam.getId(), (short) 1, (short) 0, (short) 0, (short) 0,
                        (short) 1, (short) 0);
                break;

            case HALFTIME:
                matchService.startFootballHalftime(matchId);
                break;

            case START_HALF:
                matchService.startFootballSecondHalf(matchId);
                break;

            case PENALTY_SHOOT_OUT:
                matchService.startFootballPenaltyShootout(matchId);
                break;

            case FINISH:
                matchService.finishMatch(matchId);
                break;
            default:

                Player player = footballMatchEventDTO.getPlayerId() != null
                        ? playerService.getPlayerByPlayerId(footballMatchEventDTO.getPlayerId())
                                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND))
                        : null;
                if (player == null) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }
                Team playerTeam = getPlayerTeam(player.getPlayerId(), tournament.getId());

                if (hasPlayerReceivedYellowCard(footballMatch.getId(), player.getPlayerId())
                        && eventType == FootballEventType.YELLOW_CARD) {
                    eventType = FootballEventType.RED_YELLOW_CARD;
                }

                handlePlayerEvent(footballMatch, eventType, player, playerTeam);
                event.setPlayer(player);
                event.setTeam(playerTeam);
        }

        event.setMinute(eventMinuteData.minute());
        event.setAdditionalMinute(eventMinuteData.additionalMinute());
        event.setType(eventType);
        return footballMatchEventRepository.save(event);
    }

    private EventMinuteData calculateEventMinuteData(FootballMatch footballMatch, FootballEventType eventType) {
        int minutesPerRound = footballMatch.getMatch().getTournament().getSport().getMinutesPerRound();

        if (eventType == FootballEventType.START) {
            return new EventMinuteData((short) 0, (short) 0);
        }

        if (eventType == FootballEventType.START_HALF) {
            return new EventMinuteData((short) minutesPerRound, (short) 0);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentHalfStart;
        int minuteOffset;

        if (footballMatch.getStartHalf() != null) {
            currentHalfStart = footballMatch.getStartHalf();
            minuteOffset = minutesPerRound;
        } else if (footballMatch.getMatch().getStartMatch() != null) {
            currentHalfStart = footballMatch.getMatch().getStartMatch();
            minuteOffset = 0;
        } else {
            return new EventMinuteData((short) 0, (short) 0);
        }

        int elapsedMinutes = (int) Math.max(0L, ChronoUnit.MINUTES.between(currentHalfStart, now));
        int minute = minuteOffset + Math.min(minutesPerRound, elapsedMinutes);
        short additionalMinute = (short) Math.max(0, elapsedMinutes - minutesPerRound);

        return new EventMinuteData((short) minute, additionalMinute);
    }

    private record EventMinuteData(short minute, short additionalMinute) {
    }

    private void handlePlayerEvent(FootballMatch footballMatch, FootballEventType eventType, Player player,
            Team playerTeam) {
        Tournament tournament = footballMatch.getMatch().getTournament();

        if (hasPlayerReceivedRedCard(footballMatch.getId(), player.getPlayerId())) {
            throw new BadRequestException(ErrorsString.PLAYER_HAS_RED_CARD);
        }

        Boolean isPlayerInHomeTeam = footballMatch.getHomeTeam().getId().equals(playerTeam.getId());

        if (!isPlayerInHomeTeam &&
                !footballMatch.getAwayTeam().getId().equals(playerTeam.getId())) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }

        Boolean isPenaltyShootOut = footballMatch.getStatus() == FootballMatchStatus.PENALTY_SHOOT_OUT;

        Boolean isMatchStarted = footballMatch.getMatch().getStartMatch() != null;
        Boolean isMatchFinished = footballMatch.getMatch().getFinishMatch() != null;
        // Se è halftime, non è ancora iniziata la seconda metà, quindi non si possono
        // aggiungere eventi
        Boolean isHalftime = footballMatch.getHalftime() != null;
        Boolean isSecondHalfStarted = footballMatch.getStartHalf() != null;

        if (!isMatchStarted || isMatchFinished || (isHalftime && !isSecondHalfStarted)) {
            throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
        }

        switch (eventType) {
            case YELLOW_CARD, RED_CARD, RED_YELLOW_CARD:

                if (player == null || playerTeam == null) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                Boolean isYellowCard = eventType == FootballEventType.YELLOW_CARD;

                tournamentStatService.updateFootballTournamentStats(tournament.getId(), player.getPlayerId(),
                        playerTeam.getId(), (short) 0, (short) 0, (short) (isYellowCard ? 0 : 1),
                        (short) (isYellowCard ? 1 : 0));

                teamService.updateFootballTeamStat(player.getPlayerId(),
                        playerTeam.getId(), (short) 0, (short) 0, (short) (isYellowCard ? 0 : 1),
                        (short) (isYellowCard ? 1 : 0));
                break;

            case GOAL, OWN_GOAL:
                if (isPenaltyShootOut) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }
                if (matchService.isMatchInGroupStage(footballMatch.getId())) {
                    handleGoalInGroupStage(footballMatch, eventType, player, playerTeam, isPlayerInHomeTeam);
                } else {
                    handleGoalInFinalStage(footballMatch, eventType, player, playerTeam, isPlayerInHomeTeam);
                }

                break;
            case PENALTY:

                if (matchService.isMatchInGroupStage(footballMatch.getId())) {
                    handleGoalInGroupStage(footballMatch, eventType, player, playerTeam, isPlayerInHomeTeam);
                } else {
                    if (!isPenaltyShootOut) {
                        handleGoalInFinalStage(footballMatch, eventType, player, playerTeam, isPlayerInHomeTeam);
                    } else {
                        matchService.updateFootballMatch(footballMatch.getId(), null,
                                null, isPlayerInHomeTeam ? (short) 1 : (short) 0,
                                isPlayerInHomeTeam ? (short) 0 : (short) 1, null,
                                null, null);
                    }
                }

                break;
            case MISSED_PENALTY:
                if (isPenaltyShootOut) {
                    matchService.updateFootballMatch(footballMatch.getId(), null,
                            null, null, null, isPlayerInHomeTeam ? (short) 1 : (short) 0,
                            isPlayerInHomeTeam ? (short) 0 : (short) 1,
                            null);
                }
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_FOOTBALL_EVENT_TYPE);
        }
    }

    private void handleGoalInGroupStage(FootballMatch footballMatch, FootballEventType eventType, Player player,
            Team playerTeam, Boolean isPlayerInHomeTeam) {
        Boolean isOwnGoal = eventType == FootballEventType.OWN_GOAL;
        Boolean isHomeTeamGoal = isPlayerInHomeTeam && !isOwnGoal || !isPlayerInHomeTeam && isOwnGoal;

        Team homeTeam = footballMatch.getHomeTeam();
        Team awayTeam = footballMatch.getAwayTeam();

        Short homeScoreDelta = isHomeTeamGoal ? (short) 1 : 0;
        Short awayScoreDelta = isHomeTeamGoal ? (short) 0 : 1;
        Short homePtsDelta = 0;
        Short awayPtsDelta = 0;
        Short homeWonDelta = 0;
        Short awayWonDelta = 0;
        Short homeDrawnDelta = 0;
        Short awayDrawnDelta = 0;
        Short homeLostDelta = 0;
        Short awayLostDelta = 0;

        if (footballMatch.isDraw()) {
            homePtsDelta = isHomeTeamGoal ? (short) 2 : -1;
            awayPtsDelta = isHomeTeamGoal ? (short) -1 : 2;

            homeWonDelta = isHomeTeamGoal ? (short) 1 : 0;
            awayWonDelta = isHomeTeamGoal ? (short) 0 : 1;

            homeDrawnDelta = -1;
            awayDrawnDelta = -1;

            homeLostDelta = isHomeTeamGoal ? (short) 0 : 1;
            awayLostDelta = isHomeTeamGoal ? (short) 1 : 0;
        } else if (footballMatch.isHomeWinningByOneGoal()) {
            homePtsDelta = isHomeTeamGoal ? (short) 0 : -2;
            awayPtsDelta = isHomeTeamGoal ? (short) 0 : 1;

            homeWonDelta = isHomeTeamGoal ? (short) 0 : -1;
            awayWonDelta = 0;

            homeDrawnDelta = isHomeTeamGoal ? (short) 0 : 1;
            awayDrawnDelta = isHomeTeamGoal ? (short) 0 : 1;

            homeLostDelta = 0;
            awayLostDelta = isHomeTeamGoal ? (short) 0 : -1;
        } else if (footballMatch.isAwayWinningByOneGoal()) {
            homePtsDelta = isHomeTeamGoal ? (short) 1 : 0;
            awayPtsDelta = isHomeTeamGoal ? (short) -2 : 0;

            homeWonDelta = 0;
            awayWonDelta = isHomeTeamGoal ? (short) -1 : 0;

            homeDrawnDelta = isHomeTeamGoal ? (short) 1 : 0;
            awayDrawnDelta = isHomeTeamGoal ? (short) 1 : 0;

            homeLostDelta = isHomeTeamGoal ? (short) -1 : 0;
            awayLostDelta = 0;
        } else {
            homePtsDelta = 0;
            awayPtsDelta = 0;
        }

        matchService.updateFootballMatch(footballMatch.getId(), homeScoreDelta,
                awayScoreDelta, null, null, null,
                null, null);

        tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                homeTeam.getId(), homePtsDelta, homeScoreDelta, awayScoreDelta, homeWonDelta,
                homeDrawnDelta, homeLostDelta);

        tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                awayTeam.getId(), awayPtsDelta, awayScoreDelta, homeScoreDelta, awayWonDelta,
                awayDrawnDelta, awayLostDelta);

        if (isOwnGoal) {
            return;
        }

        tournamentStatService.updateFootballTournamentStats(footballMatch.getMatch().getTournament().getId(),
                player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);

        teamService.updateFootballTeamStat(player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);
    }

    private void handleGoalInFinalStage(FootballMatch footballMatch, FootballEventType eventType, Player player,
            Team playerTeam, Boolean isPlayerInHomeTeam) {

        Boolean isOwnGoal = eventType == FootballEventType.OWN_GOAL;
        Boolean isHomeTeamGoal = isPlayerInHomeTeam && !isOwnGoal || !isPlayerInHomeTeam && isOwnGoal;
        matchService.updateFootballMatch(footballMatch.getId(), isHomeTeamGoal ? (short) 1 : (short) 0,
                isHomeTeamGoal ? (short) 0 : (short) 1, null, null, null,
                null, null);

        if (isOwnGoal) {
            return;
        }

        tournamentStatService.updateFootballTournamentStats(footballMatch.getMatch().getTournament().getId(),
                player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);

        teamService.updateFootballTeamStat(player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);

    }
}