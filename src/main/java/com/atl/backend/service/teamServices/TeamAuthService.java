package com.atl.backend.service.teamServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;
import com.atl.backend.repository.CallupRepository;
import com.atl.backend.repository.TeamRepository;

@Service
public class TeamAuthService {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private CallupRepository callupRepository;

    public Boolean canManageTeamCaptain(long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        if (AccountContext.isAdmin())
            return true;

        if (AccountContext.isPlayer()) {
            Player player = AccountContext.getActivePlayer();

            if (player.getPlayerId().equals(team.getCaptainPlayer().getPlayerId())) {
                return true;
            }
        }

        return false;
    }

    public void checkManageTeamCaptain(long teamId) {
        if (!canManageTeamCaptain(teamId)) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN);
        }
    }

    public Boolean canManageTeam(long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        if (AccountContext.isAdmin())
            return true;

        if (AccountContext.isPlayer()) {
            Player player = AccountContext.getActivePlayer();

            if (player.getPlayerId().equals(team.getCaptainPlayer().getPlayerId())) {
                return true;
            }
            if (team.getViceCaptainPlayers() != null && team.getViceCaptainPlayers().contains(player)) {
                return true;
            }
        }

        return false;
    }

    public void checkManageTeam(long teamId) {
        if (!canManageTeam(teamId)) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN);
        }
    }

    public Boolean canManageTeamInTournament(Long teamId, Long tournamentId) {
        if (AccountContext.isAdmin())
            return true;

        if (!canManageTeam(teamId)) {
            return false;
        }

        if (AccountContext.isPlayer()) {
            Player player = AccountContext.getActivePlayer();
            List<Callup> callups = callupRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId);
            if (callups.isEmpty()
                    || !callups.stream().anyMatch(c -> c.getPlayer().getPlayerId().equals(player.getPlayerId()))) {
                return false;
            }
            return true;
        }

        return false;
    }

    public void checkManageTeamInTournament(Long teamId, Long tournamentId) {
        if (!canManageTeamInTournament(teamId, tournamentId)) {
            throw new UnauthorizedException(ErrorsString.UNAUTHORIZED);
        }
    }

    public boolean canCreateTeam(TeamDTO teamDTO) {
        if (AccountContext.isAdmin())
            return true;

        if (AccountContext.isPlayer()) {
            Player player = AccountContext.getActivePlayer();

            if (teamDTO.getCaptainPlayerId().equals(player.getPlayerId())) {
                return true;
            }
        }

        return false;
    }

    public void checkCreateTeam(TeamDTO teamDTO) {
        if (!canCreateTeam(teamDTO)) {
            throw new UnauthorizedException(ErrorsString.UNAUTHORIZED);
        }
    }
}
