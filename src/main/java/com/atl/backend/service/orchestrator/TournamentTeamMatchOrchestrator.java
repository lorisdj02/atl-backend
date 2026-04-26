package com.atl.backend.service.orchestrator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Match;
import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.Team;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.paymentServices.PaymentRefundService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;
import com.atl.backend.service.tournamentServices.TournamentStatService;

@Service
public class TournamentTeamMatchOrchestrator {
    @Autowired
    TournamentService tournamentService;
    @Autowired
    TeamService teamService;
    @Autowired
    MatchService matchService;
    @Autowired
    TournamentParticipantService tournamentParticipantService;
    @Autowired
    TournamentStatService tournamentStatService;
    @Autowired
    PaymentRefundService paymentRefundService;

    @Transactional(rollbackFor = Exception.class)
    public void leaveTeam(Long teamId) {
        Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId,
                AccountContext.getActivePlayer().getPlayerId());
        List<Long> notPaidTournamentIds = tournamentParticipantService.getNotPaidTournamentIdsByTeamId(teamId);
        teamService.leaveTeam(teamId, activeTournamentsCount);

        Map<Long, BigDecimal> refundPerTournament = new HashMap<>();
        for (Long tournamentId : notPaidTournamentIds) {
            BigDecimal refundAmount = paymentRefundService.refundTournamentFeeByTournamentIdAndAccountId(tournamentId,
                    AccountContext.getActivePlayer().getAccount().getId());
            refundPerTournament.put(tournamentId, refundAmount);
        }

        for (Map.Entry<Long, BigDecimal> entry : refundPerTournament.entrySet()) {
            tournamentParticipantService.updateToPayTournamentParticipationByTournamentIdAndTeamId(entry.getKey(),
                    teamId, entry.getValue().negate());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void expelPlayer(Long teamId, Long playerId) {
        Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId, playerId);
        teamService.expelPlayer(teamId, playerId, activeTournamentsCount);

        List<Long> notPaidTournamentIds = tournamentParticipantService.getNotPaidTournamentIdsByTeamId(teamId);
        Map<Long, BigDecimal> refundPerTournament = new HashMap<>();
        for (Long tournamentId : notPaidTournamentIds) {
            BigDecimal refundAmount = paymentRefundService.refundTournamentFeeByTournamentIdAndAccountId(tournamentId,
                    AccountContext.getActivePlayer().getAccount().getId());
            refundPerTournament.put(tournamentId, refundAmount);
        }

        for (Map.Entry<Long, BigDecimal> entry : refundPerTournament.entrySet()) {
            tournamentParticipantService.updateToPayTournamentParticipationByTournamentIdAndTeamId(entry.getKey(),
                    teamId, entry.getValue().negate());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveAllTeams() {
        Long playerId = AccountContext.getPlayerId();
        List<PlaysAt> playsAts = teamService.getPlaysAtByPlayerId(playerId);
        for (PlaysAt playsAt : playsAts) {
            leaveTeam(playsAt.getTeam().getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Long addMotmToMatch(Long matchId, Long playerId) {
        Match match = matchService.getMatchById(matchId)
                .orElseThrow(() -> new BadRequestException(ErrorsString.MATCH_NOT_FOUND));

        if (!tournamentParticipantService.isPlayerCalledUpInTournament(playerId, match.getTournament().getId())) {
            throw new BadRequestException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT);
        }

        switch (match.getSportCategory()) {
            case FOOTBALL:

                Team playerTeam = teamService.getTeamByPlayerIdAndSportCategory(playerId, SportCategory.FOOTBALL)
                        .orElseThrow(() -> new BadRequestException(ErrorsString.PLAYER_NOT_IN_TEAM));

                matchService.addMotmToMatch(matchId, playerId);
                teamService.updateFootballTeamStat(playerId, playerId, null, (short) 1, null, null);
                tournamentStatService.updateFootballTournamentStats(match.getTournament().getId(), playerId,
                        playerTeam.getId(), null, (short) 1, null, null);
                break;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return playerId;
    }
}
