package com.atl.backend.service.orchestrator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.model.Field;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.Player;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.paymentServices.PaymentRedeemService;
import com.atl.backend.service.paymentServices.PaymentRefundService;
import com.atl.backend.service.paymentServices.PaymentService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;

@Service
public class PurchasesOrchestrator {
    @Autowired
    private TournamentParticipantService tournamentParticipantService;

    @Autowired
    private PaymentRedeemService paymentRedeemService;
    @Autowired
    private PaymentRefundService paymentRefundService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private FieldService fieldService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallups(Integer tournamentId, Integer teamId) {
        tournamentParticipantService.deleteCallupsCheck(tournamentId, teamId);

        // Devono essere rimborsati tutti i giocatori, perchè anche chi non è convocato
        // potrebbe
        // aver contribuito alla quota di iscrizione del torneo, e quindi ha diritto al
        // rimborso
        paymentRefundService.refundPurchaseForPlayerIds(tournamentId,
                teamService.getPlayerIdsByTeamId(teamId));

        tournamentParticipantService.deleteCallupsByTeamIdIdAndTournamentId(teamId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startBooking(Integer bookingId) {
        bookingService.startBooking(bookingId);
        paymentRedeemService.redeemBookingPurchasesByBookingId(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyBooking(Integer bookingId) {
        bookingService.refundLobbyBooking(bookingId);
        paymentRefundService.refundLobbyCreationByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBooking(Integer bookingId) {
        bookingService.refundPrivateBooking(bookingId);
        paymentRefundService.refundPrivateBookingByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveLobbyBooking(Integer bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        bookingService.leaveLobbyBooking(bookingId);
        // paymentRefundService.refundLobbyCreationByBookingId(bookingId);
        paymentRefundService.refundLobbyParticipationByBookingIdAndAccountId(bookingId,
                activePlayer.getAccount().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckoutSession createCheckoutSession(CheckoutPayload payload) {
        CheckoutType checkoutType;
        try {
            checkoutType = CheckoutType.valueOf(payload.getCheckoutType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_TYPE);
        }

        if (payload.getAmount() == null) {
            throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }

        LocalDateTime startTime;
        LocalDateTime finishTime;
        Field field;
        BigDecimal totalLobbyCost;
        BigDecimal playerCost;

        switch (checkoutType) {
            case TOURNAMENT_FEE:
                if (payload.getTournamentId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                BigDecimal remainingAmount = tournamentParticipantService.getToPayByTournamentIdAndPlayerId(
                        payload.getTournamentId(),
                        AccountContext.getPlayerId());

                if (payload.getAmount().compareTo(remainingAmount) != 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
                }
                break;
            case PRIVATE_BOOKING:
                if (payload.getFieldId() == null || payload.getStartTime() == null || payload.getFinishTime() == null
                        || payload.getOwnerPlayerId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                if (!payload.getOwnerPlayerId().equals(AccountContext.getPlayerId())) {
                    throw new BadRequestException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
                }

                try {
                    startTime = LocalDateTime.parse(payload.getStartTime());
                    finishTime = LocalDateTime.parse(payload.getFinishTime());
                } catch (Exception e) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                field = fieldService.getFieldById(payload.getFieldId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.FIELD_NOT_FOUND));

                if (!bookingService.isFieldFree(payload.getFieldId(), startTime, finishTime)) {
                    throw new BadRequestException(ErrorsString.FIELD_NOT_AVAILABLE);
                }

                if (payload.getAmount().compareTo(field.calculatePrivateCost(startTime, finishTime)) != 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
                }
                break;

            case LOBBY_BOOKING_CREATE:
                if (payload.getOwnerPlayerId() == null || payload.getFieldId() == null || payload.getStartTime() == null
                        || payload.getFinishTime() == null
                        || payload.getPlayersToFind() == null || payload.getTotalPlayers() == null
                        || payload.getPlayersPaid() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                if (!payload.getOwnerPlayerId().equals(AccountContext.getPlayerId())) {
                    throw new BadRequestException(ErrorsString.YOU_MUST_OWN_THE_BOOKING);
                }

                try {
                    startTime = LocalDateTime.parse(payload.getStartTime());
                    finishTime = LocalDateTime.parse(payload.getFinishTime());
                } catch (Exception e) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                field = fieldService.getFieldById(payload.getFieldId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.FIELD_NOT_FOUND));

                if (!bookingService.isFieldFree(payload.getFieldId(), startTime, finishTime)) {
                    throw new BadRequestException(ErrorsString.FIELD_NOT_AVAILABLE);
                }

                if (payload.getPlayersPaid() + payload.getPlayersToFind() != payload.getTotalPlayers()) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                totalLobbyCost = field.calculatePrivateCost(startTime, finishTime);

                playerCost = totalLobbyCost.divide(BigDecimal.valueOf(payload.getTotalPlayers()), 2,
                        RoundingMode.HALF_UP);

                if (payload.getAmount()
                        .compareTo(playerCost.multiply(BigDecimal.valueOf(payload.getPlayersPaid()))) < 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
                }
                break;

            case LOBBY_BOOKING_PARTICIPATE:
                if (payload.getLobbyId() == null || payload.getPlayersPaid() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                if (payload.getPlayersPaid() <= 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                LobbyBooking lobbyBooking = bookingService.getLobbyBookingByBookingId(payload.getLobbyId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.BOOKING_NOT_FOUND));

                if (lobbyBooking.getPlayersToFind() <= 0) {
                    throw new BadRequestException(ErrorsString.NO_AVAILABLE_SPOTS);
                }

                totalLobbyCost = lobbyBooking.getBooking().getField().calculatePrivateCost(
                        lobbyBooking.getBooking().getStartTime(), lobbyBooking.getBooking().getFinishTime());

                playerCost = totalLobbyCost.divide(BigDecimal.valueOf(payload.getTotalPlayers()), 2,
                        RoundingMode.HALF_UP);

                if (payload.getAmount()
                        .compareTo(playerCost.multiply(BigDecimal.valueOf(payload.getPlayersPaid()))) < 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
                }

                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_TYPE);
        }
        return paymentService.createCheckoutSession(payload, checkoutType);
    }
}
