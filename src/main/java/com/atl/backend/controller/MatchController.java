package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.enums.FootballEventType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.matchServices.FootballMatchEventService;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.orchestrator.ReviewsOrchestrator;
import com.atl.backend.service.orchestrator.TournamentTeamMatchOrchestrator;
import com.atl.backend.service.screen.MatchScreenService;
import com.atl.backend.service.tournamentServices.TournamentService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired
    private ReviewsOrchestrator reviewsOrchestrator;
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
    public ResponseEntity<?> getMatchScreen(@PathVariable Long matchId) {
        Optional<?> response = matchScreenService.getMatchScreen(matchId);
        if (response.isEmpty()) {
            throw new BadRequestException(ErrorsString.MATCH_NOT_FOUND);
        }
        return ResponseEntity.ok(matchScreenService.getMatchScreen(matchId).get());
    }

    @GetMapping("/match/{matchId}/events")
    public ResponseEntity<?> getMatchEvents(@PathVariable Long matchId) {
        Optional<?> response = matchService.getMatchEvents(matchId);
        if (response.isEmpty()) {
            throw new BadRequestException(ErrorsString.MATCH_NOT_FOUND);
        }
        return ResponseEntity.ok(response.get());
    }

    @PatchMapping("/match/{matchId}/retire")
    public ResponseEntity<Long> retireFromMatch(@PathVariable Long matchId) {
        Long response = tournamentService.retireFromMatch(matchId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/match/{matchId}/add-motm/{playerId}")
    public ResponseEntity<Long> addMotmToMatch(@PathVariable Long matchId, @PathVariable Long playerId) {
        Long response = tournamentTeamMatchOrchestrator.addMotmToMatch(matchId, playerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/match/{matchId}/insert-referee-assignment/{refereeRoleName}")
    public ResponseEntity<Void> insertRefereeAssignment(@PathVariable Long matchId,
            @PathVariable String refereeRoleName) {
        matchRefereeService.insertRefereeAssignment(matchId, refereeRoleName);
        return ResponseEntity.ok().build();
    }

    // Qui c'è anche penaltyshootout
    @PostMapping("/match/{matchId}/football-event/insert")
    public ResponseEntity<FootballMatchEventDTO> insertFootballMatchEvent(@PathVariable Long matchId,
            @RequestBody FootballMatchEventDTO footballMatchEventDTO) {
        FootballMatchEvent event = footballMatchEventService.insertFootballMatchEvent(matchId, footballMatchEventDTO);
        if (event.getType() == FootballEventType.START) {
            reviewsOrchestrator.startMatchReviews(matchId);
        } else if (event.getType() == FootballEventType.FINISH) {
            reviewsOrchestrator.finishMatchReviews(matchId);
        }
        return ResponseEntity.ok(FootballMatchEventDTO.fromFootballMatchEvent(event));
    }

    @DeleteMapping("/match/event/{eventId}/delete")
    public ResponseEntity<Void> deleteFootballMatchEvent(@PathVariable Long eventId) {
        footballMatchEventService.deleteFootballMatchEvent(eventId);
        return ResponseEntity.ok().build();
    }
}
