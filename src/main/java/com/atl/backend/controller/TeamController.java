package com.atl.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.dto.TeamRequestDTO;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.exception.*;
import com.atl.backend.model.Team;
import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AbstractTeamResponse;
import com.atl.backend.response.PayParticipationResponse;
import com.atl.backend.response.PlayerRequestsResponse;
import com.atl.backend.response.PlayersInTeamResponse;
import com.atl.backend.response.TeamDetailsResponse;
import com.atl.backend.response.TeamsResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.orchestrator.TournamentTeamMatchOrchestrator;
import com.atl.backend.service.screen.TeamScreenService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private TeamScreenService teamScreenService;
    @Autowired
    private SportService sportService;
    @Autowired
    private TeamService teamService;
    @Autowired
    TournamentTeamMatchOrchestrator tournamentTeamMatchOrchestrator;
    @Autowired
    private TournamentParticipantService tournamentParticipantService;

    @GetMapping("/{sportCategory}")
    public ResponseEntity<TeamsResponse> getTeamsScreen(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        TeamsResponse response = teamScreenService.getTeamsScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/players")
    public ResponseEntity<PlayersInTeamResponse> getPlayersInTeam(@PathVariable Integer teamId) {
        PlayersInTeamResponse response = teamService.getPlayersInTeamResponse(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/abstract")
    public ResponseEntity<AbstractTeamResponse> getAbstractTeamScreen(@PathVariable Integer teamId) {
        AbstractTeamResponse response = teamScreenService.getAbstractTeamScreen(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/pay-participation")
    public ResponseEntity<PayParticipationResponse> getPayParticipationScreen(@PathVariable Integer teamId) {
        PayParticipationResponse response = teamScreenService.getPayParticipationScreen(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/manage/view-player-requests")
    public ResponseEntity<PlayerRequestsResponse> getPlayerRequestsScreen(@PathVariable Integer teamId) {
        PlayerRequestsResponse response = teamScreenService.getPlayerRequestsScreen(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/details")
    public ResponseEntity<TeamDetailsResponse> getTeamDetailScreen(@PathVariable Integer teamId) {
        TeamDetailsResponse response = teamScreenService.getTeamDetailsScreen(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/create/{sportCategory}/check-name/{teamName}")
    public ResponseEntity<Boolean> checkTeamNameInCategoryExists(@PathVariable String sportCategory,
            @PathVariable String teamName) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        Boolean response = teamService.checkTeamNameInCategoryExists(teamName,
                SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}/not-paid-participations")
    public ResponseEntity<List<TournamentParticipationDTO>> getNotPaidTeamParticipations(@PathVariable Integer teamId) {
        List<TournamentParticipationDTO> response = tournamentParticipantService.getNotPaidTeamParticipations(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-by-id/{teamId}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Integer teamId) {
        Optional<Team> response = teamService.getTeamById(teamId);
        if (response.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        return ResponseEntity.ok(TeamDTO.fromTeam(response.get()));
    }

    @PatchMapping("/team/{teamId}/promote-or-demote/{playerId}")
    public ResponseEntity<TeamDTO> promoteOrDemotePlayer(@PathVariable Integer teamId, @PathVariable Integer playerId) {
        Team team = teamService.promoteOrDemotePlayerInTeam(teamId, playerId);
        return ResponseEntity.ok(TeamDTO.fromTeam(team));
    }

    @PostMapping("/create")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO team) {
        Team createdTeam = teamService.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamDTO.fromTeam(createdTeam));
    }

    // Si usa così: /team/1/requests/accept?playerId=2&acceptAll=false
    @PostMapping("/team/{teamId}/manage/requests/accept")
    public ResponseEntity<Void> acceptTeamRequest(@PathVariable Integer teamId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) Boolean acceptAll) {
        Boolean acceptAllValue = (acceptAll != null) ? acceptAll : false;
        teamService.acceptTeamRequest(teamId, playerId, acceptAllValue);
        return ResponseEntity.ok().build();
    }

    // Si usa così: /team/1/requests/reject?playerId=2&rejectAll=false
    @PostMapping("/team/{teamId}/manage/requests/reject")
    public ResponseEntity<Void> rejectTeamRequest(@PathVariable Integer teamId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) Boolean rejectAll) {
        Boolean rejectAllValue = (rejectAll != null) ? rejectAll : false;
        teamService.rejectTeamRequest(teamId, playerId, rejectAllValue);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/team/{teamId}/requests/send")
    public ResponseEntity<TeamRequestDTO> sendTeamRequest(@PathVariable Integer teamId) {
        TeamRequest teamRequest = teamService.sendTeamRequest(teamId);
        return ResponseEntity.ok(TeamRequestDTO.fromTeamRequest(teamRequest));
    }

    @DeleteMapping("/team/{teamId}/requests/cancel")
    public ResponseEntity<Void> cancelTeamRequest(@PathVariable Integer teamId) {
        teamService.cancelTeamRequest(teamId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/team/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(@PathVariable Integer teamId) {
        tournamentTeamMatchOrchestrator.leaveTeam(teamId);
        return ResponseEntity.ok().build();
    }
}
