package com.atl.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.CheckoutSessionDTO;
import com.atl.backend.dto.WalletDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Account;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.Player;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.enums.CheckoutStatus;
import com.atl.backend.repository.CheckoutSessionRepository;
import com.atl.backend.repository.TransactionRepository;
import com.atl.backend.repository.WalletRepository;
import com.atl.backend.response.CheckoutResponse;
import com.atl.backend.model.Field;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentService {
    private final FieldService fieldService;
    private final TournamentService tournamentService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;
    @Autowired
    private BookingService bookingService;

    PaymentService(TournamentService tournamentService, FieldService fieldService) {
        this.tournamentService = tournamentService;
        this.fieldService = fieldService;
    }

    public List<Transaction> getTransactionsByAccountId(Integer accountId) {
        return transactionRepository.findByAccount_Id(accountId);
    }

    public Optional<Wallet> getWalletByAccountId(Integer accountId) {
        return walletRepository.findByAccount_Id(accountId);
    }

    public BigDecimal getInviteBalanceByAccountId(Integer accountId) {
        Optional<Wallet> walletOpt = walletRepository.findByAccount_Id(accountId);
        if (walletOpt.isPresent()) {
            return walletOpt.get().getInviteBalance();
        } else {
            // Handle case where wallet is not found, e.g., return zero or throw an
            // exception
            return BigDecimal.ZERO;
        }
    }

    public CheckoutResponse getCheckoutScreen(Integer sessionId) {
        Account account = AccountContext.getActiveAccount();
        Optional<CheckoutSession> optSession = checkoutSessionRepository.findById(sessionId);
        if (optSession.isEmpty()) {
            throw new NotFoundException(ErrorsString.CHECKOUT_SESSION_NOT_FOUND);
        }
        CheckoutSession session = optSession.get();
        if (session.getStatus() != CheckoutStatus.PENDING) {
            throw new ConflictException(ErrorsString.CHECKOUT_SESSION_IS_NOT_PENDING);
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(CheckoutStatus.EXPIRED);
            checkoutSessionRepository.save(session);
            throw new ConflictException(ErrorsString.CHECKOUT_SESSION_IS_NOT_PENDING);
        }

        if (!session.getAccount().equals(account)) {
            throw new UnauthorizedException(ErrorsString.ACCOUNT_NOT_FOUND);
        }

        Optional<Wallet> walletOpt = walletRepository.findByAccount_Id(account.getId());
        if (walletOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.WALLET_NOT_FOUND);
        }
        Wallet wallet = walletOpt.get();

        ObjectMapper mapper = new ObjectMapper();
        String payloadJson = session.getPayload();
        if (payloadJson == null) {
            throw new NotFoundException(ErrorsString.CHECKOUT_SESSION_NOT_FOUND);
        }
        CheckoutPayload payload = mapper.readValue(payloadJson, CheckoutPayload.class);

        String tournamentName = null;
        String bookingUsername = null;
        String cityName = null;
        String fieldName = null;
        String fieldAddress = null;
        String startTime = null;
        String finishTime = null;
        Integer maxPlayers = null;
        Integer playersToPay = null;
        Integer playersToFind = null;

        switch (session.getType()) {
            case TOURNAMENT_FEE:
                Integer tournamentId = payload.getTournamentId();
                tournamentName = tournamentService.getTournamentName(tournamentId);
                break;
            case LOBBY_BOOKING_PARTICIPATE:
                Integer lobbyId = payload.getLobbyId();
                Optional<LobbyBooking> optBooking = bookingService.getLobbyBookingByBookingId(lobbyId);
                if (optBooking.isEmpty()) {
                    throw new NotFoundException(ErrorsString.BOOKING_NOT_FOUND);
                }
                LobbyBooking booking = optBooking.get();
                fieldName = booking.getBooking().getField().getName();
                fieldAddress = booking.getBooking().getField().getCompleteAddress();
                cityName = booking.getBooking().getField().getCity().getName();
                bookingUsername = booking.getOwnerPlayer().getAccount().getUsername();
                startTime = booking.getBooking().getStartTime().toString();
                finishTime = booking.getBooking().getFinishTime().toString();
                maxPlayers = Integer.valueOf(booking.getTotalPlayers());
                playersToPay = payload.getPlayersPaid();
                if (playersToPay == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }
                break;
            case LOBBY_BOOKING_CREATE:

                Integer fieldId = payload.getFieldId();
                Optional<Field> optField = fieldService.getFieldById(fieldId);
                if (optField.isEmpty()) {
                    throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND);
                }
                if (payload.getOwnerPlayerId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }
                Optional<Player> optPlayer = accountService.getPlayerByPlayerId(payload.getOwnerPlayerId());
                if (optPlayer.isEmpty()) {
                    throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
                }
                Player owner = optPlayer.get();
                Field field = optField.get();
                fieldName = field.getName();
                fieldAddress = field.getAddress();
                cityName = field.getCity().getName();
                bookingUsername = owner.getAccount().getUsername();
                startTime = payload.getStartTime();
                finishTime = payload.getFinishTime();
                maxPlayers = payload.getMaxPlayers();
                playersToFind = payload.getPlayersToFind();
                playersToPay = (maxPlayers != null && playersToFind != null) ? maxPlayers - playersToFind : null;
                if (maxPlayers == null && playersToPay == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }
                break;
            case PRIVATE_BOOKING:
                Integer privateFieldId = payload.getFieldId();
                Optional<Field> optPrivateField = fieldService.getFieldById(privateFieldId);
                if (optPrivateField.isEmpty()) {
                    throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND);
                }

                Field privateField = optPrivateField.get();
                fieldName = privateField.getName();
                cityName = privateField.getCity().getName();
                fieldAddress = privateField.getAddress();
                startTime = payload.getStartTime();
                finishTime = payload.getFinishTime();
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }
        return CheckoutResponse.builder()
                .checkoutSession(CheckoutSessionDTO.fromCheckoutSession(session))
                .wallet(WalletDTO.fromWallet(wallet))
                .tournamentName(tournamentName)
                .bookingUsername(bookingUsername)
                .cityName(cityName)
                .fieldName(fieldName)
                .fieldAddress(fieldAddress)
                .startTime(startTime)
                .finishTime(finishTime)
                .maxPlayers(maxPlayers)
                .playersToPay(playersToPay)
                .build();
    }
}
