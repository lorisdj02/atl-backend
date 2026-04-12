package com.atl.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.BookFieldResponse;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;
import com.atl.backend.response.JoinLobbyResponse;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.BookingScreenService;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingScreenService bookingScreenService;
    @Autowired
    private SportService sportService;
    @Autowired
    private BookingService bookingService;

    @GetMapping("/book-a-field/{sportCategory}")
    public ResponseEntity<BookFieldResponse> getBookFieldResponse(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return ResponseEntity
                .ok(bookingScreenService.getBookFieldScreen(SportCategory.valueOf(sportCategory.toUpperCase())));
    }

    // Funziona così:
    // /football/1/join-lobby?totalPlayers=4&selectedCityId=1&startTime=2024-07-01T10:00:00&finishTime=2024-07-01T12:00:00
    @GetMapping("/join-lobby-screen/{sportCategory}")
    public ResponseEntity<JoinLobbyResponse> getJoinLobbyScreen(@PathVariable String sportCategory,
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

    @GetMapping("/free-fields/{sportCategory}/max-players/{maxPlayers}/city/{cityId}/startDate/{startTime}/finishDate/{finishTime}")
    public ResponseEntity<FieldsAndMaxPlayersPerFieldResponse> getFreeFieldsByPlayerAndCityAndDate(
            @PathVariable String sportCategory,
            @PathVariable Integer maxPlayers, @PathVariable Integer cityId, @PathVariable String startTime,
            @PathVariable String finishTime) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            start = LocalDateTime.parse(startTime);
            end = LocalDateTime.parse(finishTime);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        if (start.isAfter(end)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        SportCategory sportCat = SportCategory.valueOf(sportCategory.toUpperCase());
        return ResponseEntity
                .ok(bookingService.getFreeFieldsByMaxPlayersAndCityAndDateAndSportCategory(maxPlayers, cityId, start,
                        end, sportCat));
    }

    /*
     * TODO: Gli endpoint non sono effettivi, dato che passa tutto tramite
     * CheckountSession
     * 
     * @PostMapping("/private-booking/create")
     * public ResponseEntity<PrivateBookingDTO> createPrivateBooking(@RequestBody
     * PrivateBookingDTO privateBookingDTO) {
     * PrivateBooking createdBooking =
     * bookingService.createPrivateBooking(privateBookingDTO);
     * return
     * ResponseEntity.ok(PrivateBookingDTO.fromPrivateBooking(createdBooking));
     * }
     * 
     * @PostMapping("/lobby-booking/create")
     * public ResponseEntity<LobbyBookingDTO> createLobbyBooking(@RequestBody
     * LobbyBookingDTO lobbyBookingDTO) {
     * LobbyBooking createdBooking =
     * bookingService.createLobbyBooking(lobbyBookingDTO);
     * return ResponseEntity.ok(LobbyBookingDTO.fromLobbyBooking(createdBooking));
     * }
     * 
     * @PostMapping("/lobby-booking/{bookingId}/join/playersToPay/{playersToPay}")
     * public ResponseEntity<Integer> joinLobbyBooking(@PathVariable Integer
     * bookingId,
     * 
     * @PathVariable Integer playersToPay) {
     * Integer participants =
     * bookingService.createLobbyBookingParticipate(bookingId, playersToPay);
     * return ResponseEntity.ok(participants);
     * }
     */

    @DeleteMapping("/lobby-booking/{bookingId}/refund")
    public ResponseEntity<Void> refundLobbyBooking(@PathVariable Integer bookingId) {
        bookingService.refundLobbyBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lobby-booking/{bookingId}/leave")
    public ResponseEntity<Void> leaveLobbyBooking(@PathVariable Integer bookingId) {
        bookingService.leaveLobbyBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/private-booking/{bookingId}/refund")
    public ResponseEntity<Void> refundPrivateBooking(@PathVariable Integer bookingId) {
        bookingService.refundPrivateBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
