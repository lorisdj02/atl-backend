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
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ErrorResponse;

import com.atl.backend.service.AccountService;
import com.atl.backend.service.HomeService;
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
    private final HomeService homeService;
    private final AccountService accountService;
    private final SportService sportService;

    public HomeController(HomeService homeService, AccountService accountService, SportService sportService) {
        this.homeService = homeService;
        this.accountService = accountService;
        this.sportService = sportService;
    }

    @GetMapping("/{sportCategory}/player/{playerId}")
    public ResponseEntity<?> getPlayerHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer playerId) {
        try {
            Optional<Player> player = accountService.getPlayerByPlayerId(playerId);
            if (player.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(ErrorsString.PLAYER_NOT_FOUND));
            }

            if (!sportService.isSupportedSportCategory(sportCategory)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(ErrorsString.UNSUPPORTED_SPORT_CATEGORY));
            }

            PlayerHomeResponse response = homeService.getPlayerHomeResponse(playerId,
                    SportCategory.valueOf(sportCategory.toUpperCase()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @GetMapping("/{sportCategory}/coordinator/{coordinatorId}")
    public ResponseEntity<?> getCoordinatorHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer coordinatorId) {
        try {
            Optional<Coordinator> coordinator = accountService.getCoordinatorByCoordinatorId(coordinatorId);
            if (coordinator.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(ErrorsString.COORDINATOR_NOT_FOUND));
            }

            if (!sportService.isSupportedSportCategory(sportCategory)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(ErrorsString.UNSUPPORTED_SPORT_CATEGORY));
            }

            CoordinatorHomeResponse response = homeService.getCoordinatorHomeResponse(coordinatorId,
                    SportCategory.valueOf(sportCategory.toUpperCase()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @GetMapping("/{sportCategory}/referee/{refereeId}")
    public ResponseEntity<?> getRefereeHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer refereeId) {
        try {
            Optional<Referee> referee = accountService.getRefereeByRefereeId(refereeId);
            if (referee.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(ErrorsString.REFEREE_NOT_FOUND));
            }

            if (!sportService.isSupportedSportCategory(sportCategory)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(ErrorsString.UNSUPPORTED_SPORT_CATEGORY));
            }

            RefereeHomeResponse response = homeService.getRefereeHomeResponse(refereeId,
                    SportCategory.valueOf(sportCategory.toUpperCase()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @GetMapping("/{sportCategory}/organization/{organizationId}")
    public ResponseEntity<?> getOrganizationHomeResponse(@PathVariable String sportCategory,
            @PathVariable Integer organizationId) {
        Optional<Organization> organization = accountService.getOrganizationByOrganizationId(organizationId);
        if (organization.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ErrorsString.ORGANIZATION_NOT_FOUND));
        }

        try {
            if (!sportService.isSupportedSportCategory(sportCategory)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(ErrorsString.UNSUPPORTED_SPORT_CATEGORY));
            }

            return ResponseEntity.ok(homeService.getOrganizationHomeResponse(organizationId,
                    SportCategory.valueOf(sportCategory.toUpperCase())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }
}
