package com.atl.backend.service;

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

    private Player getExistingPlayerByPlayerId(Integer playerId) {
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

    public List<Integer> getPlayerIdsByTeamId(Integer teamId) {
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().map(playsAt -> playsAt.getPlayer().getPlayerId()).toList();
    }

    @Transactional(readOnly = true)
    private PlaysAt canPlayerLeaveTeam(Team team, Integer playerId, Integer activeTournamentsCount) {
        Player player = getExistingPlayerByPlayerId(playerId);
        PlaysAt playsAt = playsAtRepository.findByPlayer_PlayerIdAndTeam_Id(player.getPlayerId(),
                team.getId()).orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_IN_TEAM));

        if (activeTournamentsCount > 0) {
            throw new ConflictException(ErrorsString.PLAYER_IS_IN_ACTIVE_TOURNAMENTS);
        }

        if (team.getCaptainPlayer().equals(player) && team.getViceCaptainPlayers().isEmpty()) {
            throw new ConflictException(ErrorsString.CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN);
        }

        return playsAt;
    }

    @Transactional(rollbackFor = Exception.class)
    private void removePlayer(Integer teamId, Player player, Integer activeTournamentsCount) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));
        canPlayerLeaveTeam(team, player.getPlayerId(), activeTournamentsCount);

        PlaysAt playsAt = canPlayerLeaveTeam(team, player.getPlayerId(), activeTournamentsCount);

        team.setNumPlayers((short) (team.getNumPlayers() - 1));

        if (team.getVice1Player() != null && team.getCaptainPlayer().equals(player)) {
            team.setCaptainPlayer(player);
            team.setVice1Player(team.getVice2Player());
            team.setVice2Player(null);
        } else if (team.getVice2Player() != null && team.getCaptainPlayer().equals(player)) {
            team.setCaptainPlayer(team.getVice2Player());
            team.setVice2Player(null);
        }

        team.setUpdatedAt(LocalDateTime.now());

        teamRepository.save(team);
        playsAtRepository.delete(playsAt);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveTeam(Integer teamId, Integer activeTournamentsCount) {
        Player player = AccountContext.getActivePlayer();
        removePlayer(teamId, player, activeTournamentsCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void expelPlayer(Integer teamId, Integer playerId, Integer activeTournamentsCount) {
        Player player = getExistingPlayerByPlayerId(playerId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

        if (team.getCaptainPlayer().equals(player)) {
            throw new ConflictException(ErrorsString.CAPTAIN_CANNOT_BE_EXPELLED);
        }

        needToBeCaptainOrViceCaptain(teamId, playerId);
        if (team.getViceCaptainPlayers().contains(player)
                && !team.getCaptainPlayer().equals(AccountContext.getActivePlayer())) {
            throw new ConflictException(ErrorsString.VICE_CAPTAIN_CANNOT_EXPEL_VICE_CAPTAIN);
        }
        removePlayer(teamId, player, activeTournamentsCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public Optional<PlaysAt> joinTeam(Integer playerId, Integer teamId) {
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
        checkTeamExists(teamId);
        List<PlaysAt> playsAts = playsAtRepository.findByTeam_Id(teamId);
        return playsAts.stream().anyMatch(playsAt -> playsAt.getPlayer().getPlayerId().equals(playerId));
    }

    public boolean isPlayerInATeamBySportCategory(Integer playerId, SportCategory sportCategory) {
        Optional<PlaysAt> playsAtOptional = playsAtRepository.findByPlayer_PlayerIdAndSportCategory(playerId,
                sportCategory);
        return playsAtOptional.isPresent();
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
        Player player = getExistingPlayerByPlayerId(playerId);
        Team dummyTeam = new Team();
        dummyTeam.setSportCategory(SportCategory.FOOTBALL);
        dummyTeam.setNumPlayers((short) 1);
        dummyTeam.setId(0);
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

    public void needToBeCaptainOrViceCaptain(Integer teamId, Integer playerId) {
        if (!isPlayerCaptainOrViceCaptain(teamId, playerId)) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN);
        }
    }

    public void needToBeCaptain(Integer teamId, Integer playerId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();
        if (!team.getCaptainPlayer().getPlayerId().equals(playerId)) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN);
        }
    }

    public void needToBeInTeam(Integer teamId, Integer playerId) {
        if (!isPlayerInTeam(teamId, playerId)) {
            throw new ConflictException(ErrorsString.PLAYER_NOT_IN_TEAM);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Team promoteOrDemotePlayerInTeam(Integer teamId, Integer playerId) {
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

        Integer requestingPlayerId = AccountContext.getPlayerId();

        if (team.getCaptainPlayer().equals(player)) {
            throw new ConflictException(ErrorsString.PLAYER_IS_CAPTAIN);
        } else if (team.getVice1Player().equals(player)) {
            needToBeCaptain(teamId, requestingPlayerId);
            team.setVice1Player(team.getVice2Player());
            team.setVice2Player(null);
        } else if (team.getVice2Player().equals(player)) {
            needToBeCaptain(teamId, requestingPlayerId);
            team.setVice2Player(null);
        } else {
            needToBeCaptainOrViceCaptain(teamId, requestingPlayerId);
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
        Player player = AccountContext.getActivePlayer();

        if (!sportService.isSupportedSportCategory(teamDTO.getSportCategory())) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        SportCategory sportCategory = SportCategory.valueOf(teamDTO.getSportCategory().toUpperCase());

        if (isPlayerInATeamBySportCategory(player.getPlayerId(), sportCategory)) {
            throw new ConflictException(ErrorsString.PLAYER_ALREADY_IN_TEAM);
        }
        if (teamDTO.getCityId() != null) {
            fieldService.checkCityExists(teamDTO.getCityId());
        }
        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setSportCategory(sportCategory);
        team.setCaptainPlayer(player);
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
    public void acceptTeamRequest(Integer teamId, Integer playerId, Boolean acceptAll) {
        checkTeamExists(teamId);
        needToBeCaptainOrViceCaptain(teamId, playerId);

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
    public void rejectTeamRequest(Integer teamId, Integer playerId, Boolean rejectAll) {
        checkTeamExists(teamId);
        needToBeCaptainOrViceCaptain(teamId, playerId);

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
    public void cancelTeamRequest(Integer teamId) {
        checkTeamExists(teamId);
        Integer playerId = AccountContext.getPlayerId();
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
    public TeamRequest sendTeamRequest(Integer teamId) {
        checkTeamExists(teamId);
        Team team = teamRepository.findById(teamId).get();

        Player player = AccountContext.getActivePlayer();
        Integer playerId = player.getPlayerId();
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

    public List<PlaysAt> getPlaysAtByPlayerId(Integer playerId) {
        return playsAtRepository.findByPlayer_PlayerId(playerId);
    }

    public FootballTeamStat updateFootballTeamStat(Integer playerId, Integer teamId, Short goalsDelta, Short motmDelta,
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

}
