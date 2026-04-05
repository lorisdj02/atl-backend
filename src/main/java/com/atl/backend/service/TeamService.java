package com.atl.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.Palmares;
import com.atl.backend.model.Player;
import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.Team;
import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.PlaysAtRepository;
import com.atl.backend.repository.TeamRepository;
import com.atl.backend.repository.TeamRequestRepository;
import com.atl.backend.response.PlayersInTeamResponse;

import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.repository.FootballTeamStatRepository;
import com.atl.backend.repository.PalmaresRepository;
import com.atl.backend.repository.PlayerRepository;

import com.atl.backend.exception.*;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRequestRepository teamRequestRepository;
    @Autowired
    private PlaysAtRepository playsAtRepository;
    @Autowired
    private FootballTeamStatRepository footballTeamStatRepository;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private PalmaresRepository palmaresRepository;
    @Autowired
    private AccountService accountService;

    public List<TeamRequest> getAllTeamRequests() {
        return teamRequestRepository.findAll();
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
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }

        Team team = teamOptional.get();
        Player player = playerOptional.get();
        if (team.getVice1Player() == null) {
            team.setVice1Player(player);
        } else if (team.getVice2Player() == null) {
            team.setVice2Player(player);
        } else {
            throw new ConflictException(ErrorsString.TEAM_ALREADY_HAS_VICE_CAPTAINS);
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
                throw new ConflictException(ErrorsString.CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN);
            } else {

            }

            final Integer activeTournamentsCount = tournamentService.countActiveTournamentsByTeamIdAndPlayerId(teamId,
                    playerId);
            if (activeTournamentsCount > 0) {
                throw new ConflictException(ErrorsString.PLAYER_IS_IN_ACTIVE_TOURNAMENTS);
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
                throw new ConflictException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
            }
            Optional<Player> playerOptional = playerRepository.findById(playerId);
            if (playerOptional.isEmpty()) {
                throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
            }

            Player player = playerOptional.get();
            teamRequestRepository.deleteByPlayer_PlayerId(playerId);
            PlaysAt playsAt = new PlaysAt();

            playsAt.setTeam(team);
            playsAt.setPlayer(player);
            playsAt.setSportCategory(team.getSportCategory());
            if (team.getSportCategory() == SportCategory.FOOTBALL) {
                FootballTeamStat footballTeamStat = new FootballTeamStat();
                footballTeamStat.setTeam(team);
                footballTeamStat.setPlayer(player);
            }
            if (!team.getCaptainPlayer().getPlayerId().equals(playerId)) {
                team.setNumPlayers((short) (team.getNumPlayers() + 1));
            }
            teamRepository.save(team);
            return Optional.of(playsAtRepository.save(playsAt));
        }
        return Optional.empty();
    }

    public List<Player> getPlayersByTeamId(Integer teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().map(PlaysAt::getPlayer).toList();
    }

    public List<Team> getVisibleTeamsBySportCategory(SportCategory sportCategory) {
        return teamRepository.findBySportCategoryAndVisibleTrue(sportCategory);
    }


    public List<Palmares> getPalmaresByTeamId(Integer teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        return palmaresRepository.findByTeam_Id(teamId);
    }

    public boolean isPlayerCaptainOrViceCaptain(Integer teamId, Integer playerId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }

        Optional<Player> playerOptional = playerRepository.findById(playerId);
        if (playerOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        Player player = playerOptional.get();
        Team team = teamOptional.get();

        return (team.getCaptainPlayer().getPlayerId().equals(playerId))
                || (team.getViceCaptainPlayers().contains(player));
    }

    @Transactional(readOnly = true)
    public List<FootballTeamStat> getFootballTeamStatsByTeamId(Integer teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        return footballTeamStatRepository.findByTeam_Id(teamId);
    }

    @Transactional(readOnly = true)
    public PlayersInTeamResponse getPlayersInTeamResponse(Integer teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        List<Player> players = playsAts.stream().map(PlaysAt::getPlayer).toList();
        List<Player> historicalPlayers = new ArrayList<>();

        if (team.getVip()) {
            switch (team.getSportCategory()) {
                case FOOTBALL:
                    List<FootballTeamStat> footballTeamStats = footballTeamStatRepository.findByTeam_Id(teamId);
                    for (FootballTeamStat stat : footballTeamStats) {
                        if (!players.contains(stat.getPlayer())) {
                            historicalPlayers.add(stat.getPlayer());
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
            }
        }
        return PlayersInTeamResponse.builder().players(players.stream().map(PlayerDTO::fromPlayer).toList())
                .historicalPlayers(historicalPlayers.stream().map(PlayerDTO::fromPlayer).toList()).build();

    }

    public boolean isPlayerInTeam(Integer teamId, Integer playerId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().anyMatch(playsAt -> playsAt.getPlayer().getPlayerId().equals(playerId));
    }

    public void checkTeamExists(Integer teamId) {
        if (teamRepository.existsById(teamId) == false) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
    }

    public List<TeamRequest> getTeamRequestsByTeamId(Integer teamId) {
        return teamRequestRepository.findByTeam_Id(teamId);
    }

    public Optional<TeamRequest> getTeamRequestByTeamIdAndPlayerId(Integer teamId, Integer playerId) {
        return teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId, playerId);
    }

    public FootballTeamStat getCumulativeFootballTeamStatsByPlayerId(Integer playerId) {
        accountService.checkPlayerExists(playerId);
        Team dummyTeam = new Team();
        dummyTeam.setSportCategory(SportCategory.FOOTBALL);
        dummyTeam.setNumPlayers((short) 1);
        dummyTeam.setId(0);
        dummyTeam.setCaptainPlayer(playerRepository.findById(playerId).get());
        Player player = playerRepository.findById(playerId).get();
        FootballTeamStat footballTeamStat = new FootballTeamStat();
        footballTeamStat.setPlayer(player);
        footballTeamStat.setTeam(dummyTeam);

        List<FootballTeamStat> footballTeamStats = footballTeamStatRepository.findByPlayer_PlayerId(playerId);
        if (!footballTeamStats.isEmpty()) {
            short totalGoals = 0;
            short totalMotm = 0;
            short totalRedCards = 0;
            short totalYellowCards = 0;
            for (FootballTeamStat stat : footballTeamStats) {
                totalGoals += stat.getGoals();
                totalMotm += stat.getMotm();
                totalRedCards += stat.getRedCards();
                totalYellowCards += stat.getYellowCards();
            }
            footballTeamStat.setGoals(totalGoals);
            footballTeamStat.setMotm(totalMotm);
            footballTeamStat.setRedCards(totalRedCards);
            footballTeamStat.setYellowCards(totalYellowCards);
        }
        return footballTeamStat;

    }

    public Boolean checkTeamNameInCategoryExists(String teamName, SportCategory sportCategory) {
        return teamRepository.existsByTeamNameAndSportCategory(teamName, sportCategory);
    }
}
