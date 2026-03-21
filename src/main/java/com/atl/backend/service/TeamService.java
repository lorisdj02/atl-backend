package com.atl.backend.service;

import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.Player;
import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.Team;
import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.PlaysAtRepository;
import com.atl.backend.repository.TeamRepository;
import com.atl.backend.repository.TeamRequestRepository;
import com.atl.backend.repository.FootballTeamStatRepository;
import com.atl.backend.repository.PlayerRepository;

import java.util.Optional;
import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamRequestRepository teamRequestRepository;
    private final PlaysAtRepository playsAtRepository;
    private final FootballTeamStatRepository footballTeamStatRepository;
    private final TournamentService tournamentService;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository,
            TeamRequestRepository teamRequestRepository, PlaysAtRepository playsAtRepository,
            FootballTeamStatRepository footballTeamStatRepository, TournamentService tournamentService) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamRequestRepository = teamRequestRepository;
        this.playsAtRepository = playsAtRepository;
        this.footballTeamStatRepository = footballTeamStatRepository;
        this.tournamentService = tournamentService;
    }

    public void deleteAllTeams() {
        playsAtRepository.deleteAll();
        teamRequestRepository.deleteAll();
        teamRepository.deleteAll();
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Integer id) {
        return teamRepository.findById(id);
    }

    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    public void deleteTeam(Integer id) {
        teamRepository.deleteById(id);
    }

    public PlaysAt savePlaysAt(PlaysAt playsAt) {
        return playsAtRepository.save(playsAt);
    }

    public Optional<Team> getTeamByPlayerIdAndSportCategory(Integer playerId, SportCategory sportCategory) {
        Optional<PlaysAt> playsAtOptional = playsAtRepository.findByPlayer_PlayerIdAndSportCategory(playerId,
                sportCategory);
        if (playsAtOptional.isPresent()) {
            return Optional.of(playsAtOptional.get().getTeam());
        }
        return Optional.empty();
    }

    public Team promoteToViceCaptain(Integer teamId, Integer playerId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new IllegalArgumentException(ErrorsString.TEAM_NOT_FOUND);
        }
        Optional<Player> playerOptional = playerRepository.findById(playerId);
        if (playerOptional.isEmpty()) {
            throw new IllegalArgumentException(ErrorsString.PLAYER_NOT_FOUND);
        }

        Team team = teamOptional.get();
        Player player = playerOptional.get();
        if (team.getVice1Player() == null) {
            team.setVice1Player(player);
        } else if (team.getVice2Player() == null) {
            team.setVice2Player(player);
        } else {
            throw new IllegalStateException(ErrorsString.TEAM_ALREADY_HAS_VICE_CAPTAINS);
        }
        return teamRepository.save(team);
    }

    public void leftTeam(Integer teamId, Integer playerId) {
        final Optional<PlaysAt> playsAtOptional = playsAtRepository.findByPlayer_PlayerIdAndTeam_Id(playerId, teamId);
        final Optional<Team> teamOptional = teamRepository.findById(teamId);
        final Optional<Player> playerOptional = playerRepository.findById(playerId);
        if (playsAtOptional.isPresent() && teamOptional.isPresent() && playerOptional.isPresent()) {
            final Team team = teamOptional.get();
            final Player player = playerOptional.get();
            final PlaysAt playsAt = playsAtOptional.get();

            if (team.getCaptainPlayer().getPlayerId().equals(playerId) && team.getVice1Player() == null) {
                throw new IllegalStateException(ErrorsString.CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN);
            } else {

            }

            final Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId,
                    playerId);
            if (activeTournamentsCount > 0) {
                throw new IllegalStateException(ErrorsString.PLAYER_IS_IN_ACTIVE_TOURNAMENTS);
            }

            playsAtRepository.delete(playsAt);
        }

    }

    public Optional<PlaysAt> joinTeam(Integer playerId, Integer teamId) {
        
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            Optional<PlaysAt> existingPlaysAt = playsAtRepository.findByPlayer_PlayerIdAndSportCategory(playerId,
                    team.getSportCategory());
            if (existingPlaysAt.isPresent()) {
                throw new IllegalStateException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
            }

            teamRequestRepository.deleteByPlayer_PlayerId(playerId);
            PlaysAt playsAt = new PlaysAt();
            playsAt.setTeam(team);
            playsAt.setPlayer(playerRepository.findById(playerId).orElse(null));
            playsAt.setSportCategory(team.getSportCategory());
            if (team.getSportCategory() == SportCategory.FOOTBALL) {
                FootballTeamStat footballTeamStat = new FootballTeamStat();
                footballTeamStat.setTeam(team);
                footballTeamStat.setPlayer(playerRepository.findById(playerId).orElse(null));
                footballTeamStat.setGoals(0);
            }
            return Optional.of(playsAtRepository.save(playsAt));
        }
        return Optional.empty();
    }

    public List<Player> getPlayersByTeamId(Integer teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new IllegalArgumentException(ErrorsString.TEAM_NOT_FOUND);
        }
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().map(PlaysAt::getPlayer).toList();
    }
}
