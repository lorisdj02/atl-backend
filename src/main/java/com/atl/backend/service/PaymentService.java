package com.atl.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.atl.backend.exception.PaymentRequiredException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Booking;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.Player;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.CheckoutStatus;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.model.enums.TransactionSource;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.repository.CheckoutSessionRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.repository.TransactionGroupRepository;
import com.atl.backend.repository.TransactionRepository;
import com.atl.backend.repository.WalletRepository;
import com.atl.backend.response.CheckoutResponse;
import com.atl.backend.service.tournamentServices.TournamentService;

import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.model.Field;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentService {

    @Autowired
    private FieldService fieldService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private TransactionGroupRepository transactionGroupRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Player getExistingPlayerByPlayerId(Integer playerId) {
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (optPlayer.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return optPlayer.get();
    }

    public List<Transaction> getTransactionsByAccountId(Integer accountId) {
        return transactionRepository.findByAccount_Id(accountId);
    }

    public Optional<Wallet> getWalletByAccountId(Integer accountId) {
        return walletRepository.findByAccount_Id(accountId);
    }

    public Optional<CheckoutSession> getCheckoutSessionById(Integer sessionId) {
        return checkoutSessionRepository.findById(sessionId);
    }

    public Optional<TransactionGroup> getTransactionGroupById(Integer transactionGroupId) {
        return transactionGroupRepository.findById(transactionGroupId);
    }

    public void saveCheckoutSession(CheckoutSession session) {
        checkoutSessionRepository.save(session);
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

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutScreen(Integer sessionId) {
        // TODO: Verifica
        Account account = AccountContext.getActiveAccount();
        Optional<CheckoutSession> optSession = getCheckoutSessionById(sessionId);
        if (optSession.isEmpty()) {
            throw new NotFoundException(ErrorsString.CHECKOUT_SESSION_NOT_FOUND);
        }
        CheckoutSession session = optSession.get();
        if (session.getStatus() != CheckoutStatus.PENDING) {
            throw new ConflictException(ErrorsString.CHECKOUT_SESSION_IS_NOT_PENDING);
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(CheckoutStatus.EXPIRED);
            saveCheckoutSession(session);
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
        Short totalPlayers = null;
        Short playersToPay = null;
        Short playersToFind = null;

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
                totalPlayers = booking.getTotalPlayers();
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
                Player owner = getExistingPlayerByPlayerId(payload.getOwnerPlayerId());
                Field field = optField.get();
                fieldName = field.getName();
                fieldAddress = field.getAddress();
                cityName = field.getCity().getName();
                bookingUsername = owner.getAccount().getUsername();
                startTime = payload.getStartTime();
                finishTime = payload.getFinishTime();
                totalPlayers = payload.getTotalPlayers();
                playersToFind = payload.getPlayersToFind();
                playersToPay = (totalPlayers != null && playersToFind != null) ? (short) (totalPlayers - playersToFind)
                        : 1;
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
                .totalPlayers(totalPlayers)
                .playersToPay(playersToPay)
                .build();
    }

    public void checkTransactionGroup(Integer transactionGroupId) {
        Optional<TransactionGroup> optTransactionGroup = transactionGroupRepository.findById(transactionGroupId);
        if (optTransactionGroup.isEmpty()) {
            throw new PaymentRequiredException(ErrorsString.PAYMENT_REQUIRED);
        }
    }

    public List<Transaction> getTransactionsByTransactionGroupId(Integer transactionGroupId) {
        return transactionRepository.findByTransactionGroup_Id(transactionGroupId);
    }

    public List<Purchase> getPurchasesByTournamentIdAndAccountId(Integer tournamentId, Integer accountId) {
        return purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.TOURNAMENT_FEE, tournamentId, accountId, PurchaseStatus.CONFIRMED);
    }

    public TransactionGroup saveTransactionGroup(TransactionGroup transactionGroup) {
        return transactionGroupRepository.save(transactionGroup);
    }

    public Purchase savePurchase(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPurchaseForCallups(Integer tournamentId, List<Integer> calluppedAccountIds) {
        for (Integer accountId : calluppedAccountIds) {
            List<Purchase> purchases = getPurchasesByTournamentIdAndAccountId(tournamentId, accountId);
            for (Purchase purchase : purchases) {
                if (purchase.getStatus() == PurchaseStatus.CONFIRMED) {
                    refundPurchase(purchase.getId());
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Account account, BigDecimal amount, BigDecimal netAmount, BigDecimal tax,
            BigDecimal fee, TransactionGroup transactionGroup,
            TransactionType type, TransactionSource source, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setNetAmount(netAmount);
        transaction.setTax(tax);
        transaction.setFee(fee);
        transaction.setTransactionGroup(transactionGroup);
        transaction.setType(type);
        transaction.setSource(source);
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Transactional(rollbackFor = Exception.class)
    public Purchase payCheckout(Integer sessionId, Boolean useWallet) {
        // TODO: verificare le fee e le tasse da applicare
        Optional<CheckoutSession> optSession = getCheckoutSessionById(sessionId);
        if (optSession.isEmpty()) {
            throw new NotFoundException(ErrorsString.CHECKOUT_SESSION_NOT_FOUND);
        }
        CheckoutSession session = optSession.get();
        if (session.getStatus() != CheckoutStatus.PENDING) {
            throw new ConflictException(ErrorsString.CHECKOUT_SESSION_IS_NOT_PENDING);
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(CheckoutStatus.EXPIRED);
            saveCheckoutSession(session);
            throw new ConflictException(ErrorsString.CHECKOUT_SESSION_IS_NOT_PENDING);
        }

        Account account = session.getAccount();
        if (!account.equals(AccountContext.getActiveAccount())) {
            throw new UnauthorizedException(ErrorsString.ACCOUNT_NOT_FOUND);
        }

        Optional<Wallet> walletOpt = walletRepository.findByAccount_Id(account.getId());
        if (walletOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.WALLET_NOT_FOUND);
        }
        Wallet wallet = walletOpt.get();

        BigDecimal amountToPay = session.getAmount();
        BigDecimal originalAmountToPay = amountToPay;

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(ErrorsString.PAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }

        TransactionGroup transactionGroup = new TransactionGroup();
        saveTransactionGroup(transactionGroup);

        if (useWallet) {
            // ORDINE DEI WALLET:
            // 1. Invite balance
            // 2. Prize balance
            // 3. Personal balance
            BigDecimal inviteBalance = wallet.getInviteBalance();
            BigDecimal prizeBalance = wallet.getPrizeBalance();
            BigDecimal personalBalance = wallet.getPersonalBalance();

            if (inviteBalance.compareTo(amountToPay) >= 0) {
                wallet.setInviteBalance(inviteBalance.subtract(amountToPay));
                amountToPay = BigDecimal.ZERO;
            } else if (inviteBalance.compareTo(BigDecimal.ZERO) > 0) {
                wallet.setInviteBalance(BigDecimal.ZERO);
                amountToPay = amountToPay.subtract(inviteBalance);
                Transaction inviteTransaction = createTransaction(account, inviteBalance, inviteBalance,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.INVITE_WALLET,
                        TransactionStatus.CONFIRMED);
            }

            if (prizeBalance.compareTo(amountToPay) >= 0) {
                wallet.setPrizeBalance(prizeBalance.subtract(amountToPay));
                amountToPay = BigDecimal.ZERO;
            } else {
                wallet.setPrizeBalance(BigDecimal.ZERO);
                amountToPay = amountToPay.subtract(prizeBalance);
                Transaction prizeTransaction = createTransaction(account, prizeBalance, prizeBalance,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.PRIZE_WALLET,
                        TransactionStatus.CONFIRMED);
            }

            if (personalBalance.compareTo(amountToPay) >= 0) {
                wallet.setPersonalBalance(personalBalance.subtract(amountToPay));
                amountToPay = BigDecimal.ZERO;
            } else {
                wallet.setPersonalBalance(BigDecimal.ZERO);
                amountToPay = amountToPay.subtract(personalBalance);
                Transaction personalTransaction = createTransaction(account, personalBalance, personalBalance,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.PERSONAL_WALLET,
                        TransactionStatus.CONFIRMED);
            }
            walletRepository.save(wallet);
        }

        if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
            // TODO: integrare STRIPE, gestire qui l'errore senza lanciare un'eccezione, in
            // modo da permettere comunque la creazione dell'acquisto e dei transaction
            // group, che saranno poi confermati o rimborsati a seconda dell'esito del
            // pagamento. Tutto all'interno di un try catch, in modo da poter gestire
            // eventuali errori di comunicazione con Stripe o di pagamento fallito, e in
            // modo da poter comunque creare l'acquisto e i transaction group, che saranno
            // poi confermati o rimborsati a seconda dell'esito del pagamento.
            amountToPay = BigDecimal.ZERO; // Rimuovere questa riga quando si integra Stripe
        }

        Purchase purchase = new Purchase();
        purchase.setAccount(account);
        purchase.setAmount(originalAmountToPay);
        String payloadJson = session.getPayload();
        CheckoutPayload payload;
        // Parse the JSON payload into the map
        try {
            ObjectMapper mapper = new ObjectMapper();
            payload = mapper.readValue(payloadJson, CheckoutPayload.class);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }
        purchase.setDetail(payloadJson);
        purchase.setTransactionGroup(transactionGroup);
        purchase.setDetail(payloadJson);
        purchase.setType(session.getType());
        switch (session.getType()) {
            // TODO: da rivedere... tournament_fee e lobby_booking_partecipate forse non
            // funzionano
            case TOURNAMENT_FEE:
                tournamentService.decreseTournamentParticipation_ToPay(payload.getTournamentId(), account.getId(),
                        amountToPay);
                purchase.setObjectId(payload.getTournamentId());
                break;
            case LOBBY_BOOKING_PARTICIPATE:
                bookingService.createLobbyBookingParticipate(payload.getLobbyId(), payload.getPlayersPaid());
                purchase.setObjectId(payload.getLobbyId());
                break;
            case LOBBY_BOOKING_CREATE:
                Integer lobbyId = bookingService
                        .createLobbyBookingFromCheckoutPayload(payload);
                purchase.setObjectId(lobbyId);
                break;
            case PRIVATE_BOOKING:
                Integer privateBookingId = bookingService
                        .createPrivateBookingFromCheckoutPayload(payload);
                purchase.setObjectId(privateBookingId);
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }

        return purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPurchase(Integer purchaseId) {
        Optional<Purchase> optPurchase = purchaseRepository.findById(purchaseId);
        if (optPurchase.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        Purchase purchase = optPurchase.get();

        if (purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw new ConflictException(ErrorsString.PURCHASE_NOT_REFUNDABLE);
        }
        Wallet wallet = walletRepository.findByAccount_Id(purchase.getAccount().getId())
                .orElseThrow(() -> new NotFoundException(ErrorsString.WALLET_NOT_FOUND));
        TransactionGroup transactionGroup = purchase.getTransactionGroup();
        List<Transaction> transactions = transactionRepository.findByTransactionGroup_Id(transactionGroup.getId());
        Integer numberOfRefundedTransactions = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getStatus() == TransactionStatus.CONFIRMED && transaction.getRefundable()) {

                switch (transaction.getSource()) {
                    case INVITE_WALLET:
                        wallet.setInviteBalance(wallet.getInviteBalance().add(transaction.getNetAmount()));
                        break;
                    case PRIZE_WALLET:
                        wallet.setPrizeBalance(wallet.getPrizeBalance().add(transaction.getNetAmount()));
                        break;
                    default:
                        wallet.setPersonalBalance(wallet.getPersonalBalance().add(transaction.getNetAmount()));
                        break;
                }
                Transaction refundTransaction = new Transaction();
                refundTransaction.setAccount(transaction.getAccount());
                refundTransaction.setAmount(transaction.getNetAmount());
                refundTransaction.setNetAmount(transaction.getNetAmount());
                refundTransaction.setTax(BigDecimal.ZERO);
                refundTransaction.setFee(BigDecimal.ZERO);
                refundTransaction.setTransactionGroup(transactionGroup);
                refundTransaction.setType(TransactionType.REFUND);
                refundTransaction.setSource(transaction.getSource());
                refundTransaction.setStatus(TransactionStatus.CONFIRMED);
                refundTransaction.setRelatedTransaction(transaction);
                transactionRepository.save(refundTransaction);

                transaction.setStatus(TransactionStatus.REFUNDED);
                transactionRepository.save(transaction);
                numberOfRefundedTransactions++;
            }
        }
        walletRepository.save(wallet);
        if (numberOfRefundedTransactions == 0) {
            throw new PaymentRequiredException(ErrorsString.PURCHASE_NOT_REFUNDABLE);
        } else if (numberOfRefundedTransactions < transactions.size()) {
            transactionGroup.setStatus(TransactionStatus.PARTIAL_REFUND);
            purchase.setStatus(PurchaseStatus.PARTIAL_REFUND);
        } else {
            transactionGroup.setStatus(TransactionStatus.REFUNDED);
            purchase.setStatus(PurchaseStatus.REFUNDED);
        }
        transactionGroupRepository.save(transactionGroup);
        purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void redeemPurchase(Integer purchaseId) {
        Optional<Purchase> optPurchase = purchaseRepository.findById(purchaseId);
        if (optPurchase.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        Purchase purchase = optPurchase.get();
        if (purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw new ConflictException(ErrorsString.PURCHASE_NOT_REDEEMABLE);
        }
        purchase.setStatus(PurchaseStatus.REDEEMED);
        setTransactionsNotRefundableByTransactionGroupId(purchase.getTransactionGroup().getId());
        purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setTransactionsNotRefundableByTransactionGroupId(Integer transactionGroupId) {
        checkTransactionGroup(transactionGroupId);
        List<Transaction> transactions = transactionRepository.findByTransactionGroup_Id(transactionGroupId);
        for (Transaction transaction : transactions) {
            transaction.setRefundable(false);
            transactionRepository.save(transaction);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyParticipationByBookingIdAndAccountId(Integer bookingId, Integer accountId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.LOBBY_BOOKING_PARTICIPATE, bookingId, accountId, PurchaseStatus.CONFIRMED);
        if (purchases.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        for (Purchase purchase : purchases) {
            refundPurchase(purchase.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyCreationByBookingId(Integer bookingId) {
        List<Purchase> purchases = new ArrayList<>();

        List<Purchase> lobbyBookingCreate = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.LOBBY_BOOKING_CREATE, bookingId, null, PurchaseStatus.CONFIRMED);

        if (lobbyBookingCreate.isEmpty() || lobbyBookingCreate.size() > 1) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        purchases.add(lobbyBookingCreate.get(0));

        List<Purchase> lobbyBookingParticipate = purchaseRepository.findByTypeAndObjectIdAndStatus(
                CheckoutType.LOBBY_BOOKING_PARTICIPATE, bookingId, PurchaseStatus.CONFIRMED);
        purchases.addAll(lobbyBookingParticipate);

        if (purchases.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }

        for (Purchase purchase : purchases) {
            refundPurchase(purchase.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyParticipationByAccountId(Integer bookingId, Integer accountId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.LOBBY_BOOKING_PARTICIPATE, bookingId, accountId, PurchaseStatus.CONFIRMED);
        if (purchases.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        for (Purchase purchase : purchases) {
            refundPurchase(purchase.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBookingByBookingId(Integer bookingId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndStatus(
                CheckoutType.PRIVATE_BOOKING, bookingId, PurchaseStatus.CONFIRMED);
        if (purchases.isEmpty()) {
            throw new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND);
        }
        for (Purchase purchase : purchases) {
            refundPurchase(purchase.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void redeemBookingPurchasesByBookingId(Integer bookingId) {
        List<Purchase> purchases = new ArrayList<>();
        Booking booking = bookingService.getBookingByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new ConflictException(ErrorsString.PURCHASE_NOT_REDEEMABLE);
        }

        switch (booking.getBookingType()) {
            case LOBBY:
                purchases.addAll(purchaseRepository.findByTypeAndObjectIdAndStatus(
                        CheckoutType.LOBBY_BOOKING_PARTICIPATE, bookingId, PurchaseStatus.CONFIRMED));
                purchases.addAll(purchaseRepository.findByTypeAndObjectIdAndStatus(
                        CheckoutType.LOBBY_BOOKING_CREATE, bookingId, PurchaseStatus.CONFIRMED));
                break;
            case PRIVATE:
                purchases = purchaseRepository.findByTypeAndObjectIdAndStatus(
                        CheckoutType.PRIVATE_BOOKING, bookingId, PurchaseStatus.CONFIRMED);
                break;
            case MATCH:
                // Non ci sono acquisti da riscattare per le partite, dato che non si paga
                // direttamente la partita, ma si pagano i tornei o le prenotazioni dei campi
                break;
        }

        // Se è vuoto, va bene, vuol dire che non c'è nulla da riscattare, non lancio
        // un'eccezione
        if (purchases.isEmpty()) {
            return;
        }

        for (Purchase purchase : purchases) {
            redeemPurchase(purchase.getId());
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

    @Transactional(readOnly = true)
    public Boolean isWalletEmptyByAccountId(Integer accountId) {
        Optional<Wallet> walletOpt = walletRepository.findByAccount_Id(accountId);
        if (walletOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.WALLET_NOT_FOUND);
        }
        Wallet wallet = walletOpt.get();
        // Il wallet invite non viene considerato, dato che non è possibile prelevarlo o
        // trasferirlo, e quindi non è rilevante ai fini del controllo se il wallet è
        // vuoto o meno
        return wallet.getPrizeBalance().compareTo(BigDecimal.ZERO) == 0
                && wallet.getPersonalBalance().compareTo(BigDecimal.ZERO) == 0;
    }
}
