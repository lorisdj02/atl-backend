package com.atl.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.repository.BookingRepository;
import com.atl.backend.repository.LobbyBookingRepository;
import com.atl.backend.repository.MatchBookingRepository;
import com.atl.backend.repository.PrivateBookingRepository;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;
import com.atl.backend.response.LobbyInfoResponse;
import com.atl.backend.response.ScheduleMatchResponse;
import com.atl.backend.repository.LobbyBookingParticipateRepository;

import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.LobbyBookingParticipate;
import com.atl.backend.model.MatchBooking;
import com.atl.backend.model.Field;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private MatchBookingRepository matchBookingRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private LobbyBookingRepository lobbyBookingRepository;
    @Autowired
    private PrivateBookingRepository privateBookingRepository;
    @Autowired
    private LobbyBookingParticipateRepository lobbyBookingParticipateRepository;
    @Autowired
    private FieldService fieldService;



    public List<PrivateBooking> getPrivateBookingsByPlayerId(Integer playerId) {
        return privateBookingRepository.findByPlayer_PlayerId(playerId);
    }

    public List<PrivateBooking> getActivePrivateBookingsByPlayerId(Integer playerId) {
        List<PrivateBooking> privateBookings = privateBookingRepository.findByPlayer_PlayerId(playerId);
        List<PrivateBooking> activePrivateBookings = List.of();
        for (PrivateBooking booking : privateBookings) {
            if (booking.getBooking().getStatus() != BookingStatus.CANCELED
                    && booking.getBooking().getStatus() != BookingStatus.FINISHED) {
                activePrivateBookings.add(booking);
            }
        }
        return activePrivateBookings;
    }

    public List<LobbyBooking> getLobbyBookingsByOwnerPlayerId(Integer ownerPlayerId) {
        return lobbyBookingRepository.findByOwnerPlayer_PlayerId(ownerPlayerId);
    }

    public List<LobbyBooking> getLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            lobbyBookings.add(participation.getLobbyBooking());
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getActiveLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            if (participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.CANCELED
                    && participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.FINISHED) {
                lobbyBookings.add(participation.getLobbyBooking());
            }
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getNotRefundedLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            if (participation.getTransactionGroup().getStatus() != TransactionStatus.REFUNDED) {
                lobbyBookings.add(participation.getLobbyBooking());
            }
        }
        return lobbyBookings;
    }

    public Short getPlayersPaidInLobbyBookingPerPlayerId(Integer bookingId, Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByLobbyBooking_IdAndPlayer_PlayerId(bookingId,
                        playerId)
                .stream().filter(p -> p.getTransactionGroup().getStatus() != TransactionStatus.CONFIRMED)
                .toList();

        if (participations.size() > 0) {
            return 0;
        }
        return participations.get(0).getPlayersPaid();
    }

    public List<LobbyBooking> getLobbyBookingsByFieldId(Integer fieldId) {
        return lobbyBookingRepository.findByBooking_Field_Id(fieldId);
    }

    public List<PrivateBooking> getPrivateBookingsByFieldId(Integer fieldId) {
        return privateBookingRepository.findByBooking_Field_Id(fieldId);
    }

    public List<MatchBooking> getMatchBookingsByFieldId(Integer fieldId) {
        return matchBookingRepository.findByBooking_Field_Id(fieldId);
    }

    public List<Booking> getBookingsByFieldId(Integer fieldId) {
        return bookingRepository.findByField_Id(fieldId);
    }

    public Optional<LobbyBooking> getLobbyBookingByBookingId(Integer bookingId) {
        return lobbyBookingRepository.findById(bookingId);
    }

    public Optional<PrivateBooking> getPrivateBookingByBookingId(Integer bookingId) {
        return privateBookingRepository.findById(bookingId);
    }

    public Optional<MatchBooking> getMatchBookingByBookingId(Integer bookingId) {
        return matchBookingRepository.findById(bookingId);
    }

    public List<LobbyBooking> getBookedLobbyBookings(SportCategory sportCategory, Short totalPlayers,
            Integer selectedCityId, LocalDateTime startTime, LocalDateTime finishTime) {
        List<LobbyBooking> lobbyBookings = lobbyBookingRepository
                .findBookedByBooking_SportCategory(
                        sportCategory);
        if (totalPlayers != null) {
            lobbyBookings = lobbyBookings.stream().filter(lb -> lb.getTotalPlayers() == totalPlayers).toList();
        }

        if (selectedCityId != null) {
            lobbyBookings = lobbyBookings.stream()
                    .filter(lb -> lb.getBooking().getField().getCity().getId() == selectedCityId).toList();
        }

        if (startTime != null) {
            lobbyBookings = lobbyBookings.stream().filter(lb -> !lb.getBooking().getStartTime().isBefore(startTime))
                    .toList();
        }

        if (finishTime != null) {
            lobbyBookings = lobbyBookings.stream().filter(lb -> !lb.getBooking().getFinishTime().isAfter(finishTime))
                    .toList();
        }

        return lobbyBookings;
    }

    public LobbyInfoResponse getLobbyInfo(Integer lobbyId, Integer playerId) {
        Optional<LobbyBooking> lobbyBookingOptional = getLobbyBookingByBookingId(lobbyId);
        if (lobbyBookingOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.BOOKING_NOT_FOUND);
        }
        Optional<LobbyBookingParticipate> lobbyBookingParticipate = getLobbyBookingParticipateByPlayerIdAndLobbyBookingId(
                playerId, lobbyId);
        LobbyBooking lobbyBooking = lobbyBookingOptional.get();
        String usernamePerBooking = lobbyBooking.getOwnerPlayer().getAccount().getUsername() + " ("
                + lobbyBooking.getOwnerPlayer().getAccount().getName() + " "
                + lobbyBooking.getOwnerPlayer().getAccount().getSurname() + ")";
        Short maxPlayers = lobbyBooking.getTotalPlayers();
        Short currentPlayers = lobbyBooking.getCurrentPlayers();
        Short playersPaid = 0;
        String completeAddress = lobbyBooking.getBooking().getField().getCompleteAddress();
        Field field = lobbyBooking.getBooking().getField();
        if (lobbyBookingParticipate.isPresent()) {
            playersPaid = lobbyBookingParticipate.get().getPlayersPaid();
        }

        return LobbyInfoResponse.builder()
                .lobbyBooking(LobbyBookingDTO.fromLobbyBooking(lobbyBooking))
                .usernamePerBooking(usernamePerBooking)
                .maxPlayersPerLobby(maxPlayers)
                .currentPlayersPerLobby(currentPlayers)
                .playersPaidPerBooking(playersPaid)
                .completeAddressPerField(completeAddress)
                .field(FieldDTO.fromField(field))
                .build();
    }

    public Optional<LobbyBookingParticipate> getLobbyBookingParticipateByPlayerIdAndLobbyBookingId(Integer playerId,
            Integer lobbyBookingId) {
        Optional<LobbyBookingParticipate> lobbyBookingParticipateOptional = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(playerId, lobbyBookingId);

        return lobbyBookingParticipateOptional;
    }

    public Integer countPrivateBookingsByPlayerIdAndSportCategory(Integer playerId, SportCategory sportCategory) {
        List<PrivateBooking> privateBookings = privateBookingRepository.findByPlayer_PlayerId(playerId);
        List<PrivateBooking> filteredPrivateBookings = privateBookings.stream()
                .filter(pb -> pb.getBooking().getSportCategory() == sportCategory).toList();
        return filteredPrivateBookings.size();
    }

    public Integer countLobbyParticipationByPlayerIdAndSportCategory(Integer playerId, SportCategory sportCategory) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBookingParticipate> filteredParticipations = participations.stream()
                .filter(p -> p.getLobbyBooking().getBooking().getSportCategory() == sportCategory).toList();
        return filteredParticipations.size();
    }

    @Transactional(readOnly = true)
    public FieldsAndMaxPlayersPerFieldResponse getFreeFieldsByMaxPlayersAndCityAndDateAndSportCategory(Integer maxPlayers, Integer cityId, LocalDateTime startTime, LocalDateTime finishTime, SportCategory sportCategory) {
        List<Booking> bookings = bookingRepository.findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(cityId, startTime, finishTime);
        List<Field> cityFields = fieldService.getFieldsByCityId(cityId);
        List<Integer> bookedFieldIds = bookings.stream().map(b -> b.getField().getId()).toList();

        Map<Integer, Short> maxPlayersPerField = new HashMap<>();
        List<Field> freeFields = cityFields.stream().filter(f -> !bookedFieldIds.contains(f.getId())).toList();
        for (Field field : freeFields) {
            maxPlayersPerField.put(field.getId(), fieldService.getMaxPlayersByFieldIdAndSportCategory(field.getId(), sportCategory));
        }
        
        return FieldsAndMaxPlayersPerFieldResponse.builder()
                .fields(freeFields.stream().map(FieldDTO::fromField).toList())
                .maxPlayersPerField(maxPlayersPerField)
                .build();
    }

    public List<Booking> getBookingsByFieldIdFromNow(Integer fieldId) {
        return bookingRepository.findByField_IdAndStartTimeGreaterThanEqual(fieldId, LocalDateTime.now());
    }
}
