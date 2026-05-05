package com.atl.backend.service.matchServices;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.dto.FootballMatchEventModifiedValues;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
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
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentAuthService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentStandingService;
import com.atl.backend.service.tournamentServices.TournamentStatService;

@Service
public class FootballMatchEventService {
    @Autowired
    private TournamentAuthService tournamentAuthService;
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

    public void deleteAllFootballMatchEvents() {
        footballMatchEventRepository.deleteAll();
    }

    private Team getPlayerTeam(Long playerId, Long tournamentId) {
        Team team = teamService.getTeamByPlayerIdAndSportCategory(playerId, SportCategory.FOOTBALL)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_IN_TEAM));
        Callup callup = tournamentParticipantService.getCallupByPlayerIdAndTournamentId(playerId, tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT));
        if (!callup.getTeam().getId().equals(team.getId())) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT);
        }
        return team;
    }

    public Boolean hasPlayerReceivedYellowCard(Long matchId, Long playerId) {
        return footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                FootballEventType.YELLOW_CARD);
    }

    public Boolean hasPlayerReceivedRedCard(Long matchId, Long playerId) {
        return footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                FootballEventType.RED_CARD)
                || footballMatchEventRepository.existsByMatch_IdAndPlayer_PlayerIdAndType(matchId, playerId,
                        FootballEventType.RED_YELLOW_CARD);
    }

    @Transactional(rollbackFor = Exception.class)
    public FootballMatchEvent insertFootballMatchEvent(Long matchId, FootballMatchEventDTO footballMatchEventDTO) {
        FootballMatch footballMatch = footballMatchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        FootballEventType eventType;
        try {
            eventType = FootballEventType.valueOf(footballMatchEventDTO.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorsString.INVALID_FOOTBALL_EVENT_TYPE);
        }

        Tournament tournament = footballMatch.getMatch().getTournament();
        tournamentAuthService.canManageTournament(tournament.getId());

        FootballMatchEvent event = new FootballMatchEvent();
        EventMinuteData eventMinuteData = calculateEventMinuteData(footballMatch, eventType);
        FootballMatchEventModifiedValues modifiedValues = null;
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

                if (hasPlayerReceivedYellowCard(footballMatch.getMatch().getId(), player.getPlayerId())
                        && eventType == FootballEventType.YELLOW_CARD) {
                    eventType = FootballEventType.RED_YELLOW_CARD;
                }

                modifiedValues = handlePlayerEvent(footballMatch, eventType, player, playerTeam);
                event.setPlayer(player);
                event.setTeam(playerTeam);
        }

        event.setMinute(eventMinuteData.minute());
        event.setAdditionalMinute(eventMinuteData.additionalMinute());
        event.setType(eventType);
        event.setModifiedValues(modifiedValues);
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

        Instant now = Instant.now();
        Instant currentHalfStart;
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

    @Transactional(rollbackFor = Exception.class)
    private FootballMatchEventModifiedValues handlePlayerEvent(FootballMatch footballMatch, FootballEventType eventType,
            Player player,
            Team playerTeam) {

        if (hasPlayerReceivedRedCard(footballMatch.getMatch().getId(), player.getPlayerId())) {
            throw new BadRequestException(ErrorsString.PLAYER_HAS_RED_CARD);
        }

        if (footballMatch.getHomeTeam() == null || footballMatch.getAwayTeam() == null) {
            throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
        }

        Boolean isPlayerInHomeTeam = playerTeam.getId().equals(footballMatch.getHomeTeam().getId());

        if (!isPlayerInHomeTeam && !playerTeam.getId().equals(footballMatch.getAwayTeam().getId())) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }

        Boolean isPenaltyShootOut = footballMatch.getStatus() == FootballMatchStatus.PENALTY_SHOOT_OUT;

        // Se è halftime, non è ancora iniziata la seconda metà, quindi non si possono
        // aggiungere eventi

        FootballMatchEventModifiedValues modifiedValues = new FootballMatchEventModifiedValues();

        if (!footballMatch.isStarted() || footballMatch.isFinished() || footballMatch.isHalftime()) {
            throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
        }

        switch (eventType) {
            case YELLOW_CARD, RED_CARD, RED_YELLOW_CARD:

                if (player == null || playerTeam == null) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                Boolean isYellowCard = eventType == FootballEventType.YELLOW_CARD;

                updatePlayerStat(footballMatch.getMatch().getTournament(), player, playerTeam, null, null,
                        isYellowCard ? (short) 1 : null, !isYellowCard ? (short) 1 : null);
                break;

            case GOAL, OWN_GOAL:
                if (isPenaltyShootOut) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }
                if (matchService.isMatchInGroupStage(footballMatch.getMatch().getId())) {
                    modifiedValues = handleGoalInGroupStage(footballMatch, eventType, player, playerTeam,
                            isPlayerInHomeTeam);
                } else {
                    modifiedValues = handleGoalInFinalStage(footballMatch, eventType, player, playerTeam,
                            isPlayerInHomeTeam);
                }

                break;
            case PENALTY:

                if (matchService.isMatchInGroupStage(footballMatch.getMatch().getId())) {
                    modifiedValues = handleGoalInGroupStage(footballMatch, eventType, player, playerTeam,
                            isPlayerInHomeTeam);
                } else {
                    if (!isPenaltyShootOut) {
                        modifiedValues = handleGoalInFinalStage(footballMatch, eventType, player, playerTeam,
                                isPlayerInHomeTeam);
                    } else {
                        Short penaltyScoreHomeDelta = isPlayerInHomeTeam ? (short) 1 : null;
                        Short penaltyScoreAwayDelta = !isPlayerInHomeTeam ? (short) 1 : null;
                        updateResultAndBracketInFinalStage(footballMatch, null, null, penaltyScoreHomeDelta,
                                penaltyScoreAwayDelta, null, null);
                        modifiedValues.setPenaltyScoreHomeDelta(penaltyScoreHomeDelta);
                        modifiedValues.setPenaltyScoreAwayDelta(penaltyScoreAwayDelta);
                    }
                }

                break;
            case MISSED_PENALTY:
                if (isPenaltyShootOut) {
                    Short missedPenaltyHomeDelta = isPlayerInHomeTeam ? (short) 1 : null;
                    Short missedPenaltyAwayDelta = !isPlayerInHomeTeam ? (short) 1 : null;
                    matchService.updateFootballMatch(footballMatch.getMatch().getId(), null,
                            null, null, null, missedPenaltyHomeDelta,
                            missedPenaltyAwayDelta,
                            null);
                    modifiedValues.setMissedPenaltyHomeDelta(missedPenaltyHomeDelta);
                    modifiedValues.setMissedPenaltyAwayDelta(missedPenaltyAwayDelta);
                }
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_FOOTBALL_EVENT_TYPE);
        }
        return modifiedValues;
    }

    private FootballMatchEventModifiedValues handleGoalInGroupStage(FootballMatch footballMatch,
            FootballEventType eventType, Player player,
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

        FootballMatchEventModifiedValues modifiedValues = new FootballMatchEventModifiedValues();

        modifiedValues.setHomeScoreDelta(homeScoreDelta);
        modifiedValues.setAwayScoreDelta(awayScoreDelta);
        modifiedValues.setHomePtsDelta(homePtsDelta);
        modifiedValues.setAwayPtsDelta(awayPtsDelta);
        modifiedValues.setHomeWonDelta(homeWonDelta);
        modifiedValues.setAwayWonDelta(awayWonDelta);
        modifiedValues.setHomeDrawnDelta(homeDrawnDelta);
        modifiedValues.setAwayDrawnDelta(awayDrawnDelta);
        modifiedValues.setHomeLostDelta(homeLostDelta);
        modifiedValues.setAwayLostDelta(awayLostDelta);

        updateResultAndStandings(footballMatch, homeTeam, awayTeam, homeScoreDelta, awayScoreDelta,
                homePtsDelta, awayPtsDelta, homeWonDelta, awayWonDelta, homeDrawnDelta, awayDrawnDelta, homeLostDelta,
                awayLostDelta);

        if (!isOwnGoal) {
            updatePlayerStat(footballMatch.getMatch().getTournament(), player, playerTeam, (short) 1, null, null, null);
        }

        return modifiedValues;
    }

    private FootballMatchEventModifiedValues handleGoalInFinalStage(FootballMatch footballMatch,
            FootballEventType eventType, Player player,
            Team playerTeam, Boolean isPlayerInHomeTeam) {

        Boolean isOwnGoal = eventType == FootballEventType.OWN_GOAL;
        Boolean isHomeTeamGoal = isPlayerInHomeTeam && !isOwnGoal || !isPlayerInHomeTeam && isOwnGoal;

        Short homeScoreDelta = isHomeTeamGoal ? (short) 1 : 0;
        Short awayScoreDelta = isHomeTeamGoal ? (short) 0 : 1;

        FootballMatchEventModifiedValues modifiedValues = new FootballMatchEventModifiedValues();
        modifiedValues.setHomeScoreDelta(homeScoreDelta);
        modifiedValues.setAwayScoreDelta(awayScoreDelta);

        updateResultAndBracketInFinalStage(footballMatch, homeScoreDelta, awayScoreDelta, null, null, null, null);

        if (isOwnGoal) {
            return modifiedValues;
        }

        updatePlayerStat(footballMatch.getMatch().getTournament(), player, playerTeam, (short) 1, null, null, null);
        return modifiedValues;
    }

    private void updateResultAndStandings(FootballMatch footballMatch, Team homeTeam, Team awayTeam,
            Short homeScoreDelta, Short awayScoreDelta,
            Short homePtsDelta, Short awayPtsDelta, Short homeWonDelta, Short awayWonDelta,
            Short homeDrawnDelta, Short awayDrawnDelta, Short homeLostDelta, Short awayLostDelta) {
        matchService.updateFootballMatch(footballMatch.getMatch().getId(), homeScoreDelta,
                awayScoreDelta, null, null, null,
                null, null);

        tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                homeTeam.getId(), homePtsDelta, homeScoreDelta, awayScoreDelta, homeWonDelta,
                homeDrawnDelta, homeLostDelta);

        tournamentStandingService.updateFootballStanding(footballMatch.getMatch().getTournament().getId(),
                awayTeam.getId(), awayPtsDelta, awayScoreDelta, homeScoreDelta, awayWonDelta,
                awayDrawnDelta, awayLostDelta);

        tournamentStandingService.updateTeamsQualifiedInTournament(footballMatch.getMatch().getTournament());
    }

    private void updateResultAndBracketInFinalStage(FootballMatch footballMatch, Short homeScoreDelta,
            Short awayScoreDelta, Short penaltyScoreHomeDelta, Short penaltyScoreAwayDelta,
            Short missedPenaltyHomeDelta, Short missedPenaltyAwayDelta) {
        matchService.updateFootballMatch(footballMatch.getMatch().getId(), homeScoreDelta,
                awayScoreDelta, penaltyScoreHomeDelta, penaltyScoreAwayDelta,
                missedPenaltyHomeDelta, missedPenaltyAwayDelta, null);

        matchService.handleFinalStageBracketUpdate(footballMatch.getMatch().getId());
    }

    private void updatePlayerStat(Tournament tournament, Player player, Team playerTeam, Short goalsDelta,
            Short motmDelta, Short yellowCardsDelta, Short redCardsDelta) {
        tournamentStatService.updateFootballTournamentStats(tournament.getId(),
                player.getPlayerId(),
                playerTeam.getId(), goalsDelta, motmDelta, yellowCardsDelta,
                redCardsDelta);

        teamService.updateFootballTeamStat(player.getPlayerId(),
                playerTeam.getId(), goalsDelta, motmDelta, yellowCardsDelta,
                redCardsDelta);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFootballMatchEvent(Long matchId, Long eventId) {
        FootballMatchEvent event = footballMatchEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_EVENT_NOT_FOUND));

        if (!event.getMatch().getId().equals(matchId)) {
            throw new NotFoundException(ErrorsString.MATCH_EVENT_NOT_FOUND);
        }

        Tournament tournament = event.getMatch().getMatch().getTournament();
        tournamentAuthService.canManageTournament(tournament.getId());

        FootballMatchEvent lastEvent = getLastFootballMatchEvent(matchId);
        if (!lastEvent.getId().equals(event.getId())) {
            throw new ConflictException(ErrorsString.CAN_ONLY_DELETE_LAST_EVENT);
        }

        FootballMatch footballMatch = event.getMatch();
        if (footballMatch.isFinished() || footballMatch.isHalftime() || !footballMatch.isStarted()) {
            throw new ConflictException(ErrorsString.CANNOT_DELETE_EVENT);
        }

        FootballEventType eventType = event.getType();
        Player player = event.getPlayer();
        Team playerTeam = event.getTeam();
        if (player == null || playerTeam == null) {
            throw new ConflictException(ErrorsString.CANNOT_DELETE_EVENT);
        }

        switch (eventType) {
            case YELLOW_CARD, RED_CARD, RED_YELLOW_CARD -> {
                Boolean isYellowCard = eventType == FootballEventType.YELLOW_CARD;
                updatePlayerStat(tournament, player, playerTeam, null, null, isYellowCard ? (short) -1 : null,
                        !isYellowCard ? (short) -1 : null);
            }
            case GOAL, OWN_GOAL, PENALTY, MISSED_PENALTY -> {
                Team homeTeam = footballMatch.getHomeTeam();
                Team awayTeam = footballMatch.getAwayTeam();

                if (event.getModifiedValues() == null) {
                    throw new ConflictException(ErrorsString.CANNOT_DELETE_EVENT);
                }

                Short homeScoreDelta = event.getModifiedValues().getHomeScoreDelta() != null
                        ? (short) -event.getModifiedValues().getHomeScoreDelta()
                        : null;
                Short awayScoreDelta = event.getModifiedValues().getAwayScoreDelta() != null
                        ? (short) -event.getModifiedValues().getAwayScoreDelta()
                        : null;
                Short homePtsDelta = event.getModifiedValues().getHomePtsDelta() != null
                        ? (short) -event.getModifiedValues().getHomePtsDelta()
                        : null;
                Short awayPtsDelta = event.getModifiedValues().getAwayPtsDelta() != null
                        ? (short) -event.getModifiedValues().getAwayPtsDelta()
                        : null;
                Short homeWonDelta = event.getModifiedValues().getHomeWonDelta() != null
                        ? (short) -event.getModifiedValues().getHomeWonDelta()
                        : null;
                Short awayWonDelta = event.getModifiedValues().getAwayWonDelta() != null
                        ? (short) -event.getModifiedValues().getAwayWonDelta()
                        : null;
                Short homeDrawnDelta = event.getModifiedValues().getHomeDrawnDelta() != null
                        ? (short) -event.getModifiedValues().getHomeDrawnDelta()
                        : null;
                Short awayDrawnDelta = event.getModifiedValues().getAwayDrawnDelta() != null
                        ? (short) -event.getModifiedValues().getAwayDrawnDelta()
                        : null;
                Short homeLostDelta = event.getModifiedValues().getHomeLostDelta() != null
                        ? (short) -event.getModifiedValues().getHomeLostDelta()
                        : null;
                Short awayLostDelta = event.getModifiedValues().getAwayLostDelta() != null
                        ? (short) -event.getModifiedValues().getAwayLostDelta()
                        : null;
                Short penaltyScoreHomeDelta = event.getModifiedValues().getPenaltyScoreHomeDelta() != null
                        ? (short) -event.getModifiedValues().getPenaltyScoreHomeDelta()
                        : null;
                Short penaltyScoreAwayDelta = event.getModifiedValues().getPenaltyScoreAwayDelta() != null
                        ? (short) -event.getModifiedValues().getPenaltyScoreAwayDelta()
                        : null;
                Short missedPenaltyHomeDelta = event.getModifiedValues().getMissedPenaltyHomeDelta() != null
                        ? (short) -event.getModifiedValues().getMissedPenaltyHomeDelta()
                        : null;
                Short missedPenaltyAwayDelta = event.getModifiedValues().getMissedPenaltyAwayDelta() != null
                        ? (short) -event.getModifiedValues().getMissedPenaltyAwayDelta()
                        : null;

                Short goalsDelta = event.getType() != FootballEventType.OWN_GOAL
                        && event.getType() != FootballEventType.MISSED_PENALTY
                        && ((awayScoreDelta != null && awayScoreDelta != 0)
                                || (homeScoreDelta != null && homeScoreDelta != 0)) ? (short) -1 : null;

                Boolean isGroupStage = matchService.isMatchInGroupStage(footballMatch.getMatch().getId());

                if (isGroupStage && eventType != FootballEventType.MISSED_PENALTY) {
                    updateResultAndStandings(footballMatch, homeTeam, awayTeam,
                            homeScoreDelta, awayScoreDelta, homePtsDelta, awayPtsDelta, homeWonDelta, awayWonDelta,
                            homeDrawnDelta, awayDrawnDelta, homeLostDelta, awayLostDelta);
                } else {
                    updateResultAndBracketInFinalStage(footballMatch, homeScoreDelta, awayScoreDelta,
                            penaltyScoreHomeDelta,
                            penaltyScoreAwayDelta, missedPenaltyHomeDelta, missedPenaltyAwayDelta);
                }

                if (goalsDelta != null) {
                    updatePlayerStat(tournament, player, playerTeam, goalsDelta, null, null, null);
                }
            }
            default -> throw new BadRequestException(ErrorsString.CANNOT_DELETE_CHRONOLOGICAL_EVENT);
        }

        footballMatchEventRepository.delete(event);
    }

    public FootballMatchEvent getLastFootballMatchEvent(Long matchId) {
        return footballMatchEventRepository.findTopByMatch_IdOrderByIdDesc(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_EVENT_NOT_FOUND));
    }
}