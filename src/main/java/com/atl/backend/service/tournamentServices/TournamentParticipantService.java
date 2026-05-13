package com.atl.backend.service.tournamentServices;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.FailedTournamentParticipation;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Match;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.repository.CallupRepository;
import com.atl.backend.repository.FailedTournamentParticipationRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.TournamentParticipationRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.request.ConfirmCallupsRequest;
import com.atl.backend.service.paymentServices.PaymentRefundService;
import com.atl.backend.service.teamServices.TeamAuthService;
import com.atl.backend.service.teamServices.TeamService;

@Service
public class TournamentParticipantService {
    @Autowired
    private TeamAuthService teamAuthService;
    @Autowired
    private TournamentAuthService tournamentAuthService;
    @Autowired
    private TournamentParticipationRepository tournamentParticipationRepository;
    @Autowired
    private CallupRepository callupRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamService teamService;
    @Autowired
    private PaymentRefundService paymentRefundService;
    @Autowired
    private FailedTournamentParticipationRepository failedTournamentParticipationRepository;

    @Autowired
    private FootballMatchRepository footballMatchRepository;
    @Autowired
    private MatchRepository matchRepository;

    private Player getExistingPlayerByPlayerId(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findByPlayerId(playerId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private Player getExistingPlayerByAccountId(Long accountId) {
        Optional<Player> playerOpt = playerRepository.findByAccount_Id(accountId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private Tournament getExistingTournamentByTournamentId(Long tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return tournamentOptional.get();
    }

    public void deleteAllTournamentParticipations() {
        tournamentParticipationRepository.deleteAll();
    }

    public void deleteAllCallups() {
        callupRepository.deleteAll();
    }

    public List<Tournament> getTournamentsByTeamId(Long teamId) {
        List<TournamentParticipation> participations = tournamentParticipationRepository.findByTeam_Id(teamId);
        return participations.stream().map(TournamentParticipation::getTournament).toList();
    }

    public List<TournamentParticipation> getTournamentParticipationsByTeamId(Long teamId) {
        return tournamentParticipationRepository.findByTeam_Id(teamId);
    }

    public List<TournamentParticipation> getTournamentParticipationsByTournamentId(Long tournamentId) {
        return tournamentParticipationRepository.findByTournament_Id(tournamentId);
    }

    public Optional<TournamentParticipation> getTournamentParticipationByTeamIdAndTournamentId(Long teamId,
            Long tournamentId) {
        return tournamentParticipationRepository.findByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    public TournamentParticipation saveTournamentParticipation(TournamentParticipation tournamentParticipation) {
        return tournamentParticipationRepository.save(tournamentParticipation);
    }

    public List<TournamentParticipation> getPaidTournamentParticipationsByTeamId(Long teamId) {
        return tournamentParticipationRepository.findByTeam_IdAndToPay(teamId, BigDecimal.ZERO);
    }

    public List<TournamentParticipation> getNotPaidTournamentParticipationsByTeamId(Long teamId) {
        return tournamentParticipationRepository.findByTeam_IdAndToPayGreaterThan(teamId, BigDecimal.ZERO);
    }

    public List<TournamentParticipation> getTeamParticipations(Long teamId) {
        teamService.checkTeamExists(teamId);
        List<TournamentParticipation> participations = tournamentParticipationRepository.findByTeam_Id(teamId);
        return participations;
    }

    public Integer countTournamentsByPlayerIdAndSportCategory(Long playerId, SportCategory sportCategory) {
        List<Callup> callups = callupRepository.findByPlayer_PlayerId(playerId);
        List<Callup> filteredCallups = callups.stream()
                .filter(callup -> callup.getTournament().getSport().getSportCategory() == sportCategory)
                .toList();
        return filteredCallups.size();
    }

    public boolean isPlayerCalledUpInTournament(Long playerId, Long tournamentId) {
        Optional<Callup> callupOptional = callupRepository.findByPlayer_PlayerIdAndTournament_Id(playerId,
                tournamentId);
        return callupOptional.isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public TournamentParticipation participateToTournament(Long tournamentId, Long teamId,
            List<Long> playerIds) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        if (tournament.getStatus() != TournamentStatus.CREATED) {
            throw new ConflictException(ErrorsString.TOURNAMENT_ALREADY_STARTED);
        }

        if (tournament.getNumTeams() == tournament.getPaidTeams()) {
            throw new ConflictException(ErrorsString.TOURNAMENT_ALREADY_FULL);
        }

        teamService.checkTeamExists(teamId);
        Team team = teamService.getTeamById(teamId).get();

        if (!team.getSportCategory().equals(tournament.getSport().getSportCategory())) {
            throw new ConflictException(ErrorsString.TEAM_SPORT_CATEGORY_MUST_MATCH_TOURNAMENT_SPORT_CATEGORY);
        }

        checkValidCallups(teamId, playerIds);

        System.out.println("[DEBUG - TournamentService] - Participating team " + teamId + " to tournament "
                + tournamentId + " with players " + playerIds);

        Optional<TournamentParticipation> tournamentParticipationOptional = getTournamentParticipationByTeamIdAndTournamentId(
                teamId, tournamentId);

        TournamentParticipation tournamentParticipation = null;
        if (tournamentParticipationOptional.isPresent()) {
            callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
            tournamentParticipation = tournamentParticipationOptional.get();
        } else {
            System.out.println("[DEBUG - TournamentService] - Creating new tournament participation for team " + teamId
                    + " and tournament " + tournamentId);
            tournamentParticipation = new TournamentParticipation();
            tournamentParticipation.setTeam(team);
            tournamentParticipation.setTournament(tournament);
            tournamentParticipation.setToPay(tournament.getFee());
            tournamentParticipationRepository.save(tournamentParticipation);
            System.out.println("[DEBUG - TournamentService] - New tournament participation created for team " + teamId
                    + " and tournament " + tournamentId);
        }
        List<Callup> newCallups = new ArrayList<>();
        for (Long playerId : playerIds) {
            Player player = getExistingPlayerByPlayerId(playerId);
            Callup callup = new Callup();
            callup.setPlayer(player);
            callup.setTournament(tournament);
            callup.setTeam(team);
            callupRepository.save(callup);
            newCallups.add(callup);
        }
        return tournamentParticipation;
    }

    public void checkValidCallups(Long teamId, List<Long> playerIds) {
        teamService.checkTeamExists(teamId);

        Boolean areCallupsValid = false;
        System.out.println("[DEBUG - TournamentService] - Checking callups for team " + teamId + " and playerIds "
                + playerIds);
        for (Long playerId : playerIds) {
            if (!teamService.isPlayerInTeam(teamId, playerId)) {
                throw new ConflictException(ErrorsString.PLAYER_NOT_IN_TEAM);
            }
            areCallupsValid = teamService.isPlayerCaptainOrViceCaptain(teamId, playerId);
            if (areCallupsValid) {
                return;
            }
        }

        throw new ConflictException(ErrorsString.CAPTAIN_OR_ONE_VICE_CAPTAIN_MUST_BE_CALLED_UP);

    }

    public List<Long> getTeamIdsParticipatingInTournament(Long tournamentId) {
        List<TournamentParticipation> participations = tournamentParticipationRepository
                .findByTournament_IdAndToPay(tournamentId, BigDecimal.ZERO);
        return participations.stream().map(tp -> tp.getTeam().getId()).toList();
    }

    public List<Long> getTeamIdsExcludedFromTournament(Long tournamentId) {
        List<TournamentParticipation> participations = tournamentParticipationRepository
                .findByTournament_IdAndToPayGreaterThan(tournamentId, BigDecimal.ZERO);
        return participations.stream().map(tp -> tp.getTeam().getId()).toList();
    }

    public List<Long> getCalluppedPlayerIdsByTournamentIdAndTeamId(Long tournamentId, Long teamId) {
        List<Callup> callups = callupRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public List<Long> getCalluppedPlayerIdsByTournamentId(Long tournamentId) {
        List<Callup> callups = callupRepository.findByTournament_Id(tournamentId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public List<Callup> getCallupsByPlayerId(Long playerId) {
        return callupRepository.findByPlayer_PlayerId(playerId);
    }

    public Optional<Callup> getCallupByPlayerIdAndTournamentId(Long playerId, Long tournamentId) {
        return callupRepository.findByPlayer_PlayerIdAndTournament_Id(playerId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TournamentParticipation confirmCallups(Long tournamentId, ConfirmCallupsRequest request) {
        if (!tournamentId.equals(request.getTournamentId())) {
            throw new BadRequestException(ErrorsString.TOURNAMENT_ID_MISMATCH);
        }
        return participateToTournament(request.getTournamentId(), request.getTeamId(), request.getSelectedPlayerIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallupsCheck(Long tournamentId, Long teamId) {
        // TODO: Non sono sicuro che funzioni... tanto è tutto da testare, fare
        // attenzione
        teamService.checkTeamExists(teamId);
        teamAuthService.checkManageTeamInTournament(teamId, tournamentId);

        getExistingTournamentByTournamentId(tournamentId);
        Optional<TournamentParticipation> tournamentParticipationOptional = tournamentParticipationRepository
                .findByTeam_IdAndTournament_Id(teamId, tournamentId);
        if (!tournamentParticipationOptional.isPresent()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND);
        }
        if (tournamentParticipationOptional.get().getToPay().compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException(ErrorsString.TOURNAMENT_PARTICIPATION_ALREADY_PAID);
        }
    }

    public void deleteCallupsByTeamIdIdAndTournamentId(Long teamId, Long tournamentId) {
        callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTournamentParticipationByTeamIdAndTournamentId(Long teamId, Long tournamentId) {
        callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
        tournamentParticipationRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreseTournamentParticipation_ToPay(Long tournamentId, Long accountId, BigDecimal amount) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));

        Player player = getExistingPlayerByAccountId(accountId);

        Team team = teamService.getTeamByPlayerIdAndSportCategory(player.getPlayerId(),
                tournament.getSport().getSportCategory())
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        TournamentParticipation tournamentParticipation = getTournamentParticipationByTeamIdAndTournamentId(
                team.getId(), tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND));

        if (tournamentParticipation.getToPay().compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException(ErrorsString.TOURNAMENT_PARTICIPATION_ALREADY_PAID);
        }

        if (tournamentParticipation.getToPay().compareTo(amount) < 0) {
            throw new BadRequestException(ErrorsString.PAYMENT_AMOUNT_EXCEEDS_TO_PAY);
        }
        tournamentParticipation.setToPay(tournamentParticipation.getToPay().subtract(amount));
        tournamentParticipation.setUpdatedAt(Instant.now());
        saveTournamentParticipation(tournamentParticipation);
    }

    public List<TournamentParticipationDTO> getNotPaidTeamParticipations(Long teamId) {
        teamService.checkTeamExists(teamId);
        List<TournamentParticipation> participations = tournamentParticipationRepository
                .findByTeam_IdAndToPayGreaterThan(teamId, BigDecimal.ZERO);
        return participations.stream()
                .filter(participation -> participation.getToPay().compareTo(BigDecimal.ZERO) > 0)
                .map(TournamentParticipationDTO::fromTournamentParticipation).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundAllExcludedTeamsInTournament(Tournament tournament) {
        tournamentAuthService.checkManageTournament(tournament.getId());

        List<Player> playersToRefund = new ArrayList<>();
        List<TournamentParticipation> participations = tournamentParticipationRepository
                .findByTournament_IdAndToPayGreaterThan(tournament.getId(), BigDecimal.ZERO);
        List<Long> excludedTeamIds = participations.stream().map(tp -> tp.getTeam().getId()).toList();

        for (Long teamId : excludedTeamIds) {
            List<Player> players = teamService.getPlayersByTeamId(teamId);
            playersToRefund.addAll(players);
        }

        for (Player player : playersToRefund) {
            paymentRefundService.refundTournamentFeeByTournamentIdAndAccountId(tournament.getId(),
                    player.getAccount().getId());
        }

        for (TournamentParticipation participation : participations) {
            callupRepository.deleteByTeam_IdAndTournament_Id(participation.getTeam().getId(), tournament.getId());

            FailedTournamentParticipation failedParticipation = FailedTournamentParticipation
                    .fromTournamentAndTeam(participation.getTournament(), participation.getTeam());
            tournamentParticipationRepository.delete(participation);
            failedTournamentParticipationRepository.save(failedParticipation);
        }
    }

    public List<Long> getNotPaidTournamentIdsByTeamId(Long teamId) {
        teamService.checkTeamExists(teamId);
        List<TournamentParticipation> tournamentParticipations = tournamentParticipationRepository
                .findByTeam_IdAndToPayGreaterThan(teamId, BigDecimal.ZERO);

        if (tournamentParticipations.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return tournamentParticipations.stream()
                .map(t -> t.getTournament().getId()).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateToPayTournamentParticipationByTournamentIdAndTeamId(Long tournamentId, Long teamId,
            BigDecimal toPayDelta) {
        Optional<TournamentParticipation> tournamentParticipationOptional = tournamentParticipationRepository
                .findByTeam_IdAndTournament_Id(teamId, tournamentId);
        if (tournamentParticipationOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND);
        }
        TournamentParticipation tournamentParticipation = tournamentParticipationOptional.get();
        tournamentParticipation.setToPay(tournamentParticipation.getToPay().add(toPayDelta));
        saveTournamentParticipation(tournamentParticipation);
    }

    public BigDecimal getToPayByTournamentIdAndPlayerId(Long tournamentId, Long playerId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        Player player = getExistingPlayerByPlayerId(playerId);

        Team team = teamService.getTeamByPlayerIdAndSportCategory(player.getPlayerId(),
                tournament.getSport().getSportCategory())
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        TournamentParticipation tournamentParticipation = tournamentParticipationRepository
                .findByTeam_IdAndTournament_Id(team.getId(), tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND));

        return tournamentParticipation.getToPay();
    }

    public Optional<Team> getWinningTeamInTournament(Long tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        if (tournament.getStatus() != TournamentStatus.PENDING_PAYMENTS
                && tournament.getStatus() != TournamentStatus.FINISHED) {
            throw new ConflictException(ErrorsString.TOURNAMENT_NOT_FINISHED);
        }

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<Match> finalMatches = matchRepository.findByTournament_IdAndGroupName(tournamentId, "FI");
                if (finalMatches.isEmpty() || finalMatches.size() > 1) {
                    throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                }
                Match finalMatch = finalMatches.get(0);
                FinalStageBracket finalStageBracket = finalMatch.getFinalStageBracket();
                if (finalStageBracket == null) {
                    throw new NotFoundException(ErrorsString.FINAL_STAGE_BRACKET_NOT_FOUND);
                }
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(finalMatch.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

                if (finalStageBracket.isHomeWinner()) {
                    return Optional.of(footballMatch.getHomeTeam());
                } else if (finalStageBracket.isAwayWinner()) {
                    return Optional.of(footballMatch.getAwayTeam());
                } else {
                    return Optional.empty();
                }
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public Optional<Team> getFinalistTeamInTournament(Long tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        if (tournament.getStatus() != TournamentStatus.PENDING_PAYMENTS
                && tournament.getStatus() != TournamentStatus.FINISHED) {
            throw new ConflictException(ErrorsString.TOURNAMENT_NOT_FINISHED);
        }

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<Match> finalMatches = matchRepository.findByTournament_IdAndGroupName(tournamentId, "FI");
                if (finalMatches.isEmpty() || finalMatches.size() > 1) {
                    throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                }
                Match finalMatch = finalMatches.get(0);
                FinalStageBracket finalStageBracket = finalMatch.getFinalStageBracket();
                if (finalStageBracket == null) {
                    throw new NotFoundException(ErrorsString.FINAL_STAGE_BRACKET_NOT_FOUND);
                }
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(finalMatch.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

                if (finalStageBracket.isHomeWinner()) {
                    return Optional.of(footballMatch.getAwayTeam());
                } else if (finalStageBracket.isAwayWinner()) {
                    return Optional.of(footballMatch.getHomeTeam());
                } else {
                    return Optional.empty();
                }
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public Map<TournamentPhase, Set<Team>> getTeamsInEachPhaseOfTournament(Long tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<FootballMatch> footballMatches = footballMatchRepository.findByMatch_Tournament_Id(tournamentId);
                Map<TournamentPhase, Set<Team>> teamsInEachPhase = new HashMap<>();
                for (FootballMatch footballMatch : footballMatches) {
                    Match match = footballMatch.getMatch();
                    TournamentPhase phase = getTournamentPhaseByGroupName(match.getGroupName());
                    teamsInEachPhase.putIfAbsent(phase, new HashSet<>());
                    if (footballMatch.getHomeTeam() != null) {
                        teamsInEachPhase.get(phase).add(footballMatch.getHomeTeam());
                    }
                    if (footballMatch.getAwayTeam() != null) {
                        teamsInEachPhase.get(phase).add(footballMatch.getAwayTeam());
                    }
                }
                return teamsInEachPhase;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    private TournamentPhase getTournamentPhaseByGroupName(String groupName) {
        if (groupName.startsWith("FI")) {
            return TournamentPhase.FINAL;
        } else if (groupName.startsWith("SF")) {
            return TournamentPhase.SEMI;
        } else if (groupName.startsWith("QF")) {
            return TournamentPhase.QUARTER;
        } else if (groupName.startsWith("OF")) {
            return TournamentPhase.RO16;
        } else if (groupName.startsWith("32")) {
            return TournamentPhase.RO32;
        } else {
            return TournamentPhase.GROUP;
        }
    }

    public void changeNumberOfParticipantsForTournament(Long tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        List<TournamentParticipation> participations = getPaidTournamentParticipationsByTeamId(tournamentId);
        tournament.setPaidTeams((short) participations.size());
        tournamentRepository.save(tournament);
    }

}
