package com.atl.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.BookFieldResponse;
import com.atl.backend.response.JoinLobbyResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.BookingScreenService;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingScreenService bookingScreenService;
    @Autowired
    private SportService sportService;

    @GetMapping("/{sportCategory}/book-a-field")
    public ResponseEntity<BookFieldResponse> getBookFieldResponse(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return ResponseEntity
                .ok(bookingScreenService.getBookFieldScreen(SportCategory.valueOf(sportCategory.toUpperCase())));
    }

    // Funziona così:
    // /football/1/join-lobby?totalPlayers=4&selectedCityId=1&startTime=2024-07-01T10:00:00&finishTime=2024-07-01T12:00:00
    @GetMapping("/{sportCategory}/join-lobby")
    public ResponseEntity<JoinLobbyResponse> getJoinLobbyResponse(@PathVariable String sportCategory,
            @RequestParam(required = false) Short totalPlayers, @RequestParam(required = false) Integer selectedCityId,
            @RequestParam(required = false) String startTime, @RequestParam(required = false) String finishTime) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (startTime != null) {
                start = LocalDateTime.parse(startTime);
            }
            if (finishTime != null) {
                end = LocalDateTime.parse(finishTime);
            }
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        if (startTime != null && finishTime != null && start.isAfter(end)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        SportCategory sportCat = SportCategory.valueOf(sportCategory.toUpperCase());
        return ResponseEntity.ok(bookingScreenService.getJoinLobbyScreen(sportCat, totalPlayers,
                selectedCityId, start, end));
    }
}
