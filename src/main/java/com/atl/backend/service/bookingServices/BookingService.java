package com.atl.backend.service.bookingServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.repository.BookingRepository;
import com.atl.backend.repository.PrivateBookingRepository;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;
import com.atl.backend.service.FieldService;
import com.atl.backend.repository.LobbyBookingParticipateRepository;

import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.LobbyBookingParticipate;
import com.atl.backend.model.Field;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PrivateBookingRepository privateBookingRepository;
    @Autowired
    private LobbyBookingParticipateRepository lobbyBookingParticipateRepository;
    @Autowired
    private FieldService fieldService;

    @Autowired
    private LobbyBookingService lobbyBookingService;

    @Autowired
    private PrivateBookingService privateBookingService;

    public Optional<Booking> getBookingByBookingId(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getBookingsByFieldId(Long fieldId) {
        return bookingRepository.findByField_Id(fieldId);
    }

    @Transactional(readOnly = true)
    public FieldsAndMaxPlayersPerFieldResponse getFreeFieldsByMaxPlayersAndCityAndDateAndSportCategory(
            Short maxPlayers, Long cityId, Instant startTime, Instant finishTime,
            SportCategory sportCategory) {
        List<Booking> bookings = bookingRepository
                .findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(cityId, startTime,
                        finishTime);
        List<Field> cityFields = fieldService.getFieldsByCityId(cityId);
        List<Long> bookedFieldIds = bookings.stream().map(b -> b.getField().getId()).toList();

        Map<Long, Short> maxPlayersPerField = new HashMap<>();
        List<Field> freeFields = cityFields.stream().filter(f -> !bookedFieldIds.contains(f.getId())).toList();
        for (Field field : freeFields) {
            maxPlayersPerField.put(field.getId(),
                    fieldService.getMaxPlayersByFieldIdAndSportCategory(field.getId(), sportCategory));
        }

        return FieldsAndMaxPlayersPerFieldResponse.builder()
                .fields(freeFields.stream().map(FieldDTO::fromField).toList())
                .maxPlayersPerField(maxPlayersPerField)
                .build();
    }

    public List<Booking> getBookingsByFieldIdFromNow(Long fieldId) {
        return bookingRepository.findByField_IdAndStartTimeGreaterThanEqual(fieldId, Instant.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public void startBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new ConflictException(ErrorsString.BOOKING_NOT_IN_BOOKED_STATUS);
        }
        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        booking.setStatus(BookingStatus.FINISHED);
        bookingRepository.save(booking);
    }

    public void setBookingRefunded(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        booking.setStatus(BookingStatus.REFUND);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveAllBookings() {
        Long playerId = AccountContext.getPlayerId();
        List<LobbyBooking> lobbyBookings = lobbyBookingService.getLobbyBookingsByPlayerId(playerId);
        for (LobbyBooking lobbyBooking : lobbyBookings) {
            if (lobbyBooking.getOwnerPlayer().getPlayerId() == playerId) {
                lobbyBookingService.refundLobbyBooking(lobbyBooking.getId());
            } else {
                lobbyBookingService.leaveLobbyBooking(lobbyBooking.getId());
            }
        }

        List<PrivateBooking> privateBookings = privateBookingService.getPrivateBookingsByPlayerId(playerId);
        for (PrivateBooking privateBooking : privateBookings) {
            privateBookingService.refundPrivateBooking(privateBooking.getId());
        }
    }

    public Set<Long> getPlayerIdsByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));

        switch (booking.getBookingType()) {
            case PRIVATE:
                PrivateBooking privateBooking = privateBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
                return Set.of(privateBooking.getPlayer().getPlayerId());
            case LOBBY:
                List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                        .findByLobbyBooking_Id(bookingId);
                return participations.stream().map(p -> p.getPlayer().getPlayerId()).collect(Collectors.toSet());
            default:
                throw new BadRequestException(ErrorsString.UNSUPPORTED_BOOKING_TYPE);
        }
    }
}
