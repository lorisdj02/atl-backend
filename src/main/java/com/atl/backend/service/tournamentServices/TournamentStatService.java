package com.atl.backend.service.tournamentServices;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.FootballTournamentStat;
import com.atl.backend.model.Player;
import com.atl.backend.model.Tournament;
import com.atl.backend.repository.CallupRepository;
import com.atl.backend.repository.FootballTournamentStatRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.service.teamServices.TeamService;

@Service
public class TournamentStatService {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private CallupRepository callupRepository;
    @Autowired
    private FootballTournamentStatRepository footballTournamentStatRepository;
    @Autowired
    private TeamService teamService;

    public List<FootballTournamentStat> getFootballTournamentStatsByTournamentId(Long tournamentId) {
        return footballTournamentStatRepository.findByTournament_Id(tournamentId);
    }

    public void deleteAllFootballTournamentStats() {
        footballTournamentStatRepository.deleteAll();
    }

    private Player getExistingPlayerByPlayerId(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findByPlayerId(playerId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private List<Long> getCalluppedPlayerIdsByTournamentIdAndTeamId(Long tournamentId, Long teamId) {
        List<Callup> callups = callupRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public void generateTournamentStats(Tournament tournament, List<Long> teamIds) {
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (Long teamId : teamIds) {
                    List<Long> playerIds = getCalluppedPlayerIdsByTournamentIdAndTeamId(tournament.getId(), teamId);
                    for (Long playerId : playerIds) {
                        Player player = getExistingPlayerByPlayerId(playerId);
                        FootballTournamentStat tournamentStats = new FootballTournamentStat();
                        tournamentStats.setTournament(tournament);
                        tournamentStats.setTeam(teamService.getTeamById(teamId).get());
                        tournamentStats.setPlayer(player);
                        footballTournamentStatRepository.save(tournamentStats);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public void updateFootballTournamentStats(Long tournamentId, Long playerId, Long teamId,
            Short goalsDelta, Short motmDelta, Short redCardsDelta,
            Short yellowCardsDelta) {
        FootballTournamentStat footballTournamentStats = footballTournamentStatRepository
                .findByTournament_IdAndPlayer_PlayerIdAndTeam_Id(tournamentId, playerId, teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_STAT_NOT_FOUND));

        goalsDelta = goalsDelta != null ? goalsDelta : (short) 0;
        motmDelta = motmDelta != null ? motmDelta : (short) 0;
        redCardsDelta = redCardsDelta != null ? redCardsDelta : (short) 0;
        yellowCardsDelta = yellowCardsDelta != null ? yellowCardsDelta : (short) 0;

        footballTournamentStats.setGoals((short) (footballTournamentStats.getGoals() + goalsDelta));
        footballTournamentStats.setMotm((short) (footballTournamentStats.getMotm() + motmDelta));
        footballTournamentStats.setRedCards((short) (footballTournamentStats.getRedCards() + redCardsDelta));
        footballTournamentStats.setYellowCards((short) (footballTournamentStats.getYellowCards() + yellowCardsDelta));
        footballTournamentStats.setUpdatedAt(Instant.now());
        footballTournamentStatRepository.save(footballTournamentStats);
    }
}
