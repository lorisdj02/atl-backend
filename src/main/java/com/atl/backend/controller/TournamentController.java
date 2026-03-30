package com.atl.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AllTournamentsResponse;
import com.atl.backend.response.CallupsResponse;
import com.atl.backend.response.TournamentDetailsResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.TournamentScreenService;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    @Autowired
    private TournamentScreenService tournamentScreenService;
    @Autowired
    private SportService sportService;

    @GetMapping("/all-tournaments/{sportCategory}")
    public ResponseEntity<AllTournamentsResponse> getAllTournamentsScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory category = SportCategory.valueOf(sportCategory.toUpperCase());
        AllTournamentsResponse response = tournamentScreenService.getAllTournamentsScreen(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tournamentId}/details")
    public ResponseEntity<TournamentDetailsResponse> getTournamentDetailsScreen(@PathVariable Integer tournamentId) {
        TournamentDetailsResponse response = tournamentScreenService.getTournamentDetailsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tournamentId}/view-callups")
    public ResponseEntity<List<PlayerDTO>> getViewCallupsScreen(@PathVariable Integer tournamentId) {
        List<PlayerDTO> response = tournamentScreenService.getViewCallupsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tournamentId}/callups")
    public ResponseEntity<CallupsResponse> getCallupsScreen(@PathVariable Integer tournamentId) {
        CallupsResponse response = tournamentScreenService.getCallupsScreen(tournamentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tournamentId}")
    public ResponseEntity<?> getTournamentScreen(@PathVariable Integer tournamentId) {
        Optional<?> response = tournamentScreenService.getTournamentScreen(tournamentId);
        return ResponseEntity.ok(response.get());
    }
}
