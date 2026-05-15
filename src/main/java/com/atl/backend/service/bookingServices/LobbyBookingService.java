package com.atl.backend.service.bookingServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.Field;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.LobbyBookingParticipate;
import com.atl.backend.model.Player;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.BookingType;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.LobbyBookingParticipateRepository;
import com.atl.backend.repository.LobbyBookingRepository;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.response.LobbyInfoResponse;

@Service
public class LobbyBookingService {

    @Autowired
    private LobbyBookingRepository lobbyBookingRepository;
    @Autowired
    private LobbyBookingParticipateRepository lobbyBookingParticipateRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private BookingUtilitiesService bookingUtilitiesService;

    public List<LobbyBooking> getLobbyBookingsByOwnerPlayerId(Long ownerPlayerId) {
        return lobbyBookingRepository.findByOwnerPlayer_PlayerId(ownerPlayerId);
    }

    public List<LobbyBooking> getLobbyBookingsByPlayerId(Long playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = new ArrayList<>();
        for (LobbyBookingParticipate participation : participations) {
            lobbyBookings.add(participation.getLobbyBooking());
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getActiveLobbyBookingsByPlayerId(Long playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = new ArrayList<>();
        for (LobbyBookingParticipate participation : participations) {
            if (participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.CANCELED
                    && participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.FINISHED
                    && participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.REFUNDED) {
                lobbyBookings.add(participation.getLobbyBooking());
            }
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getLobbyBookingsByFieldId(Long fieldId) {
        return lobbyBookingRepository.findByBooking_Field_Id(fieldId);
    }

    public Optional<LobbyBooking> getLobbyBookingByBookingId(Long bookingId) {
        return lobbyBookingRepository.findById(bookingId);
    }

    public List<LobbyBooking> getBookedLobbyBookings(SportCategory sportCategory, Short totalPlayers,
            Long selectedCityId, Instant startTime, Instant finishTime) {
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
            lobbyBookings = lobbyBookings.stream().filter(
                    lb -> lb.getBooking().getStartTime() != null && !startTime.isAfter(lb.getBooking().getStartTime()))
                    .toList();
        }

        if (finishTime != null) {
            lobbyBookings = lobbyBookings.stream()
                    .filter(lb -> lb.getBooking().getFinishTime() != null
                            && !finishTime.isBefore(lb.getBooking().getFinishTime()))
                    .toList();
        }

        return lobbyBookings;
    }

    public Optional<LobbyBookingParticipate> getLobbyBookingParticipateByPlayerIdAndLobbyBookingId(Long playerId,
            Long lobbyBookingId) {
        Optional<LobbyBookingParticipate> lobbyBookingParticipateOptional = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(playerId, lobbyBookingId);

        return lobbyBookingParticipateOptional;
    }

    @Transactional(rollbackFor = Exception.class)
    public LobbyBookingParticipate createLobbyBookingParticipate(Long lobbyBookingId, Short playersPaid, BigDecimal cost) {

        LobbyBooking lobbyBooking = lobbyBookingRepository.findById(lobbyBookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (lobbyBooking.getPlayersToFind() < playersPaid) {
            throw new ConflictException(ErrorsString.NO_AVAILABLE_SPOTS);
        }

        lobbyBooking.setPlayersToFind((short) (lobbyBooking.getPlayersToFind() - playersPaid));
        lobbyBooking.setUpdatedAt(Instant.now());
        lobbyBookingRepository.save(lobbyBooking);

        BigDecimal feePerPlayer = lobbyBooking.getFeePerPlayer();
        if(cost.compareTo(feePerPlayer.multiply(BigDecimal.valueOf(playersPaid))) < 0) {
            throw new ConflictException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
        }

        Optional<LobbyBookingParticipate> existingParticipation = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(AccountContext.getPlayerId(), lobbyBookingId);

        // Volendo uno può partecipare alla stessa lobby più volte pagando più quote
        if (existingParticipation.isPresent() && !isLobbyParticipationRefundedByBookingIdAndAccountId(lobbyBookingId,
                AccountContext.getActivePlayer().getAccount().getId())) {
            LobbyBookingParticipate participation = existingParticipation.get();
            // Verificare se aggiunge un player in più
            participation.setPlayersPaid((short) (participation.getPlayersPaid() + playersPaid));
            participation.setUpdatedAt(Instant.now());
            BigDecimal newCost = participation.getCost().add(cost);
            participation.setCost(newCost);
            return lobbyBookingParticipateRepository.save(participation);
        }

        LobbyBookingParticipate lobbyBookingParticipate = new LobbyBookingParticipate();
        lobbyBookingParticipate.setLobbyBooking(lobbyBooking);
        lobbyBookingParticipate.setPlayer(AccountContext.getActivePlayer());
        lobbyBookingParticipate.setPlayersPaid(playersPaid);
        lobbyBookingParticipate.setCost(cost);
        return lobbyBookingParticipateRepository.save(lobbyBookingParticipate);
    }

    public Boolean isLobbyParticipationRefundedByBookingIdAndAccountId(Long bookingId, Long accountId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.LOBBY_BOOKING_PARTICIPATE,
                bookingId, accountId, PurchaseStatus.REFUNDED);
        if (purchases.isEmpty()) {
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyBooking(Long bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        LobbyBooking lobbyBooking = lobbyBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!activePlayer.equals(lobbyBooking.getOwnerPlayer())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        bookingUtilitiesService.checkBookingRefundable(bookingId);
        lobbyBooking.setUpdatedAt(Instant.now());
        lobbyBookingRepository.save(lobbyBooking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveLobbyBooking(Long bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        LobbyBookingParticipate lobbyBookingParticipate = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(activePlayer.getPlayerId(), bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));

        bookingUtilitiesService.checkBookingRefundable(bookingId);
        lobbyBookingParticipate.setUpdatedAt(Instant.now());
        lobbyBookingParticipateRepository.save(lobbyBookingParticipate);
    }

    @Transactional(rollbackFor = Exception.class)
    public LobbyBooking createLobbyBooking(LobbyBookingDTO lobbyBookingDTO) {
        if (!lobbyBookingDTO.getOwnerPlayerId().equals(AccountContext.getPlayerId())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        System.out.println("Creating lobby booking for player ID: " + lobbyBookingDTO.getOwnerPlayerId() + ", field ID: " + lobbyBookingDTO.getFieldId() + ", start time: " + lobbyBookingDTO.getStartTime() + ", finish time: " + lobbyBookingDTO.getFinishTime() + ", sport category: " + lobbyBookingDTO.getSportCategory() + ", total players: " + lobbyBookingDTO.getTotalPlayers());

        Booking booking = bookingUtilitiesService.createBooking(lobbyBookingDTO.getFieldId(),
                lobbyBookingDTO.getStartTime(),
                lobbyBookingDTO.getFinishTime(), lobbyBookingDTO.getSportCategory(), BookingType.LOBBY);
        LobbyBooking lobbyBooking = new LobbyBooking();
        lobbyBooking.setBooking(booking);
        lobbyBooking.setOwnerPlayer(AccountContext.getActivePlayer());
        lobbyBooking.setPlayersToFind(lobbyBookingDTO.getTotalPlayers());
        lobbyBooking.setTotalPlayers(lobbyBookingDTO.getTotalPlayers());

        BigDecimal fee = booking.getField().calculatePrivateCost(booking.getStartTime(), booking.getFinishTime());
        BigDecimal feePerPlayer = fee.divide(BigDecimal.valueOf(lobbyBookingDTO.getTotalPlayers()), 2,
                RoundingMode.HALF_UP);

        lobbyBooking.setFeePerPlayer(feePerPlayer);
        LobbyBooking savedLobbyBooking = lobbyBookingRepository.save(lobbyBooking);

        Short playersToPay = (short) (lobbyBookingDTO.getTotalPlayers() - lobbyBookingDTO.getPlayersToFind());
        BigDecimal totalCost = feePerPlayer.multiply(BigDecimal.valueOf(playersToPay)).setScale(2, RoundingMode.HALF_UP);

        createLobbyBookingParticipate(savedLobbyBooking.getId(),
                playersToPay, totalCost);
                        System.out.println("Created lobby booking participate for lobby ID: " + savedLobbyBooking.getId() + ", players paid: " + playersToPay);
        return savedLobbyBooking;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createLobbyBookingFromCheckoutPayload(CheckoutPayload checkoutPayload) {

        LobbyBookingDTO lobbyBookingDTO = new LobbyBookingDTO();
        lobbyBookingDTO.setFieldId(checkoutPayload.getFieldId());
        lobbyBookingDTO.setStartTime(checkoutPayload.getStartTime());
        lobbyBookingDTO.setFinishTime(checkoutPayload.getFinishTime());
        lobbyBookingDTO.setSportCategory(checkoutPayload.getSportCategory());
        lobbyBookingDTO.setOwnerPlayerId(checkoutPayload.getOwnerPlayerId());
        lobbyBookingDTO.setPlayersToFind(checkoutPayload.getPlayersToFind());
        lobbyBookingDTO.setTotalPlayers(checkoutPayload.getTotalPlayers());
        LobbyBooking newLobby = createLobbyBooking(lobbyBookingDTO);
        
        return newLobby.getId();
    }

    public Integer countLobbyParticipationByPlayerIdAndSportCategory(Long playerId, SportCategory sportCategory) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByPlayer_PlayerId(playerId);
        List<LobbyBookingParticipate> filteredParticipations = participations.stream()
                .filter(p -> p.getLobbyBooking().getBooking().getSportCategory() == sportCategory
                        && p.getLobbyBooking().getBooking().getStatus() != BookingStatus.CANCELED
                        && p.getLobbyBooking().getBooking().getStatus() != BookingStatus.REFUNDED)
                .toList();
        return filteredParticipations.size();
    }

    public Short getPlayersPaidInLobbyBookingPerPlayerId(Long bookingId, Long playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByLobbyBooking_IdAndPlayer_PlayerId(bookingId,
                        playerId)
                .stream()
                .filter(p -> !isLobbyParticipationRefundedByBookingIdAndAccountId(
                        p.getLobbyBooking().getId(),
                        p.getPlayer().getAccount().getId()))
                .toList();

        Short playersPaid = 0;
        for (LobbyBookingParticipate participation : participations) {
            playersPaid = (short) (playersPaid + participation.getPlayersPaid().shortValue());
        }
        return playersPaid;
    }

    public LobbyInfoResponse getLobbyInfo(Long lobbyId, Long playerId) {
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

}
