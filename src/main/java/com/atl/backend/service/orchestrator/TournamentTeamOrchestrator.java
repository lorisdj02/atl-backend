package com.atl.backend.service.orchestrator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.model.PlaysAt;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class TournamentTeamOrchestrator {
    @Autowired
    TournamentService tournamentService;
    @Autowired
    TeamService teamService;

    @Transactional(rollbackFor = Exception.class)
    public void leaveTeam(Integer teamId) {
        Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId,
                AccountContext.getActivePlayer().getPlayerId());
        teamService.leaveTeam(teamId, activeTournamentsCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveAllTeams() {
        Integer playerId = AccountContext.getPlayerId();
        List<PlaysAt> playsAts = teamService.getPlaysAtByPlayerId(playerId);
        for (PlaysAt playsAt : playsAts) {
            leaveTeam(playsAt.getTeam().getId());
        }
    }
}
