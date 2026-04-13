package com.atl.backend.service.tournamentServices;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.FootballTournamentStats;
import com.atl.backend.model.Player;
import com.atl.backend.model.Tournament;
import com.atl.backend.repository.CallupRepository;
import com.atl.backend.repository.FootballTournamentStatsRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.service.TeamService;

@Service
public class TournamentStatService {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private CallupRepository callupRepository;
    @Autowired
    private FootballTournamentStatsRepository footballTournamentStatsRepository;
    @Autowired
    private TeamService teamService;

    public List<FootballTournamentStats> getFootballTournamentStatsByTournamentId(Integer tournamentId) {
        return footballTournamentStatsRepository.findByTournament_Id(tournamentId);
    }

    public void deleteAllFootballTournamentStats() {
        footballTournamentStatsRepository.deleteAll();
    }

    private Player getExistingPlayerByPlayerId(Integer playerId) {
        Optional<Player> playerOpt = playerRepository.findByPlayerId(playerId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private List<Integer> getCalluppedPlayerIdsByTournamentIdAndTeamId(Integer tournamentId, Integer teamId) {
        List<Callup> callups = callupRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public void generateTournamentStats(Tournament tournament, List<Integer> teamIds) {
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (Integer teamId : teamIds) {
                    List<Integer> playerIds = getCalluppedPlayerIdsByTournamentIdAndTeamId(tournament.getId(), teamId);
                    for (Integer playerId : playerIds) {
                        Player player = getExistingPlayerByPlayerId(playerId);
                        FootballTournamentStats tournamentStats = new FootballTournamentStats();
                        tournamentStats.setTournament(tournament);
                        tournamentStats.setTeam(teamService.getTeamById(teamId).get());
                        tournamentStats.setPlayer(player);
                        footballTournamentStatsRepository.save(tournamentStats);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public void updateFootballTournamentStats(Integer tournamentId, Integer playerId, Integer teamId,
            Short goalsDelta, Short motmDelta, Short redCardsDelta,
            Short yellowCardsDelta) {
        FootballTournamentStats footballTournamentStats = footballTournamentStatsRepository
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
        footballTournamentStatsRepository.save(footballTournamentStats);
    }
}
