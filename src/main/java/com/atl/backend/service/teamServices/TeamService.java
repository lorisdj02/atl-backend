package com.atl.backend.service.teamServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
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
import com.atl.backend.service.FieldService;
import com.atl.backend.service.SportService;

import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.repository.FootballTeamStatRepository;
import com.atl.backend.repository.PalmaresRepository;
import com.atl.backend.repository.PlayerRepository;

import com.atl.backend.exception.*;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {
    @Autowired
    private TeamAuthService teamAuthService;
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
    private PalmaresRepository palmaresRepository;
    @Autowired
    private SportService sportService;
    @Autowired
    private FieldService fieldService;

    public List<TeamRequest> getAllTeamRequests() {
        return teamRequestRepository.findAll();
    }

    private Player getExistingPlayerByPlayerId(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    public void deleteAllTeams() {
        playsAtRepository.deleteAll();
        teamRequestRepository.deleteAll();
        teamRepository.deleteAll();
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

    public PlaysAt savePlaysAt(PlaysAt playsAt) {
        return playsAtRepository.save(playsAt);
    }

    public Optional<Team> getTeamByPlayerIdAndSportCategory(Long playerId, SportCategory sportCategory) {
        Optional<PlaysAt> playsAtOptional = playsAtRepository.findByPlayer_PlayerIdAndSportCategory(playerId,
                sportCategory);
        if (playsAtOptional.isPresent()) {
            return Optional.of(playsAtOptional.get().getTeam());
        }
        return Optional.empty();
    }

    public List<Long> getPlayerIdsByTeamId(Long teamId) {
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().map(playsAt -> playsAt.getPlayer().getPlayerId()).toList();
    }

    @Transactional(readOnly = true)
    private PlaysAt canPlayerLeaveTeam(Team team, Long playerId, Integer activeTournamentsCount) {
        Player player = getExistingPlayerByPlayerId(playerId);
        PlaysAt playsAt = playsAtRepository.findByPlayer_PlayerIdAndTeam_Id(player.getPlayerId(),
                team.getId()).orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_IN_TEAM));

        if (activeTournamentsCount > 0) {
            throw new ConflictException(ErrorsString.PLAYER_IS_IN_ACTIVE_TOURNAMENTS);
        }

        if (team.getCaptainPlayer().equals(player) && team.getViceCaptainPlayers().isEmpty()
                && team.getNumPlayers() > 1) {
            throw new ConflictException(ErrorsString.CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN);
        }

        return playsAt;
    }

    @Transactional(rollbackFor = Exception.class)
    private void removePlayer(Long teamId, Player player, Integer activeTournamentsCount) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));
        canPlayerLeaveTeam(team, player.getPlayerId(), activeTournamentsCount);

        PlaysAt playsAt = canPlayerLeaveTeam(team, player.getPlayerId(), activeTournamentsCount);

        team.setNumPlayers((short) (team.getNumPlayers() - 1));

        if (team.getVice1Player() != null && player.equals(team.getCaptainPlayer())) {
            team.setCaptainPlayer(player);
            team.setVice1Player(team.getVice2Player());
            team.setVice2Player(null);
        } else if (team.getVice2Player() != null && player.equals(team.getCaptainPlayer())) {
            team.setCaptainPlayer(team.getVice2Player());
            team.setVice2Player(null);
        }

        team.setUpdatedAt(LocalDateTime.now());

        teamRepository.save(team);
        if (team.getNumPlayers() == 0) {
            softDeleteTeam(teamId);
        }
        playsAtRepository.delete(playsAt);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveTeam(Long teamId, Integer activeTournamentsCount) {
        Player player = AccountContext.getActivePlayer();
        removePlayer(teamId, player, activeTournamentsCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void expelPlayer(Long teamId, Long playerId, Integer activeTournamentsCount) {
        Player player = getExistingPlayerByPlayerId(playerId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        if (team.getCaptainPlayer().equals(player)) {
            throw new ConflictException(ErrorsString.CAPTAIN_CANNOT_BE_EXPELLED);
        }

        teamAuthService.checkManageTeam(teamId);
        if (team.getViceCaptainPlayers().contains(player)
                && !team.getCaptainPlayer().equals(AccountContext.getActivePlayer())) {
            throw new ConflictException(ErrorsString.VICE_CAPTAIN_CANNOT_EXPEL_VICE_CAPTAIN);
        }
        removePlayer(teamId, player, activeTournamentsCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public Optional<PlaysAt> joinTeam(Long playerId, Long teamId) {
        checkTeamExists(teamId);

        Team team = teamRepository.findById(teamId).get();

        Player player = getExistingPlayerByPlayerId(playerId);
        if (isPlayerInATeamBySportCategory(playerId, team.getSportCategory())) {
            throw new ConflictException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
        }

        teamRequestRepository.deleteByPlayer_PlayerId(playerId);
        PlaysAt playsAt = new PlaysAt();

        playsAt.setTeam(team);
        playsAt.setPlayer(player);
        playsAt.setSportCategory(team.getSportCategory());
        if (team.getSportCategory() == SportCategory.FOOTBALL) {
            Optional<FootballTeamStat> footballTeamStatOptional = footballTeamStatRepository
                    .findByTeam_IdAndPlayer_PlayerId(teamId, playerId);
            if (!footballTeamStatOptional.isPresent()) {
                FootballTeamStat footballTeamStat = new FootballTeamStat();
                footballTeamStat.setTeam(team);
                footballTeamStat.setPlayer(player);
                footballTeamStatRepository.save(footballTeamStat);
            }
        }
        if (!team.getCaptainPlayer().equals(player)) {
            team.setNumPlayers((short) (team.getNumPlayers() + 1));
        }
        team.setUpdatedAt(LocalDateTime.now());
        teamRepository.save(team);
        return Optional.of(playsAtRepository.save(playsAt));
    }

    public List<Player> getPlayersByTeamId(Long teamId) {
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

    public List<Palmares> getPalmaresByTeamId(Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        return palmaresRepository.findByTeam_Id(teamId);
    }

    public boolean isPlayerCaptainOrViceCaptain(Long teamId, Long playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND));

        System.out
                .println("[DEBUG] - Checking if player " + playerId + " is captain or vice-captain of team " + teamId);
        System.out.println("[DEBUG] - Team captain: " + team.getCaptainPlayer().getPlayerId());

        return (playerId.equals(team.getCaptainPlayer().getPlayerId()))
                || (team.getViceCaptainPlayers().contains(player));
    }

    @Transactional(readOnly = true)
    public List<FootballTeamStat> getFootballTeamStatsByTeamId(Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        return footballTeamStatRepository.findByTeam_Id(teamId);
    }

    @Transactional(readOnly = true)
    public PlayersInTeamResponse getPlayersInTeamResponse(Long teamId) {
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

    public boolean isPlayerInTeam(Long teamId, Long playerId) {
        checkTeamExists(teamId);
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().anyMatch(playsAt -> playsAt.getPlayer().getPlayerId().equals(playerId));
    }

    public boolean isPlayerInATeamBySportCategory(Long playerId, SportCategory sportCategory) {
        Optional<PlaysAt> playsAtOptional = playsAtRepository.findByPlayer_PlayerIdAndSportCategory(playerId,
                sportCategory);
        return playsAtOptional.isPresent();
    }

    public void checkTeamExists(Long teamId) {
        if (teamRepository.existsById(teamId) == false) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
    }

    public List<TeamRequest> getTeamRequestsByTeamId(Long teamId) {
        return teamRequestRepository.findByTeam_Id(teamId);
    }

    public Optional<TeamRequest> getTeamRequestByTeamIdAndPlayerId(Long teamId, Long playerId) {
        return teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId, playerId);
    }

    public FootballTeamStat getCumulativeFootballTeamStatsByPlayerId(Long playerId) {
        Player player = getExistingPlayerByPlayerId(playerId);
        Team dummyTeam = new Team();
        dummyTeam.setSportCategory(SportCategory.FOOTBALL);
        dummyTeam.setNumPlayers((short) 1);
        dummyTeam.setId(0L);
        dummyTeam.setCaptainPlayer(playerRepository.findById(playerId).get());
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

    public void needToBeInTeam(Long teamId, Long playerId) {
        if (!isPlayerInTeam(teamId, playerId)) {
            throw new ConflictException(ErrorsString.PLAYER_NOT_IN_TEAM);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Team promoteOrDemotePlayerInTeam(Long teamId, Long playerId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();

        Optional<Player> playerOptional = playerRepository.findById(playerId);
        if (playerOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        Player player = playerOptional.get();

        needToBeInTeam(teamId, playerId);

        if (team.getCaptainPlayer() != null && team.getCaptainPlayer().equals(player)) {
            throw new ConflictException(ErrorsString.PLAYER_IS_CAPTAIN);
        } else if (team.getVice1Player() != null && team.getVice1Player().equals(player)) {
            teamAuthService.checkManageTeamCaptain(teamId);
            team.setVice1Player(team.getVice2Player());
            team.setVice2Player(null);
        } else if (team.getVice2Player() != null && team.getVice2Player().equals(player)) {
            teamAuthService.checkManageTeamCaptain(teamId);
            team.setVice2Player(null);
        } else {
            teamAuthService.checkManageTeam(teamId);
            if (team.getVice1Player() == null) {
                team.setVice1Player(player);
            } else if (team.getVice2Player() == null) {
                team.setVice2Player(player);
            } else {
                throw new ConflictException(ErrorsString.TEAM_ALREADY_HAS_VICE_CAPTAINS);
            }
        }
        Team savedTeam = teamRepository.save(team);
        return savedTeam;
    }

    @Transactional(rollbackFor = Exception.class)
    public Team createTeam(TeamDTO teamDTO) {

        teamAuthService.checkCreateTeam(teamDTO);

        if (!sportService.isSupportedSportCategory(teamDTO.getSportCategory())) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        Player captainPlayer = playerRepository.findById(teamDTO.getCaptainPlayerId())
                .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND));

        SportCategory sportCategory = SportCategory.valueOf(teamDTO.getSportCategory().toUpperCase());

        if (isPlayerInATeamBySportCategory(captainPlayer.getPlayerId(), sportCategory)) {
            throw new ConflictException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
        }
        if (teamDTO.getCityId() != null) {
            fieldService.checkCityExists(teamDTO.getCityId());
        }
        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setSportCategory(sportCategory);
        team.setCaptainPlayer(captainPlayer);
        team.setLogoUrl(teamDTO.getLogoUrl());
        team.setCity(teamDTO.getCityId() != null ? fieldService.getCityById(teamDTO.getCityId()).get() : null);
        team.setDescription(teamDTO.getDescription());
        team.setAlias(teamDTO.getAlias());
        team.setVisible(teamDTO.getVisible());
        Team createdTeam = teamRepository.save(team);
        joinTeam(AccountContext.getPlayerId(), createdTeam.getId());
        return createdTeam;
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptTeamRequest(Long teamId, Long playerId, Boolean acceptAll) {
        checkTeamExists(teamId);
        teamAuthService.checkManageTeam(teamId);

        List<TeamRequest> teamRequests = new ArrayList<>();
        if (playerId == null && acceptAll) {
            teamRequests = teamRequestRepository.findByTeam_Id(teamId);
        } else if (playerId != null && !acceptAll) {
            Optional<TeamRequest> teamRequestOptional = teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId,
                    playerId);
            if (teamRequestOptional.isEmpty()) {
                throw new NotFoundException(ErrorsString.TEAM_REQUEST_NOT_FOUND);
            }
            TeamRequest teamRequest = teamRequestOptional.get();
            teamRequests.add(teamRequest);
        } else {
            throw new BadRequestException(ErrorsString.INVALID_TEAM_REQUEST_PARAMETERS);
        }

        for (TeamRequest teamRequest : teamRequests) {
            joinTeam(teamRequest.getPlayer().getPlayerId(), teamId);
        }
        return;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectTeamRequest(Long teamId, Long playerId, Boolean rejectAll) {
        checkTeamExists(teamId);
        teamAuthService.checkManageTeam(teamId);

        List<TeamRequest> teamRequests = new ArrayList<>();
        if (playerId == null && rejectAll) {
            teamRequests = teamRequestRepository.findByTeam_Id(teamId);
        } else if (playerId != null && !rejectAll) {
            Optional<TeamRequest> teamRequestOptional = teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId,
                    playerId);
            if (teamRequestOptional.isEmpty()) {
                throw new NotFoundException(ErrorsString.TEAM_REQUEST_NOT_FOUND);
            }
            TeamRequest teamRequest = teamRequestOptional.get();
            teamRequests.add(teamRequest);
        } else {
            throw new BadRequestException(ErrorsString.INVALID_TEAM_REQUEST_PARAMETERS);
        }

        for (TeamRequest teamRequest : teamRequests) {
            teamRequestRepository.delete(teamRequest);
        }
        return;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTeamRequest(Long teamId) {
        checkTeamExists(teamId);
        Long playerId = AccountContext.getPlayerId();
        Optional<TeamRequest> teamRequestOptional = teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId,
                playerId);
        if (teamRequestOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_REQUEST_NOT_FOUND);
        }
        TeamRequest teamRequest = teamRequestOptional.get();
        teamRequestRepository.delete(teamRequest);
        return;
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamRequest sendTeamRequest(Long teamId) {
        checkTeamExists(teamId);
        Team team = teamRepository.findById(teamId).get();

        Player player = AccountContext.getActivePlayer();
        Long playerId = player.getPlayerId();
        if (isPlayerInATeamBySportCategory(playerId, team.getSportCategory())) {
            throw new ConflictException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
        }
        Optional<TeamRequest> teamRequestOptional = teamRequestRepository.findByTeam_IdAndPlayer_PlayerId(teamId,
                playerId);
        if (teamRequestOptional.isPresent()) {
            throw new ConflictException(ErrorsString.TEAM_REQUEST_ALREADY_SENT);
        }
        TeamRequest teamRequest = new TeamRequest();
        teamRequest.setTeam(team);
        teamRequest.setPlayer(player);
        return teamRequestRepository.save(teamRequest);
    }

    public List<PlaysAt> getPlaysAtByPlayerId(Long playerId) {
        return playsAtRepository.findByPlayer_PlayerId(playerId);
    }

    public FootballTeamStat updateFootballTeamStat(Long playerId, Long teamId, Short goalsDelta, Short motmDelta,
            Short redCardsDelta, Short yellowCardsDelta) {
        Optional<FootballTeamStat> footballTeamStatOptional = footballTeamStatRepository
                .findByTeam_IdAndPlayer_PlayerId(teamId, playerId);
        if (footballTeamStatOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_STAT_NOT_FOUND);
        }

        goalsDelta = (goalsDelta != null) ? goalsDelta : 0;
        motmDelta = (motmDelta != null) ? motmDelta : 0;
        redCardsDelta = (redCardsDelta != null) ? redCardsDelta : 0;
        yellowCardsDelta = (yellowCardsDelta != null) ? yellowCardsDelta : 0;

        FootballTeamStat footballTeamStat = footballTeamStatOptional.get();
        footballTeamStat.setGoals((short) (footballTeamStat.getGoals() + goalsDelta));
        footballTeamStat.setMotm((short) (footballTeamStat.getMotm() + motmDelta));
        footballTeamStat.setRedCards((short) (footballTeamStat.getRedCards() + redCardsDelta));
        footballTeamStat.setYellowCards((short) (footballTeamStat.getYellowCards() + yellowCardsDelta));
        footballTeamStat.setUpdatedAt(LocalDateTime.now());
        return footballTeamStatRepository.save(footballTeamStat);

    }

    public void softDeleteTeam(Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();
        if (team.getNumPlayers() != 0) {
            throw new ConflictException(ErrorsString.CANNOT_DELETE_TEAM_WITH_PLAYERS);
        }
        team.setNumPlayers((short) 0);
        team.setCaptainPlayer(null);
        team.setVisible(false);
        team.setUpdatedAt(LocalDateTime.now());
        teamRepository.save(team);
    }
}
