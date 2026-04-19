package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.matchServices.FootballMatchEventService;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.orchestrator.TournamentTeamMatchOrchestrator;
import com.atl.backend.service.screen.MatchScreenService;
import com.atl.backend.service.tournamentServices.TournamentService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired
    private MatchScreenService matchScreenService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private SportService sportService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private FootballMatchEventService footballMatchEventService;
    @Autowired
    private MatchRefereeService matchRefereeService;
    @Autowired
    private TournamentTeamMatchOrchestrator tournamentTeamMatchOrchestrator;


    @GetMapping("/{sportCategory}/referee/available-matches")
    public ResponseEntity<AvailableMatchesResponse> getAvailableMatchesScreen(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        AvailableMatchesResponse response = matchScreenService
                .getAvailableMatchesScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sportCategory}/referee/past-assigned-matches")
    public ResponseEntity<PastAssignedMatchesResponse> getPastAssignedMatchesScreen(
            @PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        PastAssignedMatchesResponse response = matchScreenService
                .getPastAssignedMatchesScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatchScreen(@PathVariable Integer matchId) {
        Optional<?> response = matchScreenService.getMatchScreen(matchId);
        if (response.isEmpty()) {
            throw new BadRequestException(ErrorsString.MATCH_NOT_FOUND);
        }
        return ResponseEntity.ok(matchScreenService.getMatchScreen(matchId).get());
    }

    @GetMapping("/match/{matchId}/events")
    public ResponseEntity<?> getMatchEvents(@PathVariable Integer matchId) {
        Optional<?> response = matchService.getMatchEvents(matchId);
        if (response.isEmpty()) {
            throw new BadRequestException(ErrorsString.MATCH_NOT_FOUND);
        }
        return ResponseEntity.ok(response.get());
    }

    @PatchMapping("/match/{matchId}/retire")
    public ResponseEntity<Integer> retireFromMatch(@PathVariable Integer matchId) {
        Integer response = tournamentService.retireFromMatch(matchId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/match/{matchId}/add-motm/{playerId}")
    public ResponseEntity<Integer> addMotmToMatch(@PathVariable Integer matchId, @PathVariable Integer playerId) {
        Integer response = tournamentTeamMatchOrchestrator.addMotmToMatch(matchId, playerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/match/{matchId}/insert-referee-assignment/{refereeRoleName}")
    public ResponseEntity<Void> insertRefereeAssignment(@PathVariable Integer matchId, @PathVariable String refereeRoleName) {
        matchRefereeService.insertRefereeAssignment(matchId, refereeRoleName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/match/{matchId}/football/start-penalty-shootout")
    public ResponseEntity<Void> startPenaltyShootout(@PathVariable Integer matchId) {
        matchService.startFootballPenaltyShootout(matchId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/match/{matchId}/insert-football-event")
    public ResponseEntity<FootballMatchEventDTO> insertFootballMatchEvent(@PathVariable Integer matchId, @RequestBody FootballMatchEventDTO footballMatchEventDTO) {
        FootballMatchEvent event = footballMatchEventService.insertFootballMatchEvent(matchId, footballMatchEventDTO);
        return ResponseEntity.ok(FootballMatchEventDTO.fromFootballMatchEvent(event));
    }
}
