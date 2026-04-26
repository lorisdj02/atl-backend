package com.atl.backend.service.matchServices;

import com.atl.backend.repository.FinalStageBracketRepository;
import com.atl.backend.repository.FootballMatchEventRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.response.FootballMatchEventsResponse;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentAuthService;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atl.backend.model.enums.*;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.*;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.Match;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;

@Service
public class MatchService {
    @Autowired
    private TournamentAuthService tournamentAuthService;
    @Autowired
    private FootballMatchEventRepository footballMatchEventRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private FootballMatchRepository footballMatchRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private FinalStageBracketRepository finalStageBracketRepository;

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private PlayerRepository playerRepository;

    public void deleteAllMatches() {
        matchRepository.deleteAll();
        footballMatchRepository.deleteAll();
        finalStageBracketRepository.deleteAll();
    }

    public Integer calculateMatchesInTournament(TournamentDTO tournamentDTO) {

        final int numTeams = tournamentDTO.getNumTeams();
        final int numGroups = tournamentDTO.getNumGroups();
        final int numTeamsQualified = tournamentDTO.getNumTeamsQualified();

        final int numTeamsPerGroup = numTeams / numGroups;
        final int round2GroupStage = tournamentDTO.getRound2GroupStage() ? 1 : 0;
        final int round2FinalStage = tournamentDTO.getRound2FinalStage() ? 1 : 0;

        final int matchesInGroupStage = numGroups * (numTeamsPerGroup * (numTeamsPerGroup - 1))
                / (2 - round2GroupStage);
        final int matchesInFinalStage = numTeamsQualified - 1 + round2FinalStage * (numTeamsQualified - 2);
        return matchesInGroupStage + matchesInFinalStage;
    }

    private Tournament getExistingTournamentByTournamentId(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
    }

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public List<FootballMatch> getAllFootballMatches() {
        return footballMatchRepository.findAll();
    }

    public Optional<FootballMatch> getFootballMatchById(Long id) {
        return footballMatchRepository.findById(id);
    }

    public FootballMatch saveFootballMatch(FootballMatch match) {
        return footballMatchRepository.save(match);
    }

    public void deleteFootballMatch(Long id) {
        footballMatchRepository.deleteById(id);
    }

