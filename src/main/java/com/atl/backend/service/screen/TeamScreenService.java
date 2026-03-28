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

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.PalmaresDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.Palmares;
import com.atl.backend.model.Field;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AbstractTeamResponse;
import com.atl.backend.response.PlayersInTeamResponse;
import com.atl.backend.response.TeamsResponse;
import com.atl.backend.service.TeamService;

@Service
public class TeamScreenService {
    @Autowired
    private TeamService teamService;

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
        Optional<Team> teamOpt = teamService.getTeamById(teamId);
        if (teamOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }

        Team team = teamOpt.get();
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
}
