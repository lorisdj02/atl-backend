package com.atl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.MatchScreenService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    @Autowired
    private MatchScreenService matchScreenService;
    @Autowired
    private SportService sportService;

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
    public ResponseEntity<PastAssignedMatchesResponse> getPastAssignedMatchesScreen(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        PastAssignedMatchesResponse response = matchScreenService
                .getPastAssignedMatchesScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }
}