    public List<FootballMatch> getFootballMatchesByTournamentId(Long tournamentId) {
        return footballMatchRepository.findByMatch_Tournament_Id(tournamentId);
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public FinalStageBracket saveFinalStageBracket(FinalStageBracket finalStageBracket) {
        return finalStageBracketRepository.save(finalStageBracket);
    }

    public List<Long> getTeamIdsByTournamentIdAndGroupStage(Long tournamentId, String groupName) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        System.out.println("[DEBUG - MatchService] - Retrieving team IDs for tournament " + tournamentId + " and group "
                + groupName);

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = footballMatchRepository
                        .findByMatch_Tournament_IdAndMatch_GroupName(tournamentId, groupName);
                if (footballMatches.isEmpty()) {
                    throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
                }
                return footballMatches.stream()
                        .flatMap(match -> List.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()).stream())
                        .distinct()
                        .toList();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public List<Team> getTeamsByMatchId(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);
        if (matchOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = matchOptional.get();
        if (match.getSportCategory() == SportCategory.FOOTBALL) {
            Optional<FootballMatch> footballMatchOptional = footballMatchRepository.findByMatch_Id(matchId);
            if (footballMatchOptional.isEmpty()) {
                throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
            }
            FootballMatch footballMatch = footballMatchOptional.get();
            return List.of(footballMatch.getHomeTeam(), footballMatch.getAwayTeam());
        } else {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public List<FootballMatch> getScheduledFootballMatches() {
        return footballMatchRepository.findByStatus(FootballMatchStatus.SCHEDULED);
    }

    public Boolean canMatchFinish(Long matchId) {
        Optional<Match> optionalMatch = matchRepository.findById(matchId);
        if (optionalMatch.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = optionalMatch.get();

        if (match.getStartMatch() == null) {
            return false;
        }

        switch (match.getSportCategory()) {
            case FOOTBALL:
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                if (footballMatch
                        .getStatus() != FootballMatchStatus.START_HALF
                        && footballMatch.getStatus() != FootballMatchStatus.PENALTY_SHOOT_OUT) {
                    return false;
                }

                if (footballMatch.getStatus() == FootballMatchStatus.PENALTY_SHOOT_OUT) {
                    if (footballMatch.getPenaltyScoreHome() == null || footballMatch.getPenaltyScoreAway() == null
                            || footballMatch.getMissedPenaltyHome() == null
                            || footballMatch.getMissedPenaltyAway() == null) {
                        return false;
                    }
                    // Il match può finire se una delle due squadre ha raggiunto un numero di rigori
                    // realizzati che non può più essere raggiunto dall'altra squadra, considerando
                    // anche i rigori mancati
                    final Short totalPenalties = (short) 5;
                    Short totalHomeAttempts = (short) (footballMatch.getPenaltyScoreHome()
                            + footballMatch.getMissedPenaltyHome());
                    Short totalAwayAttempts = (short) (footballMatch.getPenaltyScoreAway()
                            + footballMatch.getMissedPenaltyAway());

                    Integer homeScore = footballMatch.getPenaltyScoreHome().intValue();
                    Integer awayScore = footballMatch.getPenaltyScoreAway().intValue();
                    Integer homeAttempts = totalHomeAttempts.intValue();
                    Integer awayAttempts = totalAwayAttempts.intValue();

                    // Prima dei 5 rigori a testa controlliamo se chi è sotto può ancora
                    // raggiungere.
                    if (homeAttempts < totalPenalties || awayAttempts < totalPenalties) {
                        Integer homeRemaining = Math.max(0, totalPenalties - homeAttempts);
                        Integer awayRemaining = Math.max(0, totalPenalties - awayAttempts);

                        if (homeScore > awayScore && awayScore + awayRemaining >= homeScore) {
                            return false;
                        }
                        if (awayScore > homeScore && homeScore + homeRemaining >= awayScore) {
                            return false;
                        }
                        if (homeScore.equals(awayScore)) {
                            return false;
                        }
                    }

                    // In oltranza si può finire solo dopo lo stesso numero di tiri e punteggio
                    // diverso.
                    if (homeAttempts >= totalPenalties && awayAttempts >= totalPenalties) {
                        if (!homeAttempts.equals(awayAttempts)) {
                            return false;
                        }
                        if (homeScore.equals(awayScore)) {
                            return false;
                        }
                    }
                }

                break;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        if (match.getFinalStageBracket() == null) {
            return true;
        }
        FinalStageBracket finalStageBracket = match.getFinalStageBracket();
        List<Match> matchesInBracket = matchRepository.findByFinalStageBracket_Id(finalStageBracket.getId());
        if (matchesInBracket.size() == 2 && match.getLegOrder() == LegOrderType.FIRST_LEG) {
            // Se è la prima partita di un bracket andata e ritorno, può finire il first leg
            // anche con un pareggio, perché si aspetta il ritorno per decidere chi passa il
            // turno
            return true;
        }

        return !finalStageBracket.isDraw();
    }

    public List<FootballMatchEvent> getFootballMatchEventsByMatchId(Long matchId) {
        Optional<FootballMatch> footballMatchOptional = footballMatchRepository.findByMatch_Id(matchId);
        if (footballMatchOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        FootballMatch footballMatch = footballMatchOptional.get();
        return footballMatchEventRepository.findByMatch_Match_Id(footballMatch.getId());
    }

    @Transactional(readOnly = true)
    public Optional<?> getMatchEvents(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);
        if (matchOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = matchOptional.get();
        switch (match.getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatchEvent> footballMatchEvents = getFootballMatchEventsByMatchId(matchId);
                Boolean canMatchFinish = canMatchFinish(matchId);
                return Optional.of(FootballMatchEventsResponse.builder()
                        .events(footballMatchEvents.stream()
                                .map(event -> FootballMatchEventDTO.fromFootballMatchEvent(event)).toList())
                        .canMatchFinish(canMatchFinish)
                        .build());
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(readOnly = true)
    public Boolean isTeamStillPlayingInTournamentByTeamIdAndTournamentIdAndPhase(Long teamId, Long tournamentId,
            TournamentPhase tournamentPhase) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        Team team = teamService.getTeamById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = getFootballMatchesByTournamentId(tournamentId);
                switch (tournamentPhase) {
                    case FINAL -> {
                        FootballMatch finalMatch = footballMatches.stream()
                                .filter(match -> match.getMatch().getGroupName().equals("FI"))
                                .findFirst()
                                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                        return finalMatch.getHomeTeam().equals(team) || finalMatch.getAwayTeam().equals(team);
                    }
                    case SEMI -> {
                        List<FootballMatch> semiFinalMatches = footballMatches.stream()
                                .filter(match -> match.getMatch().getGroupName().equals("SF1")
                                        || match.getMatch().getGroupName().equals("SF2"))
                                .toList();
                        return semiFinalMatches.stream()
                                .anyMatch(
                                        match -> match.getHomeTeam().equals(team) || match.getAwayTeam().equals(team));
                    }
                    case QUARTER -> {
                        List<FootballMatch> quarterFinalMatches = footballMatches.stream()
                                .filter(match -> match.getMatch().getGroupName().equals("QF1")
                                        || match.getMatch().getGroupName().equals("QF2")
                                        || match.getMatch().getGroupName().equals("QF3")
                                        || match.getMatch().getGroupName().equals("QF4"))
                                .toList();
                        return quarterFinalMatches.stream()
                                .anyMatch(
                                        match -> match.getHomeTeam().equals(team) || match.getAwayTeam().equals(team));
                    }
                    case RO16 -> {
                        List<FootballMatch> roundOf16Matches = footballMatches.stream()
                                .filter(match -> match.getMatch().getGroupName().startsWith("OF"))
                                .toList();
                        return roundOf16Matches.stream()
                                .anyMatch(
                                        match -> match.getHomeTeam().equals(team) || match.getAwayTeam().equals(team));
                    }
                    case RO32 -> {
                        // Non è ancora possibile crearlo, ma vabbè
                        List<FootballMatch> roundOf32Matches = footballMatches.stream()
                                .filter(match -> match.getMatch().getGroupName().startsWith("32"))
                                .toList();
                        return roundOf32Matches.stream()
                                .anyMatch(
                                        match -> match.getHomeTeam().equals(team) || match.getAwayTeam().equals(team));
                    }

                    default -> {
                        return true;
                    }
                }
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);

        }
    }

    @Transactional(readOnly = true)
    public Boolean hasTeamPrizesByTeamIdAndTournamentId(Long teamId, Long tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        Boolean prize1 = !tournament.getPrize1().equals(BigDecimal.ZERO);
        Boolean prize2 = !tournament.getPrize2().equals(BigDecimal.ZERO);

        if (!prize1 && !prize2) {
            return false;
        }

        Team team = teamService.getTeamById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        List<Match> finalMatches = matchRepository.findByTournament_IdAndGroupName(tournamentId, "FI");
        if (finalMatches.isEmpty() || finalMatches.size() > 1) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }

        Match finalMatch = finalMatches.get(0);
        FinalStageBracket finalStageBracket = finalMatch.getFinalStageBracket();
        if (finalStageBracket == null) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Short aggregateScoreHome = finalStageBracket.getAggregateScoreHome();
        Short aggregateScoreAway = finalStageBracket.getAggregateScoreAway();
        if (aggregateScoreHome == null || aggregateScoreAway == null) {
            return false;
        }
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                Optional<FootballMatch> footballMatchOptional = footballMatchRepository
                        .findByMatch_Id(finalMatch.getId());
                if (footballMatchOptional.isEmpty()) {
                    throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                }
                FootballMatch footballMatch = footballMatchOptional.get();
                Boolean teamIsHome = footballMatch.getHomeTeam().equals(team);
                Boolean teamIsAway = footballMatch.getAwayTeam().equals(team);
                if (!teamIsHome && !teamIsAway) {
                    return false;
                }
                if (teamIsHome && aggregateScoreHome > aggregateScoreAway) {
                    return prize1;
                } else if (teamIsAway && aggregateScoreAway > aggregateScoreHome) {
                    return prize1;
                } else {
                    return prize2;
                }
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);

        }
    }

    @Transactional(readOnly = true)
    public Boolean areMatchesFinishedByTournamentIdAndGroupName(Long tournamentId, String prefixGroupName) {
        List<Match> matches = matchRepository.findByTournament_Id(tournamentId);
        if (matches.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        if (prefixGroupName == null) {
            for (Match match : matches) {
                if (match.getGroupName().length() != 1) {
                    continue;
                }
                switch (match.getSportCategory()) {
                    case FOOTBALL -> {
                        Optional<FootballMatch> footballMatchOptional = footballMatchRepository
                                .findByMatch_Id(match.getId());
                        if (footballMatchOptional.isEmpty()) {
                            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                        }
                        FootballMatch footballMatch = footballMatchOptional.get();

                        if (match.getFinishMatch() == null && footballMatch.getHomeTeam() != null
                                && footballMatch.getAwayTeam() != null && match.getMotmPlayer() == null) {
                            return false;
                        }
                    }
                    default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                }
            }
            return true;
        } else {
            matches = matches.stream()
                    .filter(match -> match.getGroupName() != null && match.getGroupName().startsWith(prefixGroupName))
                    .toList();

            for (Match match : matches) {
                if (match.getFinishMatch() == null && match.getMotmPlayer() == null) {
                    return false;
                }
            }
            return true;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public FinalStageBracket updateFinalStageBracket(Long finalStageBracketId, Short aggregateScoreHomeDelta,
            Short aggregateScoreAwayDelta) {
        Optional<FinalStageBracket> finalStageBracketOptional = finalStageBracketRepository
                .findById(finalStageBracketId);
        if (finalStageBracketOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.FINAL_STAGE_BRACKET_NOT_FOUND);
        }
        FinalStageBracket finalStageBracket = finalStageBracketOptional.get();
        if (aggregateScoreHomeDelta != null) {
            Short currentAggregateScoreHome = finalStageBracket.getAggregateScoreHome() != null
                    ? finalStageBracket.getAggregateScoreHome()
                    : 0;
            finalStageBracket.setAggregateScoreHome((short) (currentAggregateScoreHome + aggregateScoreHomeDelta));
        }
        if (aggregateScoreAwayDelta != null) {
            Short currentAggregateScoreAway = finalStageBracket.getAggregateScoreAway() != null
                    ? finalStageBracket.getAggregateScoreAway()
                    : 0;
            finalStageBracket.setAggregateScoreAway((short) (currentAggregateScoreAway + aggregateScoreAwayDelta));
        }
        return saveFinalStageBracket(finalStageBracket);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleFinalStageBracketUpdate(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

        if (match.getFinalStageBracket() == null) {
            return;
        }
        FinalStageBracket finalStageBracket = match.getFinalStageBracket();
        FinalStageBracket nextFinalStageBracket = finalStageBracket.getNextFinalStageBracket();

        if (nextFinalStageBracket == null || match.getGroupName().equals("FI")) {
            return;
        }

        Tournament tournament = match.getTournament();

        switch (match.getSportCategory()) {
            case FOOTBALL:
                FootballMatch footballMatch = getFirstLegFootballMatch(matchId);
                List<FootballMatch> matchesInBracket = footballMatchRepository
                        .findByMatch_FinalStageBracket_Id(nextFinalStageBracket.getId());
                FootballMatch firstLeg = matchesInBracket.stream()
                        .filter(m -> m.getMatch().getLegOrder() == LegOrderType.FIRST_LEG
                                || (tournament.getRound2FinalStage() == false && m.getMatch().getLegOrder() == null)
                                || m.getMatch().getGroupName().equals("FI"))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                FootballMatch secondLeg = getSecondLegFootballMatch(firstLeg.getMatch().getId()).orElse(null);

                if (tournament.getRound2FinalStage() && secondLeg == null
                        && !firstLeg.getMatch().getGroupName().equals("FI")) {
                    throw new NotFoundException(ErrorsString.FINAL_STAGE_BRACKET_NOT_FOUND);
                }

                Boolean willBeHomeTeamInNextBracket = finalStageBracket
                        .getNextFinalStageBracketSlot() == MatchSlotType.HOME;

                if (finalStageBracket.isHomeWinner()) {
                    if (willBeHomeTeamInNextBracket) {
                        firstLeg.setHomeTeam(footballMatch.getHomeTeam());
                        if (secondLeg != null) {
                            secondLeg.setAwayTeam(footballMatch.getHomeTeam());
                        }
                    } else {
                        firstLeg.setAwayTeam(footballMatch.getHomeTeam());
                        if (secondLeg != null) {
                            secondLeg.setHomeTeam(footballMatch.getHomeTeam());
                        }
                    }
                } else if (finalStageBracket.isAwayWinner()) {
                    if (willBeHomeTeamInNextBracket) {
                        firstLeg.setHomeTeam(footballMatch.getAwayTeam());
                        if (secondLeg != null) {
                            secondLeg.setAwayTeam(footballMatch.getAwayTeam());
                        }
                    } else {
                        firstLeg.setAwayTeam(footballMatch.getAwayTeam());
                        if (secondLeg != null) {
                            secondLeg.setHomeTeam(footballMatch.getAwayTeam());
                        }
                    }
                } else {
                    if (willBeHomeTeamInNextBracket) {
                        firstLeg.setHomeTeam(null);
                        if (secondLeg != null) {
                            secondLeg.setAwayTeam(null);
                        }
                    } else {
                        firstLeg.setAwayTeam(null);
                        if (secondLeg != null) {
                            secondLeg.setHomeTeam(null);
                        }
                    }
                }
                saveFootballMatch(firstLeg);
                if (secondLeg != null) {
                    saveFootballMatch(secondLeg);
                }
                break;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

    }

    public FootballMatch updateFootballMatch(Long matchId, Short scoreHomeDelta, Short scoreAwayDelta,
            Short penaltyScoreHomeDelta, Short penaltyScoreAwayDelta, Short missedPenaltyHomeDelta,
            Short missedPenaltyAwayDelta, FootballMatchStatus newStatus) {
        Optional<FootballMatch> footballMatchOptional = footballMatchRepository.findByMatch_Id(matchId);
        if (footballMatchOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        FootballMatch footballMatch = footballMatchOptional.get();
        if (scoreHomeDelta != null) {
            Short currentScoreHome = footballMatch.getScoreHome() != null ? footballMatch.getScoreHome()
                    : 0;
            footballMatch.setScoreHome((short) (currentScoreHome + scoreHomeDelta));
        }
        if (scoreAwayDelta != null) {
            Short currentScoreAway = footballMatch.getScoreAway() != null ? footballMatch.getScoreAway()
                    : 0;
            footballMatch.setScoreAway((short) (currentScoreAway + scoreAwayDelta));
        }
        if (penaltyScoreHomeDelta != null) {
            if (canMatchFinish(matchId)) {
                throw new ConflictException(ErrorsString.PENALTY_SHOOTOUT_FINISHED);
            }
            Short currentPenaltyScoreHome = footballMatch.getPenaltyScoreHome() != null
                    ? footballMatch.getPenaltyScoreHome()
                    : 0;
            footballMatch.setPenaltyScoreHome((short) (currentPenaltyScoreHome + penaltyScoreHomeDelta));
        }
        if (penaltyScoreAwayDelta != null) {
            if (canMatchFinish(matchId)) {
                throw new ConflictException(ErrorsString.PENALTY_SHOOTOUT_FINISHED);
            }
            Short currentPenaltyScoreAway = footballMatch.getPenaltyScoreAway() != null
                    ? footballMatch.getPenaltyScoreAway()
                    : 0;
            footballMatch.setPenaltyScoreAway((short) (currentPenaltyScoreAway + penaltyScoreAwayDelta));
        }
        if (missedPenaltyHomeDelta != null) {
            if (canMatchFinish(matchId)) {
                throw new ConflictException(ErrorsString.PENALTY_SHOOTOUT_FINISHED);
            }
            Short currentMissedPenaltyHome = footballMatch.getMissedPenaltyHome() != null
                    ? footballMatch.getMissedPenaltyHome()
                    : 0;
            footballMatch.setMissedPenaltyHome((short) (currentMissedPenaltyHome + missedPenaltyHomeDelta));
        }
        if (missedPenaltyAwayDelta != null) {
            if (canMatchFinish(matchId)) {
                throw new ConflictException(ErrorsString.PENALTY_SHOOTOUT_FINISHED);
            }
            Short currentMissedPenaltyAway = footballMatch.getMissedPenaltyAway() != null
                    ? footballMatch.getMissedPenaltyAway()
                    : 0;
            footballMatch.setMissedPenaltyAway((short) (currentMissedPenaltyAway + missedPenaltyAwayDelta));
        }

        if (newStatus != null) {
            footballMatch.setStatus(newStatus);
        }

        if (footballMatch.getMatch().getFinalStageBracket() != null) {
            Short bracketScoreHomeDelta = (short) (scoreHomeDelta != null ? scoreHomeDelta
                    : 0 + (short) (penaltyScoreHomeDelta != null ? penaltyScoreHomeDelta : 0));
            Short bracketScoreAwayDelta = (short) (scoreAwayDelta != null ? scoreAwayDelta
                    : 0 + (short) (penaltyScoreAwayDelta != null ? penaltyScoreAwayDelta : 0));
            Short swapDelta = 0;
            if (footballMatch.getMatch().getLegOrder() == LegOrderType.SECOND_LEG) {
                swapDelta = bracketScoreHomeDelta;
                bracketScoreHomeDelta = bracketScoreAwayDelta;
                bracketScoreAwayDelta = swapDelta;
            }

            updateFinalStageBracket(footballMatch.getMatch().getFinalStageBracket().getId(), bracketScoreHomeDelta,
                    bracketScoreAwayDelta);
        }

        footballMatch.setUpdatedAt(LocalDateTime.now());
        return saveFootballMatch(footballMatch);
    }

    public FootballMatch getFirstLegFootballMatch(Long matchId) {
        FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (footballMatch.getMatch().getFinalStageBracket() == null) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }

        Tournament tournament = footballMatch.getMatch().getTournament();

        FinalStageBracket finalStageBracket = footballMatch.getMatch().getFinalStageBracket();
        List<FootballMatch> matchesInBracket = footballMatchRepository
                .findByMatch_FinalStageBracket_Id(finalStageBracket.getId());

        FootballMatch firstLegMatch = matchesInBracket.stream()
                .filter(match -> match.getMatch().getLegOrder() == LegOrderType.FIRST_LEG
                        || (tournament.getRound2FinalStage() == false && match.getMatch().getLegOrder() == null))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        return firstLegMatch;
    }

    public Optional<FootballMatch> getSecondLegFootballMatch(Long matchId) {
        FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (footballMatch.getMatch().getFinalStageBracket() == null) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        FinalStageBracket finalStageBracket = footballMatch.getMatch().getFinalStageBracket();
        List<FootballMatch> matchesInBracket = footballMatchRepository
                .findByMatch_FinalStageBracket_Id(finalStageBracket.getId());

        Optional<FootballMatch> secondLegMatchOptional = matchesInBracket.stream()
                .filter(match -> match.getMatch().getLegOrder() == LegOrderType.SECOND_LEG)
                .findFirst();
        return secondLegMatchOptional;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleTeamRetireInKnockoutPhaseMatch(Long matchId, Long teamId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (match.getFinalStageBracket() == null) {
            throw new ConflictException(ErrorsString.MATCH_NOT_IN_KNOCKOUT_PHASE);
        }

        Tournament tournament = match.getTournament();
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                Boolean isHomeTeamRetiring = footballMatch.getHomeTeam() != null
                        && footballMatch.getHomeTeam().getId().equals(teamId);

                if (match.getLegOrder() == LegOrderType.FIRST_LEG) {
                    updateFootballMatch(matchId, isHomeTeamRetiring ? (short) 0 : 3, isHomeTeamRetiring ? (short) 3 : 0,
                            null, null, null, null,
                            isHomeTeamRetiring ? FootballMatchStatus.HOME_RETIRED : FootballMatchStatus.AWAY_RETIRED);
                } else {
                    // Se siamo nella seconda partita, ritirarsi vuol dire perdere a prescindere,
                    // quindi settiamo il punteggio in modo che l'altra squadra vinca
                    // Con il +3
                    int winnerDelta = Math.abs((int) match.getFinalStageBracket().getAggregateScoreHome()
                            - (int) match.getFinalStageBracket().getAggregateScoreAway()) + 3;
                    updateFootballMatch(matchId, isHomeTeamRetiring ? (short) 0 : (short) winnerDelta,
                            isHomeTeamRetiring ? (short) winnerDelta : (short) 0,
                            null, null, null, null,
                            isHomeTeamRetiring ? FootballMatchStatus.HOME_RETIRED : FootballMatchStatus.AWAY_RETIRED);
                }
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public Long addMotmToMatch(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (match.getMotmPlayer() != null) {
            throw new ConflictException(ErrorsString.MOTM_ALREADY_ASSIGNED);
        }

        tournamentAuthService.canManageTournament(match.getTournament().getId());
        match.setMotmPlayer(playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND)));

        matchRepository.save(match);
        return match.getMotmPlayer().getPlayerId();
    }

    public void startFootballPenaltyShootout(Long matchId) {
        FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (footballMatch.getStatus() != FootballMatchStatus.START_HALF) {
            throw new ConflictException(ErrorsString.CANNOT_START_PENALTY_SHOOTOUT);
        }

        tournamentAuthService.canManageTournament(footballMatch.getMatch().getTournament().getId());

        if (canMatchFinish(matchId)) {
            throw new ConflictException(ErrorsString.CANNOT_START_PENALTY_SHOOTOUT);
        }

        footballMatch.setStatus(FootballMatchStatus.PENALTY_SHOOT_OUT);
        footballMatch.setUpdatedAt(LocalDateTime.now());
        footballMatchRepository.save(footballMatch);
    }

    public Match startMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

        tournamentAuthService.canManageTournament(match.getTournament().getId());
        if (match.getScheduledStart() == null) {
            throw new ConflictException(ErrorsString.MATCH_NOT_SCHEDULED);
        }
        if (match.getStartMatch() != null) {
            throw new ConflictException(ErrorsString.MATCH_ALREADY_STARTED);
        }

        if (LocalDateTime.now().isBefore(match.getScheduledStart())) {
            throw new ConflictException(ErrorsString.CANNOT_START_MATCH_BEFORE_SCHEDULED_TIME);
        }

        if (match.getFinalStageBracket() != null) {
            List<Match> matchesInBracket = match.getFinalStageBracket() != null
                    ? matchRepository.findByFinalStageBracket_Id(match.getFinalStageBracket().getId())
                    : List.of();
            if (matchesInBracket.size() == 2 && match.getLegOrder() == LegOrderType.SECOND_LEG) {
                // Se è la seconda partita di un bracket andata e ritorno, può iniziare solo se
                // è finita la prima partita
                Match firstLegMatch = matchesInBracket.stream()
                        .filter(m -> m.getLegOrder() == LegOrderType.FIRST_LEG)
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                if (firstLegMatch.getFinishMatch() == null) {
                    throw new ConflictException(ErrorsString.CANNOT_START_SECOND_LEG_BEFORE_FIRST_LEG_FINISHED);
                }
            }
        }

        match.setStartMatch(LocalDateTime.now());
        return matchRepository.save(match);
    }

    public FootballMatch startFootballHalftime(Long matchId) {
        FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (footballMatch.getStatus() != FootballMatchStatus.START_HALF) {
            throw new ConflictException(ErrorsString.CANNOT_GO_HALFTIME);
        }

        tournamentAuthService.canManageTournament(footballMatch.getMatch().getTournament().getId());
        footballMatch.setStatus(FootballMatchStatus.HALFTIME);
        footballMatch.setHalftime(LocalDateTime.now());
        footballMatch.setUpdatedAt(LocalDateTime.now());
        return footballMatchRepository.save(footballMatch);
    }

    public FootballMatch startFootballSecondHalf(Long matchId) {
        FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (footballMatch.getStatus() != FootballMatchStatus.HALFTIME) {
            throw new ConflictException(ErrorsString.CANNOT_START_HALF);
        }
        tournamentAuthService.canManageTournament(footballMatch.getMatch().getTournament().getId());
        footballMatch.setStatus(FootballMatchStatus.START_HALF);
        footballMatch.setStartHalf(LocalDateTime.now());
        footballMatch.setUpdatedAt(LocalDateTime.now());
        return footballMatchRepository.save(footballMatch);
    }

    public Match finishMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (!canMatchFinish(matchId)) {
            throw new ConflictException(ErrorsString.CANNOT_FINISH_MATCH);
        }
        tournamentAuthService.canManageTournament(match.getTournament().getId());
        if (match.getFinishMatch() != null) {
            throw new ConflictException(ErrorsString.MATCH_ALREADY_FINISHED);
        }
        match.setFinishMatch(LocalDateTime.now());
        return matchRepository.save(match);
    }

    public Boolean hasUnfinishedMatchesInTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournament_Id(tournamentId);
        if (matches.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        for (Match match : matches) {
            if (match.getFinishMatch() == null) {
                return true;
            }
        }
        return false;
    }

    public Boolean isMatchInGroupStage(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        return match.getGroupName() != null && match.getGroupName().length() == 1;
    }
}
