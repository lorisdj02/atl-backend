package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AllTournamentsResponse;
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
}
