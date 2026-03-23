package com.atl.backend.controller;

import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.enums.SportCategory;

import com.atl.backend.response.CoordinatorHomeResponse;
import com.atl.backend.response.PlayerHomeResponse;
import com.atl.backend.response.RefereeHomeResponse;
import com.atl.backend.response.OrganizationHomeResponse;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ErrorResponse;
import com.atl.backend.exception.*;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.HomeScreenService;
import com.atl.backend.service.SportService;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homes")
public class HomeController {
    private final HomeScreenService homeService;
    private final AccountService accountService;
    private final SportService sportService;

    public HomeController(HomeScreenService homeService, AccountService accountService, SportService sportService) {
        this.homeService = homeService;
        this.accountService = accountService;
        this.sportService = sportService;
    }

    @GetMapping("/{sportCategory}/player/{playerId}")
    public ResponseEntity<PlayerHomeResponse> getPlayerHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer playerId) {
        Optional<Player> player = accountService.getPlayerByPlayerId(playerId);
        if (player.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        PlayerHomeResponse response = homeService.getPlayerHomeResponse(playerId,
                SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{sportCategory}/coordinator/{coordinatorId}")
    public ResponseEntity<CoordinatorHomeResponse> getCoordinatorHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer coordinatorId) {
        Optional<Coordinator> coordinator = accountService.getCoordinatorByCoordinatorId(coordinatorId);
        if (coordinator.isEmpty()) {
            throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
        }

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        CoordinatorHomeResponse response = homeService.getCoordinatorHomeResponse(coordinatorId,
                SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sportCategory}/referee/{refereeId}")
    public ResponseEntity<RefereeHomeResponse> getRefereeHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer refereeId) {
        Optional<Referee> referee = accountService.getRefereeByRefereeId(refereeId);
        if (referee.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
        }

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        RefereeHomeResponse response = homeService.getRefereeHomeResponse(refereeId,
                SportCategory.valueOf(sportCategory.toUpperCase()));
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{sportCategory}/organization/{organizationId}")
    public ResponseEntity<OrganizationHomeResponse> getOrganizationHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer organizationId) {
        Optional<Organization> organization = accountService.getOrganizationByOrganizationId(organizationId);
        if (organization.isEmpty()) {
            throw new NotFoundException(ErrorsString.ORGANIZATION_NOT_FOUND);
        }

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        OrganizationHomeResponse response = homeService.getOrganizationHomeResponse(organizationId,
                SportCategory.valueOf(sportCategory.toUpperCase()));

        return ResponseEntity.ok(response);

    }
}
