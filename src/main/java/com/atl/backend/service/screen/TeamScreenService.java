package com.atl.backend.service.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PalmaresDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TeamRequestDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.Palmares;
import com.atl.backend.model.Player;
import com.atl.backend.model.Field;
import com.atl.backend.model.Team;
import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AbstractTeamResponse;
import com.atl.backend.response.PayParticipationResponse;
import com.atl.backend.response.PlayerRequestsResponse;
import com.atl.backend.response.PlayersInTeamResponse;
import com.atl.backend.response.TeamDetailsResponse;
import com.atl.backend.response.TeamsResponse;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class TeamScreenService {
    @Autowired
    private TeamService teamService;
    @Autowired
    private TournamentService tournamentService;

    @Transactional(readOnly = true)
    public TeamsResponse getTeamsScreen(SportCategory sportCategory) {
        Map<Integer, Team> teamsCache = teamService.getVisibleTeamsBySportCategory(sportCategory).stream()
                .collect(Collectors.toMap(Team::getId, team -> team));
        Map<Integer, String> captainNamePerTeam = new HashMap<>();
        for (Team team : teamsCache.values()) {
            captainNamePerTeam.put(team.getId(), team.getCaptainPlayer().getAccount().getUsername());
        }
        return TeamsResponse.builder().captainNamePerTeam(captainNamePerTeam).teamsCache(teamsCache.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> TeamDTO.fromTeam(entry.getValue())))).build();
    }

    @Transactional(readOnly = true)
    public AbstractTeamResponse getAbstractTeamScreen(Integer teamId) {
        teamService.checkTeamExists(teamId);

        Team team = teamService.getTeamById(teamId).get();
        List<Tournament> tournaments = new ArrayList<>();
        Map<Integer, Coordinator> coordinatorsCache = new HashMap<>();
        Map<Integer, Field> fieldsCache = new HashMap<>();
        Map<Integer, FootballTeamStat> footballPlayerStatsCache = new HashMap<>();
        Map<Integer, String> cityNamePerField = new HashMap<>();

        List<Palmares> palmares = teamService.getPalmaresByTeamId(teamId);
        for (Palmares palmaresEntry : palmares) {
            Tournament tournament = palmaresEntry.getTournament();
            if (tournament != null) {
                tournaments.add(tournament);
                coordinatorsCache.put(tournament.getId(), tournament.getCoordinator());
                fieldsCache.put(tournament.getId(), tournament.getField());
                cityNamePerField.put(tournament.getField().getId(),
                        tournament.getField() != null ? tournament.getField().getCity().getName() : "N/A");
            }
        }

        PlayersInTeamResponse playersInTeamResponse = teamService.getPlayersInTeamResponse(teamId);
        switch (team.getSportCategory()) {
            case FOOTBALL:
                List<FootballTeamStat> footballTeamStats = teamService.getFootballTeamStatsByTeamId(teamId);
                for (FootballTeamStat stat : footballTeamStats) {
                    if (team.getVip() == false
                            && playersInTeamResponse.getPlayers().stream()
                                    .noneMatch(player -> player.getPlayerId().equals(stat.getPlayer().getPlayerId()))) {
                                        continue; // Skip historical players for non-VIP teams
                    }
                    footballPlayerStatsCache.put(stat.getPlayer().getPlayerId(), stat);
                    }
                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return AbstractTeamResponse.builder().team(TeamDTO.fromTeam(team))
                .palmares(palmares.stream().map(PalmaresDTO::fromPalmares).toList())
                .tournaments(tournaments.stream().map(TournamentDTO::fromTournament).toList())
                .coordinatorsCache(coordinatorsCache.values().stream().map(CoordinatorDTO::fromCoordinator).toList())
                .fieldsCache(fieldsCache.values().stream().map(FieldDTO::fromField).toList())
                .footballPlayerStatsCache(footballPlayerStatsCache.values().stream()
                        .map(FootballTeamStatDTO::fromFootballTeamStat).toList())
                .historicalPlayers(playersInTeamResponse.getHistoricalPlayers())
                .players(playersInTeamResponse.getPlayers())
                .cityNamePerField(cityNamePerField).build();
    }

    @Transactional(readOnly = true)
    public PayParticipationResponse getPayParticipationScreen(Integer teamId) {
        teamService.checkTeamExists(teamId);

        Player player = AccountContext.getActivePlayer();
        if (teamService.isPlayerInTeam(teamId, player.getPlayerId()) == false) {
            throw new UnauthorizedException(ErrorsString.NEED_TO_BE_IN_TEAM);
        }

        List<TournamentParticipation> tournamentParticipations = tournamentService
                .getNotPaidTournamentParticipationsByTeamId(teamId);
        Map<Integer, Tournament> tournamentsCache = new HashMap<>();
        Map<Integer, Boolean> isCalledUpPerTournament = new HashMap<>();
        for (TournamentParticipation tournamentParticipation : tournamentParticipations) {
            Tournament tournament = tournamentParticipation.getTournament();
            tournamentsCache.put(tournament.getId(), tournament);
            isCalledUpPerTournament.put(tournament.getId(),
                    tournamentService.isPlayerCalledUpInTournament(player.getPlayerId(), tournament.getId()));
        }

        return PayParticipationResponse.builder()
                .tournamentsCache(tournamentsCache.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> TournamentDTO.fromTournament(entry.getValue()))))
                .tournamentParticipations(tournamentParticipations.stream()
                        .map(TournamentParticipationDTO::fromTournamentParticipation).toList())
                .isCalledUpPerTournament(isCalledUpPerTournament)
                .build();
    }

    @Transactional(readOnly = true)
    public PlayerRequestsResponse getPlayerRequestsScreen(Integer teamId) {
        teamService.checkTeamExists(teamId);
        Team team = teamService.getTeamById(teamId).get();
        Player activePlayer = AccountContext.getActivePlayer();

        if(!teamService.isPlayerInTeam(teamId, activePlayer.getPlayerId())) {
            throw new UnauthorizedException(ErrorsString.NEED_TO_BE_IN_TEAM);
        }

        if(!team.getViceCaptainPlayers().contains(activePlayer) && !team.getCaptainPlayer().equals(activePlayer)) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN);
        }
        
        List<TeamRequest> teamRequests = teamService.getTeamRequestsByTeamId(teamId);
        Map<Integer, Player> playersCache = new HashMap<>();
        for (TeamRequest teamRequest : teamRequests) {
            Player player = teamRequest.getPlayer();
            playersCache.put(player.getPlayerId(), player);
        }
        return PlayerRequestsResponse.builder()
                .teamRequests(teamRequests.stream().map(TeamRequestDTO::fromTeamRequest).toList())
                .playersCache(playersCache.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> PlayerDTO.fromPlayer(entry.getValue()))))
                .build();
    }

    @Transactional(readOnly = true)
    public TeamDetailsResponse getTeamDetailsScreen(Integer teamId){
        teamService.checkTeamExists(teamId);
        TeamRequest existingRequest = null;
        Boolean isPlayerInTeam = false;
        if(AccountContext.getAccountRole() == AccountRole.PLAYER) {
            Optional<TeamRequest> optionalTeamRequest = teamService.getTeamRequestByTeamIdAndPlayerId(teamId, AccountContext.getActivePlayer().getPlayerId());
            if(optionalTeamRequest.isPresent()) {
                existingRequest = optionalTeamRequest.get();
            }
            isPlayerInTeam = teamService.isPlayerInTeam(teamId, AccountContext.getActivePlayer().getPlayerId());

        }
        AbstractTeamResponse abstractTeamResponse = getAbstractTeamScreen(teamId);
        return TeamDetailsResponse.builder()
                .team(abstractTeamResponse.getTeam())
                .players(abstractTeamResponse.getPlayers())
                .palmares(abstractTeamResponse.getPalmares())
                .tournaments(abstractTeamResponse.getTournaments())
                .coordinatorsCache(abstractTeamResponse.getCoordinatorsCache())
                .fieldsCache(abstractTeamResponse.getFieldsCache())
                .footballPlayerStatsCache(abstractTeamResponse.getFootballPlayerStatsCache())
                .cityNamePerField(abstractTeamResponse.getCityNamePerField())
                .historicalPlayers(abstractTeamResponse.getHistoricalPlayers())
                .isPlayerInTeam(isPlayerInTeam)
                .existingRequest(existingRequest != null ? TeamRequestDTO.fromTeamRequest(existingRequest) : null)
                .build();
    }
}
