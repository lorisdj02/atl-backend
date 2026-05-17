package com.atl.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.MatchBookingDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.request.BookingRefundRequest;
import com.atl.backend.response.BookFieldResponse;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;
import com.atl.backend.response.JoinLobbyResponse;
import com.atl.backend.service.SportService;
import com.atl.backend.service.bookingServices.BookingService;
import com.atl.backend.service.bookingServices.MatchBookingService;
import com.atl.backend.service.orchestrator.PurchasesOrchestrator;
import com.atl.backend.service.screen.BookingScreenService;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    @Autowired
    private PurchasesOrchestrator purchasesOrchestrator;
    @Autowired
    private BookingScreenService bookingScreenService;
    @Autowired
    private SportService sportService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private MatchBookingService matchBookingService;

    @GetMapping("/book-a-field/{sportCategory}")
    public ResponseEntity<BookFieldResponse> getBookFieldScreen(@PathVariable String sportCategory) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return ResponseEntity
                .ok(bookingScreenService.getBookFieldScreen(SportCategory.valueOf(sportCategory.toUpperCase())));
    }

    // Funziona così:
    // /join-lobby-screen/football/city/1?totalPlayers=4&selectedCityId=1&startTime=2024-07-01T10:00:00&finishTime=2024-07-01T12:00:00
    @GetMapping("/join-lobby-screen/{sportCategory}/city/{selectedCityId}")
    public ResponseEntity<JoinLobbyResponse> getJoinLobbyScreen(@PathVariable String sportCategory,
            @PathVariable Long selectedCityId,
            @RequestParam(required = false) Short totalPlayers,
            @RequestParam(required = false) String startTime, @RequestParam(required = false) String finishTime) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        System.out.println("Received parameters - Sport Category: " + sportCategory + ", Total Players: " + totalPlayers
                + ", Selected City ID: " + selectedCityId + ", Start Time: " + startTime + ", Finish Time: "
                + finishTime);
        Instant start = null;
        Instant end = null;

        try {
            if (startTime != null && !startTime.isEmpty()) {
                start = Instant.parse(startTime);
            }
            if (finishTime != null && !finishTime.isEmpty()) {
                end = Instant.parse(finishTime);
            }
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        if (startTime != null && finishTime != null && start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        SportCategory sportCat = SportCategory.valueOf(sportCategory.toUpperCase());
        return ResponseEntity.ok(bookingScreenService.getJoinLobbyScreen(sportCat, totalPlayers,
                selectedCityId, start, end));
    }

    @GetMapping("/free-fields/{sportCategory}/max-players/{maxPlayers}/city/{cityId}/start-time/{startTime}/finish-time/{finishTime}")
    public ResponseEntity<FieldsAndMaxPlayersPerFieldResponse> getFreeFieldsByPlayerAndCityAndDate(
            @PathVariable String sportCategory,
            @PathVariable Short maxPlayers, @PathVariable Long cityId, @PathVariable String startTime,
            @PathVariable String finishTime) {
        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        Instant start = null;
        Instant end = null;

        try {
            start = Instant.parse(startTime);
            end = Instant.parse(finishTime);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        if (start != null && end != null && start.isAfter(end)) {
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
     * public ResponseEntity<Long> joinLobbyBooking(@PathVariable Long
     * bookingId,
     * 
     * @PathVariable Long playersToPay) {
     * Long participants =
     * bookingService.createLobbyBookingParticipate(bookingId, playersToPay);
     * return ResponseEntity.ok(participants);
     * }
     */

    /*
     * @DeleteMapping("/lobby-booking/{bookingId}/refund")
     * public ResponseEntity<Void> refundLobbyBooking(@PathVariable Long bookingId)
     * {
     * purchasesOrchestrator.refundLobbyBooking(bookingId);
     * return ResponseEntity.noContent().build();
     * }
     */
    @DeleteMapping("/lobby-booking/{bookingId}/leave")
    public ResponseEntity<Void> leaveLobbyBooking(@PathVariable Long bookingId,
            @RequestBody(required = false) BookingRefundRequest bookingRefundRequest) {
        purchasesOrchestrator.leaveLobbyBooking(bookingId, bookingRefundRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/private-booking/{bookingId}/refund")
    public ResponseEntity<Void> refundPrivateBooking(@PathVariable Long bookingId,
            @RequestBody(required = false) BookingRefundRequest bookingRefundRequest) {
        purchasesOrchestrator.refundPrivateBooking(bookingId, bookingRefundRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match-booking/{matchId}/schedule/{scheduledStart}")
    public ResponseEntity<MatchBookingDTO> scheduleMatch(@PathVariable Long matchId,
            @PathVariable String scheduledStart) {
        Instant scheduledStartTime;
        try {
            scheduledStartTime = Instant.parse(scheduledStart);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        MatchBookingDTO matchBookingDTO = matchBookingService.scheduleMatch(matchId, scheduledStartTime);
        return ResponseEntity.ok(matchBookingDTO);
    }
}
