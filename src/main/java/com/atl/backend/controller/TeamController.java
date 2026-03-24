package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.*;
import com.atl.backend.model.Account;
import com.atl.backend.model.Team;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AbstractTeamResponse;
import com.atl.backend.response.MyGrowthResponse;
import com.atl.backend.response.PlayersInTeamResponse;
import com.atl.backend.response.TeamsResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.screen.ProfileScreenService;
import com.atl.backend.service.screen.TeamScreenService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamScreenService teamScreenService;
    private final SportService sportService;
    private final TeamService teamService;


    public TeamController(TeamScreenService teamScreenService, SportService sportService, TeamService teamService) {
        this.teamScreenService = teamScreenService;
        this.sportService = sportService;
        this.teamService = teamService;
    }

    @GetMapping("/{sportCategory}")
    public ResponseEntity<TeamsResponse> getTeamsScreen(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        TeamsResponse response = teamScreenService.getTeamsScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<PlayersInTeamResponse> getPlayersInTeam(@PathVariable Integer teamId) {
        Optional<Team> team = teamService.getTeamById(teamId);
        if (team.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }

        PlayersInTeamResponse response = teamService.getPlayersInTeamResponse(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/abstract")
    public ResponseEntity<AbstractTeamResponse> getAbstractTeamScreen(@PathVariable Integer teamId) {
        Optional<Team> team = teamService.getTeamById(teamId);
        if (team.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        AbstractTeamResponse response = teamScreenService.getAbstractTeamScreen(teamId);
        return ResponseEntity.ok(response);
    }

}
