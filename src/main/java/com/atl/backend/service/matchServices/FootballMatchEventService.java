package com.atl.backend.service.matchServices;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;

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
import com.atl.backend.service.tournamentServices.TournamentStatService;

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
                FootballEventType.RED_CARD);
    }

    public FootballMatchEvent insertFootballMatchEvent(Integer matchId, FootballMatchEventDTO footballMatchEventDTO) {
        FootballMatch match = footballMatchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        FootballEventType eventType;
        try {
            eventType = FootballEventType.valueOf(footballMatchEventDTO.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorsString.INVALID_FOOTBALL_EVENT_TYPE);
        }

        Tournament tournament = match.getMatch().getTournament();
        if (!tournament.getCoordinator().getCoordinatorId().equals(AccountContext.getCoordinatorId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        Boolean isMatchStarted = match.getMatch().getStartMatch() != null;
        Boolean isMatchFinished = match.getMatch().getFinishMatch() != null;
        Boolean isHalftime = match.getHalftime() != null;

        Boolean isFirstHalf = match.getStartHalf() == null && isMatchStarted;
        Boolean isSecondHalf = match.getStartHalf() != null;

        Boolean isPenaltyShootOut = match.getStatus() == FootballMatchStatus.PENALTY_SHOOT_OUT;

        FootballMatchEvent event = new FootballMatchEvent();

        Player player = footballMatchEventDTO.getPlayerId() != null
                ? playerService.getPlayerByPlayerId(footballMatchEventDTO.getPlayerId())
                        .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND))
                : null;

        Team playerTeam = footballMatchEventDTO.getPlayerId() != null
                ? getPlayerTeam(footballMatchEventDTO.getPlayerId(), tournament.getId())
                : null;

        Boolean isHomeTeam = playerTeam != null && match.getHomeTeam().getId().equals(playerTeam.getId());

        // Non so se il calcolo è corretto, da testare
        Integer minute = Math.min(tournament.getSport().getMinutesPerRound(), isFirstHalf
                ? LocalDateTime.now().getMinute() - match.getMatch().getStartMatch().getMinute()
                : LocalDateTime.now().getMinute() - match.getStartHalf().getMinute()) * (isSecondHalf ? 2 : 1);

        Integer actualMinutesDuration = LocalDateTime.now().getMinute()
                - (isFirstHalf ? match.getMatch().getStartMatch().getMinute()
                        : match.getStartHalf().getMinute());

        Short additionalMinute = (short) Math.max(0,
                actualMinutesDuration - tournament.getSport().getMinutesPerRound());

        FootballMatch firstLegMatch = matchService.getFirstLegFootballMatch(match.getMatch().getId());
        Boolean isHomeTeamFirstLeg = firstLegMatch != null
                && firstLegMatch.getHomeTeam().getId().equals(playerTeam.getId());

        event.setMatch(match);
        event.setType(eventType);

        switch (eventType) {
            case START:
                matchService.startMatch(matchId);
                minute = 0;
                additionalMinute = 0;
                break;

            case HALFTIME:
                matchService.startFootballHalftime(matchId);
                break;

            case START_HALF:
                matchService.startFootballSecondHalf(matchId);
                minute = Integer.valueOf(tournament.getSport().getMinutesPerRound());
                additionalMinute = 0;
                break;

            case PENALTY_SHOOT_OUT:
                matchService.startFootballPenaltyShootout(matchId);
                break;

            case FINISH:
                matchService.finishMatch(matchId);
                break;

            case YELLOW_CARD, RED_CARD, RED_YELLOW_CARD:

                if (!isMatchStarted || isMatchFinished || isHalftime) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                if (hasPlayerReceivedRedCard(matchId, footballMatchEventDTO.getPlayerId())) {
                    throw new BadRequestException(ErrorsString.PLAYER_HAS_RED_CARD);
                }
                if (hasPlayerReceivedYellowCard(matchId, footballMatchEventDTO.getPlayerId())
                        && eventType == FootballEventType.YELLOW_CARD) {
                    eventType = FootballEventType.RED_YELLOW_CARD;
                }
                Boolean isYellowCard = eventType == FootballEventType.YELLOW_CARD;
                Boolean isRedCard = eventType == FootballEventType.RED_CARD
                        || eventType == FootballEventType.RED_YELLOW_CARD;

                if (player == null || playerTeam == null) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                tournamentStatService.updateFootballTournamentStats(tournament.getId(), player.getPlayerId(),
                        playerTeam.getId(), (short) 0, (short) 0, (short) (isRedCard ? 0 : 1),
                        (short) (isYellowCard ? 1 : 0));

                teamService.updateFootballTeamStat(player.getPlayerId(),
                        playerTeam.getId(), (short) 0, (short) 0, (short) (isRedCard ? 0 : 1),
                        (short) (isYellowCard ? 1 : 0));
                break;

            case GOAL, OWN_GOAL:

                if (!isMatchStarted || isMatchFinished) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                if (player == null || playerTeam == null) {
                    throw new BadRequestException(ErrorsString.CANNOT_ADD_EVENT_TO_MATCH);
                }

                if (matchService.isMatchInGroupStage(matchId)) {
                    handleGoalInGroupStage();
                } else {
                    handleGoalInFinalStage(match, eventType, player, playerTeam, isHomeTeam, isHomeTeamFirstLeg);
                }

                break;
            case PENALTY:
                if (matchService.isMatchInGroupStage(matchId)) {
                    handleGoalInGroupStage();
                } else {
                    if (!isPenaltyShootOut) {
                        handleGoalInFinalStage(match, eventType, player, playerTeam, isHomeTeam, isHomeTeamFirstLeg);
                    } else {
                        matchService.updateFootballMatch(matchId, null,
                                null, isHomeTeam ? (short) 1 : (short) 0, isHomeTeam ? (short) 0 : (short) 1, null,
                                null, null);

                        matchService.updateFinalStageBracket(match.getMatch().getFinalStageBracket().getId(),
                                isHomeTeamFirstLeg ? (short) 1 : (short) 0, isHomeTeamFirstLeg ? (short) 0 : (short) 1);
                    }
                }

                break;
            case MISSED_PENALTY:
                if (isPenaltyShootOut) {
                    matchService.updateFootballMatch(matchId, null,
                            null, null, null, isHomeTeam ? (short) 1 : (short) 0, isHomeTeam ? (short) 0 : (short) 1,
                            null);
                }
                break;

        }
        event.setPlayer(player);
        event.setTeam(playerTeam);
        event.setMinute(minute.shortValue());
        event.setAdditionalMinute(additionalMinute);
        return footballMatchEventRepository.save(event);
    }

    private void handleGoalInGroupStage() {

    }

    private void handleGoalInFinalStage(FootballMatch footballMatch, FootballEventType eventType, Player player,
            Team playerTeam, Boolean isHomeTeam, Boolean isHomeTeamFirstLeg) {
        matchService.updateFootballMatch(footballMatch.getId(), isHomeTeam ? (short) 1 : (short) 0,
                isHomeTeam ? (short) 0 : (short) 1, null, null, null,
                null, null);

        matchService.updateFinalStageBracket(footballMatch.getMatch().getFinalStageBracket().getId(),
                isHomeTeamFirstLeg ? (short) 1 : (short) 0, isHomeTeamFirstLeg ? (short) 0 : (short) 1);

        tournamentStatService.updateFootballTournamentStats(footballMatch.getMatch().getTournament().getId(),
                player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);

        teamService.updateFootballTeamStat(player.getPlayerId(),
                playerTeam.getId(), (short) 1, (short) 0, (short) 0,
                (short) 0);

    }
}