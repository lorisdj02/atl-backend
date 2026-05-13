package com.atl.backend.service.bookingServices;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.Player;
import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.BookingType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.PrivateBookingRepository;

@Service
public class PrivateBookingService {

    @Autowired
    private BookingUtilitiesService bookingUtilitiesService;

    @Autowired
    private PrivateBookingRepository privateBookingRepository;

    public List<PrivateBooking> getPrivateBookingsByPlayerId(Long playerId) {
        return privateBookingRepository.findByPlayer_PlayerId(playerId);
    }

    public List<PrivateBooking> getActivePrivateBookingsByPlayerId(Long playerId) {
        List<PrivateBooking> privateBookings = privateBookingRepository.findByPlayer_PlayerId(playerId);
        List<PrivateBooking> activePrivateBookings = new ArrayList<>();
        for (PrivateBooking booking : privateBookings) {
            if (booking.getBooking().getStatus() != BookingStatus.CANCELED
                    && booking.getBooking().getStatus() != BookingStatus.FINISHED
                    && booking.getBooking().getStatus() != BookingStatus.REFUNDED) {
                activePrivateBookings.add(booking);
            }
        }
        return activePrivateBookings;
    }

    public List<PrivateBooking> getPrivateBookingsByFieldId(Long fieldId) {
        return privateBookingRepository.findByBooking_Field_Id(fieldId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PrivateBooking createPrivateBooking(PrivateBookingDTO privateBookingDTO) {
        if (!privateBookingDTO.getPlayerId().equals(AccountContext.getPlayerId())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        Booking booking = bookingUtilitiesService.createBooking(privateBookingDTO.getFieldId(),
                privateBookingDTO.getStartTime(),
                privateBookingDTO.getFinishTime(), privateBookingDTO.getSportCategory(), BookingType.PRIVATE);
        PrivateBooking privateBooking = new PrivateBooking();
        privateBooking.setBooking(booking);
        privateBooking.setPlayer(AccountContext.getActivePlayer());

        BigDecimal cost = booking.getField().calculatePrivateCost(booking.getStartTime(), booking.getFinishTime());
        privateBooking.setCost(cost);
        PrivateBooking savedPrivateBooking = privateBookingRepository.save(privateBooking);
        return savedPrivateBooking;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBookingFromCheckoutPayload(CheckoutPayload checkoutPayload) {

        PrivateBookingDTO privateBookingDTO = new PrivateBookingDTO();
        privateBookingDTO.setFieldId(checkoutPayload.getFieldId());
        privateBookingDTO.setStartTime(checkoutPayload.getStartTime());
        privateBookingDTO.setFinishTime(checkoutPayload.getFinishTime());
        privateBookingDTO.setSportCategory(checkoutPayload.getSportCategory());
        privateBookingDTO.setPlayerId(checkoutPayload.getOwnerPlayerId());

        PrivateBooking newPrivateBooking = createPrivateBooking(privateBookingDTO);
        return newPrivateBooking.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBooking(Long bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        PrivateBooking privateBooking = privateBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!activePlayer.equals(privateBooking.getPlayer())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        bookingUtilitiesService.checkBookingRefundable(bookingId);
        privateBooking.setUpdatedAt(Instant.now());
        privateBookingRepository.save(privateBooking);
    }

    public Integer countPrivateBookingsByPlayerIdAndSportCategory(Long playerId, SportCategory sportCategory) {
        List<PrivateBooking> privateBookings = privateBookingRepository.findByPlayer_PlayerId(playerId);
        List<PrivateBooking> filteredPrivateBookings = privateBookings.stream()
                .filter(pb -> pb.getBooking().getSportCategory() == sportCategory && pb.getBooking().getStatus() != BookingStatus.CANCELED
                        && pb.getBooking().getStatus() != BookingStatus.REFUNDED).toList();
        return filteredPrivateBookings.size();
    }

    public Optional<PrivateBooking> getPrivateBookingByBookingId(Long bookingId) {
        return privateBookingRepository.findById(bookingId);
    }

}
