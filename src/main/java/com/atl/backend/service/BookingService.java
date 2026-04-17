package com.atl.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.repository.BookingRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.LobbyBookingRepository;
import com.atl.backend.repository.MatchBookingRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.PrivateBookingRepository;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;
import com.atl.backend.response.LobbyInfoResponse;
import com.atl.backend.repository.LobbyBookingParticipateRepository;

import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.BookingType;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.FootballMatchStatus;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.dto.MatchBookingDTO;
import com.atl.backend.dto.PrivateBookingDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.LobbyBookingParticipate;
import com.atl.backend.model.Match;
import com.atl.backend.model.MatchBooking;
import com.atl.backend.model.Player;
import com.atl.backend.model.Field;
import com.atl.backend.model.FootballMatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private SportService sportService;
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

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private FootballMatchRepository footballMatchRepository;

    public Optional<Booking> getBookingByBookingId(Integer bookingId) {
        return bookingRepository.findById(bookingId);
    }

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

    public Short getPlayersPaidInLobbyBookingPerPlayerId(Integer bookingId, Integer playerId) {
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
    public FieldsAndMaxPlayersPerFieldResponse getFreeFieldsByMaxPlayersAndCityAndDateAndSportCategory(
            Integer maxPlayers, Integer cityId, LocalDateTime startTime, LocalDateTime finishTime,
            SportCategory sportCategory) {
        List<Booking> bookings = bookingRepository
                .findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(cityId, startTime,
                        finishTime);
        List<Field> cityFields = fieldService.getFieldsByCityId(cityId);
        List<Integer> bookedFieldIds = bookings.stream().map(b -> b.getField().getId()).toList();

        Map<Integer, Short> maxPlayersPerField = new HashMap<>();
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

    public List<Booking> getBookingsByFieldIdFromNow(Integer fieldId) {
        return bookingRepository.findByField_IdAndStartTimeGreaterThanEqual(fieldId, LocalDateTime.now());
    }

    public List<Field> getFreeFieldInCityByDate(Integer cityId, LocalDateTime startTime, LocalDateTime finishTime) {
        List<Booking> bookings = bookingRepository
                .findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(cityId, startTime,
                        finishTime);
        List<Field> cityFields = fieldService.getFieldsByCityId(cityId);
        List<Integer> bookedFieldIds = bookings.stream().map(b -> b.getField().getId()).toList();
        return cityFields.stream().filter(f -> !bookedFieldIds.contains(f.getId())).toList();
    }

    public Boolean isFieldFree(Integer fieldId, LocalDateTime startTime, LocalDateTime finishTime) {

        if (startTime.isBefore(LocalDateTime.now()) || finishTime.isBefore(LocalDateTime.now())
                || startTime.isAfter(finishTime)) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }

        Field field = fieldService.getFieldById(fieldId).get();
        List<Field> freeFields = getFreeFieldInCityByDate(field.getCity().getId(), startTime, finishTime);
        return freeFields.contains(field);
    }

    public Booking createBooking(Integer fieldId, String startTime, String finishTime,
            String sportCategory,
            BookingType bookingType) {

        if (!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory sportCategoryEnum = SportCategory.valueOf(sportCategory.toUpperCase());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            start = LocalDateTime.parse(startTime);
            end = LocalDateTime.parse(finishTime);
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

        Booking booking = new Booking();
        booking.setField(field);
        booking.setStartTime(start);
        booking.setFinishTime(end);
        booking.setSportCategory(sportCategoryEnum);
        booking.setBookingType(bookingType);
        return bookingRepository.save(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public LobbyBookingParticipate createLobbyBookingParticipate(Integer lobbyBookingId, Short playersPaid) {

        LobbyBooking lobbyBooking = lobbyBookingRepository.findById(lobbyBookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));

        if (lobbyBooking.getPlayersToFind() < playersPaid) {
            throw new ConflictException(ErrorsString.NO_AVAILABLE_SPOTS);
        }

        lobbyBooking.setPlayersToFind((short) (lobbyBooking.getPlayersToFind() - playersPaid));
        lobbyBooking.setUpdatedAt(LocalDateTime.now());
        lobbyBookingRepository.save(lobbyBooking);

        Optional<LobbyBookingParticipate> existingParticipation = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(AccountContext.getPlayerId(), lobbyBookingId);

        // Volendo uno può partecipare alla stessa lobby più volte pagando più quote
        if (existingParticipation.isPresent() && !isLobbyParticipationRefundedByBookingIdAndAccountId(lobbyBookingId,
                AccountContext.getActivePlayer().getAccount().getId())) {
            LobbyBookingParticipate participation = existingParticipation.get();
            // Verificare se aggiunge un player in più
            participation.setPlayersPaid((short) (participation.getPlayersPaid() + playersPaid));
            participation.setUpdatedAt(LocalDateTime.now());

            return lobbyBookingParticipateRepository.save(participation);
        }

        LobbyBookingParticipate lobbyBookingParticipate = new LobbyBookingParticipate();
        lobbyBookingParticipate.setLobbyBooking(lobbyBooking);
        lobbyBookingParticipate.setPlayer(AccountContext.getActivePlayer());
        lobbyBookingParticipate.setPlayersPaid(playersPaid);
        lobbyBookingParticipateRepository.save(lobbyBookingParticipate);
        return lobbyBookingParticipate;
    }

    @Transactional(rollbackFor = Exception.class)
    public PrivateBooking createPrivateBooking(PrivateBookingDTO privateBookingDTO) {
        if (!AccountContext.getPlayerId().equals(privateBookingDTO.getPlayerId())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        Booking booking = createBooking(privateBookingDTO.getFieldId(), privateBookingDTO.getStartTime(),
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
    public LobbyBooking createLobbyBooking(LobbyBookingDTO lobbyBookingDTO) {
        if (!AccountContext.getPlayerId().equals(lobbyBookingDTO.getOwnerPlayerId())) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        Booking booking = createBooking(lobbyBookingDTO.getFieldId(), lobbyBookingDTO.getStartTime(),
                lobbyBookingDTO.getFinishTime(), lobbyBookingDTO.getSportCategory(), BookingType.LOBBY);
        LobbyBooking lobbyBooking = new LobbyBooking();
        lobbyBooking.setBooking(booking);
        lobbyBooking.setOwnerPlayer(AccountContext.getActivePlayer());
        lobbyBooking.setPlayersToFind(lobbyBookingDTO.getPlayersToFind());
        lobbyBooking.setTotalPlayers(lobbyBookingDTO.getTotalPlayers());

        BigDecimal fee = booking.getField().calculatePrivateCost(booking.getStartTime(), booking.getFinishTime());
        BigDecimal feePerPlayer = fee.divide(BigDecimal.valueOf(lobbyBookingDTO.getTotalPlayers()), 2,
                RoundingMode.HALF_UP);

        lobbyBooking.setFeePerPlayer(feePerPlayer);
        LobbyBooking savedLobbyBooking = lobbyBookingRepository.save(lobbyBooking);

        Short playersToPay = (short) (lobbyBookingDTO.getTotalPlayers() - lobbyBookingDTO.getPlayersToFind());

        createLobbyBookingParticipate(lobbyBooking.getId(),
                playersToPay);
        return savedLobbyBooking;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer createLobbyBookingFromCheckoutPayload(CheckoutPayload checkoutPayload) {

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

    @Transactional(rollbackFor = Exception.class)
    public Integer createPrivateBookingFromCheckoutPayload(CheckoutPayload checkoutPayload) {

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
    public void startBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        booking.setStatus(BookingStatus.FINISHED);
        bookingRepository.save(booking);
    }

    public Boolean isBookingRefundable(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!booking.getStatus().equals(BookingStatus.BOOKED)) {
            throw new ConflictException(ErrorsString.BOOKING_NOT_REFUNDABLE);
        }

        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            if (booking.getFinishTime().isAfter(LocalDateTime.now())) {
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
        if (booking.getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException(ErrorsString.BOOKING_NOT_REFUNDABLE);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyBooking(Integer bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        LobbyBooking lobbyBooking = lobbyBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!lobbyBooking.getOwnerPlayer().equals(activePlayer)) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        isBookingRefundable(bookingId);
        lobbyBooking.setUpdatedAt(LocalDateTime.now());
        lobbyBookingRepository.save(lobbyBooking);
    }

    public void setBookingRefunded(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        booking.setStatus(BookingStatus.REFUND);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBooking(Integer bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        PrivateBooking privateBooking = privateBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (!privateBooking.getPlayer().equals(activePlayer)) {
            throw new ForbiddenException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
        }

        isBookingRefundable(bookingId);
        privateBooking.setUpdatedAt(LocalDateTime.now());
        privateBookingRepository.save(privateBooking);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveLobbyBooking(Integer bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        LobbyBookingParticipate lobbyBookingParticipate = lobbyBookingParticipateRepository
                .findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(activePlayer.getPlayerId(), bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));

        isBookingRefundable(bookingId);
        lobbyBookingParticipate.setUpdatedAt(LocalDateTime.now());
        lobbyBookingParticipateRepository.save(lobbyBookingParticipate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveAllBookings() {
        Integer playerId = AccountContext.getPlayerId();
        List<LobbyBooking> lobbyBookings = getLobbyBookingsByPlayerId(playerId);
        for (LobbyBooking lobbyBooking : lobbyBookings) {
            if (lobbyBooking.getOwnerPlayer().getPlayerId() == playerId) {
                refundLobbyBooking(lobbyBooking.getId());
            } else {
                leaveLobbyBooking(lobbyBooking.getId());
            }
        }

        List<PrivateBooking> privateBookings = getPrivateBookingsByPlayerId(playerId);
        for (PrivateBooking privateBooking : privateBookings) {
            refundPrivateBooking(privateBooking.getId());
        }
    }

    public Boolean isLobbyParticipationRefundedByBookingIdAndAccountId(Integer bookingId, Integer accountId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.LOBBY_BOOKING_PARTICIPATE,
                bookingId, accountId, PurchaseStatus.REFUNDED);
        if (purchases.isEmpty()) {
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchBooking createMatchBooking(Integer matchId, LocalDateTime startTime,
            LocalDateTime finishTime) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

        Booking booking = createBooking(match.getTournament().getField().getId(), startTime.toString(),
                finishTime.toString(), match.getTournament().getSport().getSportCategory().toString(),
                BookingType.MATCH);

        MatchBooking matchBooking = new MatchBooking();
        matchBooking.setBooking(booking);
        matchBooking.setMatch(match);
        matchBooking.setCoordinator(match.getTournament().getCoordinator());
        return matchBookingRepository.save(matchBooking);
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchBookingDTO scheduleMatch(Integer matchId, LocalDateTime scheduledStart) {
        // Il match deve essere schedulato almeno 2 ore prima dell'orario attuale
        if (scheduledStart.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException(ErrorsString.MATCH_SCHEDULE_TOO_LATE);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        Tournament tournament = match.getTournament();

        if (!tournament.isTournamentActive()) {
            throw new ConflictException(ErrorsString.TOURNAMENT_NOT_ACTIVE);
        }

        if (!tournament.getCoordinator().equals(AccountContext.getActiveCoordinator())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        Short minutesPerRound = tournament.getSport().getMinutesPerRound();
        Short minutesPerPause = tournament.getSport().getMinutesPerPause();

        // Considero 15 minuti di preparazione
        LocalDateTime startTime = scheduledStart.minusMinutes(15);

        // Considero la durata massima della partita, che è 2 round più una pausa, più
        // 15 minuti di buffer finale
        LocalDateTime finishTime = scheduledStart.plusMinutes(minutesPerRound * 2 + minutesPerPause + 15);

        if (!isFieldFree(tournament.getField().getId(), startTime, finishTime)) {
            throw new ConflictException(ErrorsString.FIELD_NOT_AVAILABLE);
        }

        if (match.getScheduledStart().equals(scheduledStart)) {
            throw new ConflictException(ErrorsString.MATCH_ALREADY_SCHEDULED_FOR_THIS_TIME);
        } else if (match.getScheduledStart() != null
                && match.getScheduledStart().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new ConflictException(ErrorsString.YOU_CANNOT_RESCHEDULE_MATCH_LESS_THAN_24_HOURS_BEFORE);
        }

        MatchBooking matchBooking;
        if (match.getScheduledStart() == null) {
            matchBooking = createMatchBooking(matchId, startTime, finishTime);
        } else {
            Optional<MatchBooking> existingMatchBooking = matchBookingRepository.findByMatch_Id(matchId);
            if (existingMatchBooking.isPresent()) {
                matchBooking = existingMatchBooking.get();
                matchBooking.getBooking().setStartTime(startTime);
                matchBooking.getBooking().setFinishTime(finishTime);
                matchBooking.getBooking().setUpdatedAt(LocalDateTime.now());
                matchBookingRepository.save(matchBooking);
            } else {
                matchBooking = createMatchBooking(matchId, startTime, finishTime);
            }
        }

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                footballMatch.setStatus(FootballMatchStatus.SCHEDULED);
                footballMatchRepository.save(footballMatch);
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        return MatchBookingDTO.fromMatchBooking(matchBooking);
    }
}
