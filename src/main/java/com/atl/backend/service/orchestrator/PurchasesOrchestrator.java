package com.atl.backend.service.orchestrator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ATLCostants;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.PurchaseDetail;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Account;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.model.Field;
import com.atl.backend.model.FriendInvite;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.Player;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.TransactionPendingReason;
import com.atl.backend.model.enums.TransactionSource;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.request.RegisterAccountRequest;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.accountServices.AccountService;
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.bookingServices.BookingService;
import com.atl.backend.service.bookingServices.BookingUtilitiesService;
import com.atl.backend.service.bookingServices.LobbyBookingService;
import com.atl.backend.service.bookingServices.PrivateBookingService;
import com.atl.backend.service.paymentServices.PaymentRedeemService;
import com.atl.backend.service.paymentServices.PaymentRefundService;
import com.atl.backend.service.paymentServices.PaymentService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class PurchasesOrchestrator {
    @Autowired
    private TournamentParticipantService tournamentParticipantService;
    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private PaymentRedeemService paymentRedeemService;
    @Autowired
    private PaymentRefundService paymentRefundService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private AccountService accountService;

    @Autowired
    private LobbyBookingService lobbyBookingService;
    @Autowired
    private PrivateBookingService privateBookingService;
    @Autowired
    private BookingUtilitiesService bookingUtilitiesService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallups(Long tournamentId, Long teamId) {
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
    public void startBooking(Long bookingId) {
        bookingService.startBooking(bookingId);
        paymentRedeemService.redeemBookingPurchasesByBookingId(bookingId);
        paymentService.payOrganizationByBookingId(bookingId);
        Set<Long> playerIds = bookingService.getPlayerIdsByBookingId(bookingId);

        for (Long playerId : playerIds) {
            Player player = playerService.getPlayerByPlayerId(playerId)
                    .orElseThrow(() -> new BadRequestException(ErrorsString.PLAYER_NOT_FOUND));
            Optional<FriendInvite> friendInvite = accountService.redeemInvite(player.getAccount());
            if (friendInvite.isPresent()) {
                paymentService.updateTransactionGroupStatus(
                        friendInvite.get().getInviterTransaction().getTransactionGroup().getId(),
                        TransactionStatus.CONFIRMED);
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyBooking(Long bookingId) {
        lobbyBookingService.refundLobbyBooking(bookingId);
        paymentRefundService.refundLobbyCreationByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBooking(Long bookingId) {
        privateBookingService.refundPrivateBooking(bookingId);
        paymentRefundService.refundPrivateBookingByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveLobbyBooking(Long bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        LobbyBooking lobbyBooking = lobbyBookingService.getLobbyBookingByBookingId(bookingId)
                .orElseThrow(() -> new BadRequestException(ErrorsString.BOOKING_NOT_FOUND));

        if (activePlayer.getPlayerId().equals(lobbyBooking.getOwnerPlayer().getPlayerId())) {
            refundLobbyBooking(bookingId);
            return;
        }
        lobbyBookingService.leaveLobbyBooking(bookingId);
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
        BigDecimal atlFee;

        switch (checkoutType) {
            case TOURNAMENT_FEE:
                if (payload.getTournamentId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                BigDecimal remainingAmount = tournamentParticipantService.getToPayByTournamentIdAndPlayerId(
                        payload.getTournamentId(),
                        AccountContext.getPlayerId());

                if (payload.getAmount().compareTo(remainingAmount) > 0) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
                }
                break;
            case PRIVATE_BOOKING:
                if (payload.getFieldId() == null || payload.getStartTime() == null || payload.getFinishTime() == null
                        || payload.getOwnerPlayerId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }

                if (!AccountContext.getPlayerId().equals(payload.getOwnerPlayerId())) {
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

                if (!bookingUtilitiesService.isFieldFree(payload.getFieldId(), startTime, finishTime)) {
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

                if (!AccountContext.getPlayerId().equals(payload.getOwnerPlayerId())) {
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

                if (!bookingUtilitiesService.isFieldFree(payload.getFieldId(), startTime, finishTime)) {
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

                LobbyBooking lobbyBooking = lobbyBookingService.getLobbyBookingByBookingId(payload.getLobbyId())
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

        switch (checkoutType) {
            case TOURNAMENT_FEE:
                atlFee = BigDecimal.ZERO;
                break;
            case LOBBY_BOOKING_PARTICIPATE:
            case LOBBY_BOOKING_CREATE:
            case PRIVATE_BOOKING:
                atlFee = ATLCostants.USER_FEE_ATL;
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_TYPE);
        }

        payload.setAmount(payload.getAmount().multiply(atlFee.add(BigDecimal.ONE)));

        return paymentService.createCheckoutSession(payload, checkoutType);
    }

    @Transactional(rollbackFor = Exception.class)
    public Purchase payCheckoutSession(Long sessionId, Boolean useWallet) {
        CheckoutSession session = paymentService.getCheckoutSessionById(sessionId)
                .orElseThrow(() -> new BadRequestException(ErrorsString.CHECKOUT_SESSION_NOT_FOUND));

        PurchaseDetail detail = new PurchaseDetail();
        CheckoutPayload payload = session.getPayload();
        switch (session.getType()) {
            case TOURNAMENT_FEE:
                Tournament tournament = tournamentService.getTournamentById(payload.getTournamentId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.TOURNAMENT_NOT_FOUND));
                Player player = playerService.getPlayerByAccountId(session.getAccount().getId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.PLAYER_NOT_FOUND));
                detail.setTitle(tournament.getName());
                detail.setDescription(teamService
                        .getTeamByPlayerIdAndSportCategory(player.getPlayerId(),
                                tournament.getSport().getSportCategory())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.TEAM_NOT_FOUND)).getDescription());
                break;
            case LOBBY_BOOKING_PARTICIPATE:
            case LOBBY_BOOKING_CREATE:
            case PRIVATE_BOOKING:
                Field field = fieldService.getFieldById(payload.getFieldId())
                        .orElseThrow(() -> new BadRequestException(ErrorsString.FIELD_NOT_FOUND));
                detail.setTitle(field.getName());
                detail.setDescription(payload.getStartTime() + " - " + payload.getFinishTime());
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_TYPE);
        }

        Purchase purchase = paymentService.payCheckout(session, useWallet, detail);
        if (purchase == null) {
            return null;
        }

        BigDecimal amountToPay = payload.getAmount();
        Account account = session.getAccount();

        switch (session.getType()) {
            case TOURNAMENT_FEE:
                tournamentParticipantService.decreseTournamentParticipation_ToPay(payload.getTournamentId(),
                        account.getId(),
                        amountToPay);
                purchase.setObjectId(payload.getTournamentId());
                break;
            case LOBBY_BOOKING_PARTICIPATE:
                lobbyBookingService.createLobbyBookingParticipate(payload.getLobbyId(), payload.getPlayersPaid());
                purchase.setObjectId(payload.getLobbyId());
                break;
            case LOBBY_BOOKING_CREATE:
                Long lobbyId = lobbyBookingService
                        .createLobbyBookingFromCheckoutPayload(payload);
                purchase.setObjectId(lobbyId);
                break;
            case PRIVATE_BOOKING:
                Long privateBookingId = privateBookingService
                        .createPrivateBookingFromCheckoutPayload(payload);
                purchase.setObjectId(privateBookingId);
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }

        return purchase;
    }

    @Transactional(rollbackFor = Exception.class)
    public Account registerAccount(RegisterAccountRequest request) {
        Account account = accountService.registerAccount(request);
        if (account == null) {
            return null;
        }

        paymentService.createWalletForAccount(account);

        Optional<Account> inviterAccountOptional = accountService.getInviterAccount(request.getInviteCode());
        if (inviterAccountOptional.isEmpty()) {
            return account;
        }

        Account inviterAccount = inviterAccountOptional.get();

        TransactionGroup inviterGroup = new TransactionGroup();
        TransactionGroup invitedGroup = new TransactionGroup();

        paymentService.saveTransactionGroup(inviterGroup);
        paymentService.saveTransactionGroup(invitedGroup);

        BigDecimal reward = BigDecimal.valueOf(2.5); // TODO: configurare

        Transaction inviterTransaction = paymentService.createTransaction(inviterAccount, reward, reward,
                BigDecimal.ZERO,
                BigDecimal.ZERO, inviterGroup, TransactionType.INVITE, TransactionSource.INVITE_WALLET,
                TransactionStatus.PENDING, TransactionPendingReason.INVITE_REWARD);

        paymentService.createTransaction(account, reward, reward, BigDecimal.ZERO, BigDecimal.ZERO, invitedGroup,
                TransactionType.INVITE, TransactionSource.INVITE_WALLET, TransactionStatus.CONFIRMED,
                TransactionPendingReason.INVITE_REWARD);

        accountService.addInvite(request.getInviteCode(), account, inviterTransaction);

        return account;
    }
}
