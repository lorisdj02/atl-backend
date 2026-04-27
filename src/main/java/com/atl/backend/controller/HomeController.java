package com.atl.backend.controller;

import com.atl.backend.model.enums.SportCategory;

import com.atl.backend.response.CoordinatorHomeResponse;
import com.atl.backend.response.PlayerHomeResponse;
import com.atl.backend.response.RefereeHomeResponse;
import com.atl.backend.response.OrganizationHomeResponse;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.*;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.HomeScreenService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/homes")
public class HomeController {
    @Autowired
    private HomeScreenService homeService;
    @Autowired
    private SportService sportService;

    @GetMapping("/player/{sportCategory}")
    public ResponseEntity<PlayerHomeResponse> getPlayerHomeScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        PlayerHomeResponse response = homeService.getPlayerHomeScreen(
                SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);

    }

    @GetMapping("/coordinator/{sportCategory}")
    public ResponseEntity<CoordinatorHomeResponse> getCoordinatorHomeScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        CoordinatorHomeResponse response = homeService
                .getCoordinatorHomeScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/referee/{sportCategory}")
    public ResponseEntity<RefereeHomeResponse> getRefereeHomeScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        RefereeHomeResponse response = homeService
                .getRefereeHomeScreen(SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);

    }

    @GetMapping("/organization/{sportCategory}")
    public ResponseEntity<OrganizationHomeResponse> getOrganizationHomeScreen(@PathVariable String sportCategory) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        OrganizationHomeResponse response = homeService
                .getOrganizationHomeScreen(SportCategory.valueOf(sportCategory.toUpperCase()));

        return ResponseEntity.ok(response);

    }
}
