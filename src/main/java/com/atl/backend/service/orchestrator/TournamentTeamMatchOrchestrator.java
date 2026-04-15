package com.atl.backend.service.orchestrator;

import java.util.List;

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
import com.atl.backend.service.TeamService;
import com.atl.backend.service.matchServices.MatchService;
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

    @Transactional(rollbackFor = Exception.class)
    public void leaveTeam(Integer teamId) {
        Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId,
                AccountContext.getActivePlayer().getPlayerId());
        List<Integer> notPaidTournamentIds = tournamentParticipantService.getNotPaidTournamentIdsByTeamId(teamId);
        teamService.leaveTeam(teamId, activeTournamentsCount, notPaidTournamentIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void expelPlayer(Integer teamId, Integer playerId) {
        Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId, playerId);
        List<Integer> notPaidTournamentIds = tournamentParticipantService.getNotPaidTournamentIdsByTeamId(teamId);
        teamService.expelPlayer(teamId, playerId, activeTournamentsCount, notPaidTournamentIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveAllTeams() {
        Integer playerId = AccountContext.getPlayerId();
        List<PlaysAt> playsAts = teamService.getPlaysAtByPlayerId(playerId);
        for (PlaysAt playsAt : playsAts) {
            leaveTeam(playsAt.getTeam().getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer addMotmToMatch(Integer matchId, Integer playerId) {
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
                teamService.upgradeFootballTeamStat(playerId, playerId, null, (short) 1, null, null);
                tournamentStatService.updateFootballTournamentStats(match.getTournament().getId(), playerId,
                        playerTeam.getId(), null, (short) 1, null, null);
                break;
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return playerId;
    }
}
