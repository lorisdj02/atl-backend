package com.atl.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AllTournamentsResponse;
import com.atl.backend.response.ApplyTournamentFiltersResponse;
import com.atl.backend.response.CallupsResponse;
import com.atl.backend.response.ScheduleMatchResponse;
import com.atl.backend.response.TournamentDetailsResponse;
import com.atl.backend.response.TournamentParametersResponse;
import com.atl.backend.response.TournamentSummaryResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TournamentService;
import com.atl.backend.service.screen.TournamentScreenService;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    @Autowired
    private TournamentScreenService tournamentScreenService;
    @Autowired
    private SportService sportService;
    @Autowired
    private TournamentService tournamentService;

    @GetMapping("{sportCategory}/all-tournaments")
    public ResponseEntity<AllTournamentsResponse> getAllTournamentsScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory category = SportCategory.valueOf(sportCategory.toUpperCase());
        AllTournamentsResponse response = tournamentScreenService.getAllTournamentsScreen(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}/details")
    public ResponseEntity<TournamentDetailsResponse> getTournamentDetailsScreen(@PathVariable Integer tournamentId) {
        TournamentDetailsResponse response = tournamentScreenService.getTournamentDetailsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}/view-callups")
    public ResponseEntity<List<PlayerDTO>> getViewCallupsScreen(@PathVariable Integer tournamentId) {
        List<PlayerDTO> response = tournamentScreenService.getViewCallupsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}/callups")
    public ResponseEntity<CallupsResponse> getCallupsScreen(@PathVariable Integer tournamentId) {
        CallupsResponse response = tournamentScreenService.getCallupsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> getTournamentScreen(@PathVariable Integer tournamentId) {
        Optional<?> response = tournamentScreenService.getTournamentScreen(tournamentId);
        return ResponseEntity.ok(response.get());
    }

    // Funziona così:
    // /football/filters?cityId=1&numTeams=4&minFee=10&maxFee=50&minPrize=100&maxPrize=500&name=summer
    @GetMapping("/{sportCategory}/filters")
    public ResponseEntity<ApplyTournamentFiltersResponse> applyTournamentFilters(
            @PathVariable String sportCategory,
            @RequestParam(required = false) Integer cityId,
            @RequestParam(required = false) Short minTeams,
            @RequestParam(required = false) Short maxTeams,
            @RequestParam(required = false) BigDecimal minFee,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) BigDecimal minPrize,
            @RequestParam(required = false) BigDecimal maxPrize,
            @RequestParam(required = false) String name) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory category = SportCategory.valueOf(sportCategory.toUpperCase());
        ApplyTournamentFiltersResponse response = tournamentService.applyTournamentFilters(category, cityId,
                minTeams, maxTeams, minFee, maxFee,
                minPrize, maxPrize, name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate-tournament-parameters")
    public ResponseEntity<TournamentParametersResponse> calculateTournamentParameters(
            @RequestBody TournamentDTO tournamentDTO) {
        TournamentParametersResponse response = tournamentService.getTournamentParameters(tournamentDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/{tournamentId}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable Integer tournamentId) {
        Optional<Tournament> response = tournamentService.getTournamentById(tournamentId);
        if (response.isEmpty()) {
            throw new BadRequestException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return ResponseEntity.ok(TournamentDTO.fromTournament(response.get()));
    }

    @GetMapping("/tournament/{tournamentId}/schedule-data")
    public ResponseEntity<ScheduleMatchResponse> getScheduleData(@PathVariable Integer tournamentId) {
        ScheduleMatchResponse response = tournamentService.getScheduleData(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}/summary")
    public ResponseEntity<TournamentSummaryResponse> getTournamentSummary(@PathVariable Integer tournamentId) {
        TournamentSummaryResponse response = tournamentScreenService.getTournamentSummary(tournamentId);
        return ResponseEntity.ok(response);
    }
}
