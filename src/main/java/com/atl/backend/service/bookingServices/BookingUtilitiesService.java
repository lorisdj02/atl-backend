package com.atl.backend.service.bookingServices;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ATLCostants;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.Field;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.BookingType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.BookingRepository;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.SportService;

@Service
public class BookingUtilitiesService {
    @Autowired
    private SportService sportService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private BookingRepository bookingRepository;

    public Booking createBooking(Long fieldId, String startTime, String finishTime,
            String sportCategory,
            BookingType bookingType) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory sportCategoryEnum = SportCategory.valueOf(sportCategory.toUpperCase());

        Instant now = Instant.now();
        Instant start = null;
        Instant end = null;
        try {
            start = Instant.parse(startTime);
            end = Instant.parse(finishTime);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        if (start.isBefore(now) || end.isBefore(now) || start.isAfter(end)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        fieldService.checkFieldExists(fieldId);
        Field field = fieldService.getFieldById(fieldId).get();

        if (!isFieldFree(fieldId, start, end)) {
            throw new ConflictException(ErrorsString.FIELD_NOT_AVAILABLE);
        }

        Instant openingTime = field.getOpeningTime().atDate(start.atZone(field.getCity().getZoneId()).toLocalDate())
                .atZone(field.getCity().getZoneId()).toInstant();
        Instant closingTime = field.getClosingTime().atDate(start.atZone(field.getCity().getZoneId()).toLocalDate())
                .atZone(field.getCity().getZoneId()).toInstant();
        if (start.isBefore(openingTime) || end.isAfter(closingTime)) {
            throw new BadRequestException(ErrorsString.BOOKING_OUTSIDE_OPENING_HOURS);
        }

        Booking booking = new Booking();
        booking.setField(field);
        booking.setStartTime(start);
        booking.setFinishTime(end);
        booking.setSportCategory(sportCategoryEnum);
        booking.setBookingType(bookingType);
        return bookingRepository.save(booking);
    }

    public Boolean isFieldFree(Long fieldId, Instant startTime, Instant finishTime) {

        if (startTime.isBefore(Instant.now()) || finishTime.isBefore(Instant.now())
                || startTime.isAfter(finishTime)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        Field field = fieldService.getFieldById(fieldId).get();
        List<Field> freeFields = getFreeFieldInCityByDate(field.getCity().getId(), startTime, finishTime);
        return freeFields.contains(field);
    }

    public List<Field> getFreeFieldInCityByDate(Long cityId, Instant startTime, Instant finishTime) {

        List<Booking> bookings = bookingRepository
                .findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(cityId, startTime,
                        finishTime);
        List<Field> cityFields = fieldService.getFieldsByCityId(cityId);
        List<Long> bookedFieldIds = bookings.stream().map(b -> b.getField().getId()).toList();
        return cityFields.stream().filter(f -> !bookedFieldIds.contains(f.getId())).toList();
    }

    public Boolean checkBookingRefundable(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!(BookingStatus.BOOKED).equals(booking.getStatus())) {
            throw new ConflictException(ErrorsString.BOOKING_NOT_REFUNDABLE);
        }

        if (!booking.getStartTime().isAfter(Instant.now())) {
            if (booking.getFinishTime().isAfter(Instant.now())) {
                // finishBooking(bookingId);
                throw new ConflictException(ErrorsString.BOOKING_ALREADY_FINISHED);
            } else {
                // startBooking(bookingId);
                // Non dovrebbe avvenire dato che c'è un job che controlla sempre
                throw new ConflictException(ErrorsString.BOOKING_ALREADY_STARTED);
            }
        }

        // Il booking è rimborsabile solo se manca più di un'ora all'inizio, per evitare
        // abusi (es. prenotazione last minute e richiesta di rimborso subito dopo)
        if (booking.getStartTime().isBefore(ATLCostants.getBookingRefundLimit())) {
            throw new ConflictException(ErrorsString.BOOKING_NOT_REFUNDABLE);
        }

        return true;
    }

}
