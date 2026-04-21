package com.atl.backend.service.paymentServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ATLCostants;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.CheckoutSessionDTO;
import com.atl.backend.dto.PurchaseDetail;
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
import com.atl.backend.model.Tournament;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.WalletBalance;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.CheckoutStatus;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.model.enums.TransactionPendingReason;
import com.atl.backend.model.enums.TransactionSource;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.repository.BookingRepository;
import com.atl.backend.repository.CheckoutSessionRepository;
import com.atl.backend.repository.FieldRepository;
import com.atl.backend.repository.LobbyBookingRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.repository.TransactionGroupRepository;
import com.atl.backend.repository.TransactionRepository;
import com.atl.backend.repository.WalletBalanceRepository;
import com.atl.backend.repository.WalletRepository;
import com.atl.backend.response.CheckoutResponse;

import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.model.Field;

@Service
public class PaymentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;
    @Autowired
    private TransactionGroupRepository transactionGroupRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private WalletBalanceRepository walletBalanceRepository;

    @Autowired
    private LobbyBookingRepository lobbyBookingRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public Wallet createWalletForAccount(Account account) {
        Wallet wallet = new Wallet();
        wallet.setAccount(account);
        return walletRepository.save(wallet);
    }

    private Player getExistingPlayerByPlayerId(Long playerId) {
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (optPlayer.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return optPlayer.get();
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccount_Id(accountId);
    }

    public Optional<Wallet> getWalletByAccountId(Long accountId) {
        return walletRepository.findByAccount_Id(accountId);
    }

    public Optional<CheckoutSession> getCheckoutSessionById(Long sessionId) {
        return checkoutSessionRepository.findById(sessionId);
    }

    public Optional<TransactionGroup> getTransactionGroupById(Long transactionGroupId) {
        return transactionGroupRepository.findById(transactionGroupId);
    }

    public void saveCheckoutSession(CheckoutSession session) {
        checkoutSessionRepository.save(session);
    }

    public BigDecimal getInviteBalanceByAccountId(Long accountId) {
        Optional<Wallet> walletOpt = walletRepository.findByAccount_Id(accountId);
        WalletBalance walletBalance = getWalletBalanceByAccountId(accountId);
        if (walletOpt.isPresent()) {
            return walletBalance.getInviteBalance();
        } else {
            // Handle case where wallet is not found, e.g., return zero or throw an
            // exception
            return BigDecimal.ZERO;
        }
    }

    public WalletBalance getWalletBalanceByAccountId(Long accountId) {
        return walletBalanceRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.WALLET_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutScreen(Long sessionId) {
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

        CheckoutPayload payload = session.getPayload();

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
                Long tournamentId = payload.getTournamentId();
                Tournament tournament = tournamentRepository.findById(tournamentId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
                tournamentName = tournament.getName();
                break;
            case LOBBY_BOOKING_PARTICIPATE:
                Long lobbyId = payload.getLobbyId();
                LobbyBooking booking = lobbyBookingRepository.findById(lobbyId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
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

                Long fieldId = payload.getFieldId();
                Field field = fieldRepository.findById(fieldId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.FIELD_NOT_FOUND));
                if (payload.getOwnerPlayerId() == null) {
                    throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
                }
                Player owner = getExistingPlayerByPlayerId(payload.getOwnerPlayerId());
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
                Long privateFieldId = payload.getFieldId();
                Field privateField = fieldRepository.findById(privateFieldId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.FIELD_NOT_FOUND));

                fieldName = privateField.getName();
                cityName = privateField.getCity().getName();
                fieldAddress = privateField.getAddress();
                startTime = payload.getStartTime();
                finishTime = payload.getFinishTime();
                break;
            default:
                throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_SESSION);
        }

        WalletBalance walletBalance = getWalletBalanceByAccountId(sessionId);

        return CheckoutResponse.builder()
                .checkoutSession(CheckoutSessionDTO.fromCheckoutSession(session))
                .wallet(WalletDTO.fromWallet(wallet, walletBalance.getPrizeBalance(),
                        walletBalance.getPersonalBalance(), walletBalance.getInviteBalance()))
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

    public void checkTransactionGroup(Long transactionGroupId) {
        Optional<TransactionGroup> optTransactionGroup = transactionGroupRepository.findById(transactionGroupId);
        if (optTransactionGroup.isEmpty()) {
            throw new PaymentRequiredException(ErrorsString.PAYMENT_REQUIRED);
        }
    }

    public List<Transaction> getTransactionsByTransactionGroupId(Long transactionGroupId) {
        return transactionRepository.findByTransactionGroup_Id(transactionGroupId);
    }

    public List<Purchase> getPurchasesByTournamentIdAndAccountId(Long tournamentId, Long accountId) {
        return purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.TOURNAMENT_FEE, tournamentId, accountId, PurchaseStatus.CONFIRMED);
    }

    public TransactionGroup saveTransactionGroup(TransactionGroup transactionGroup) {
        return transactionGroupRepository.save(transactionGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Account account, BigDecimal amount, BigDecimal netAmount, BigDecimal tax,
            BigDecimal fee, TransactionGroup transactionGroup,
            TransactionType type, TransactionSource source, TransactionStatus status,
            TransactionPendingReason pendingReason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (netAmount.add(tax).add(fee).compareTo(amount) != 0) {
            throw new BadRequestException(ErrorsString.INVALID_CHECKOUT_AMOUNT);
        }
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
        transaction.setPendingReason(pendingReason);
        return transactionRepository.save(transaction);
    }

    public TransactionGroup updateTransactionGroupStatus(Long transactionGroupId, TransactionStatus status) {
        TransactionGroup transactionGroup = transactionGroupRepository.findById(transactionGroupId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TRANSACTION_GROUP_NOT_FOUND));

        if (transactionGroup.getStatus() == TransactionStatus.FAILED) {
            throw new ConflictException(ErrorsString.TRANSACTION_FAILED);
        }
        transactionGroup.setStatus(status);
        transactionGroup.setUpdatedAt(LocalDateTime.now());
        List<Transaction> transactions = transactionRepository.findByTransactionGroup_Id(transactionGroupId);
        for (Transaction transaction : transactions) {
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        }
        return transactionGroupRepository.save(transactionGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public Purchase payCheckout(CheckoutSession session, Boolean useWallet, PurchaseDetail detail) {
        // TODO: verificare le fee e le tasse da applicare
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

        BigDecimal amountToPay = session.getAmount();
        BigDecimal originalAmountToPay = amountToPay;

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(ErrorsString.PAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }

        TransactionGroup transactionGroup = new TransactionGroup();
        saveTransactionGroup(transactionGroup);
        WalletBalance walletBalance = getWalletBalanceByAccountId(account.getId());
        if (useWallet && walletBalance.hasSomeBalance()) {
            // ORDINE DEI WALLET:
            // 1. Invite balance
            // 2. Prize balance
            // 3. Personal balance

            BigDecimal inviteBalance = walletBalance.getInviteBalance();
            BigDecimal prizeBalance = walletBalance.getPrizeBalance();
            BigDecimal personalBalance = walletBalance.getPersonalBalance();
            BigDecimal transactionAmount = BigDecimal.ZERO;
            Transaction inviteTransaction;
            Transaction prizeTransaction;
            Transaction personalTransaction;
            List<Transaction> savedTransactions = new ArrayList<>();

            if (walletBalance.hasInviteBalance()) {
                transactionAmount = inviteBalance.compareTo(amountToPay) >= 0 ? amountToPay : inviteBalance;
                inviteTransaction = createTransaction(account, transactionAmount,
                        transactionAmount,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.INVITE_WALLET,
                        TransactionStatus.PENDING, null);
                amountToPay = amountToPay.subtract(transactionAmount);
            }

            if (walletBalance.hasPrizeBalance()) {
                transactionAmount = prizeBalance.compareTo(amountToPay) >= 0 ? amountToPay : prizeBalance;
                prizeTransaction = createTransaction(account, transactionAmount,
                        transactionAmount,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.PRIZE_WALLET,
                        TransactionStatus.PENDING, null);
                amountToPay = amountToPay.subtract(transactionAmount);
            }

            if (walletBalance.hasPersonalBalance()) {
                transactionAmount = personalBalance.compareTo(amountToPay) >= 0 ? amountToPay : personalBalance;
                personalTransaction = createTransaction(account, transactionAmount,
                        transactionAmount,
                        BigDecimal.ZERO, BigDecimal.ZERO, transactionGroup, TransactionType.PAYMENT,
                        TransactionSource.PERSONAL_WALLET,
                        TransactionStatus.PENDING, null);
                amountToPay = amountToPay.subtract(transactionAmount);
            }
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

            if (false) { // TODO: Sostituire questa condizione con il controllo dell'esito del pagamento
                         // tramite Stripe
                transactionGroup.setStatus(TransactionStatus.FAILED);
                saveTransactionGroup(transactionGroup);
                return null;
            }
        }

        transactionGroup.setStatus(TransactionStatus.CONFIRMED);

        Purchase purchase = new Purchase();
        purchase.setAccount(account);
        purchase.setAmount(originalAmountToPay);
        purchase.setDetail(detail);
        purchase.setTransactionGroup(transactionGroup);
        purchase.setType(session.getType());
        return purchaseRepository.save(purchase);
    }

    @Transactional(readOnly = true)
    public Boolean isWalletEmptyByAccountId(Long accountId) {
        WalletBalance walletBalance = getWalletBalanceByAccountId(accountId);
        // Il wallet invite non viene considerato, dato che non è possibile prelevarlo o
        // trasferirlo, e quindi non è rilevante ai fini del controllo se il wallet è
        // vuoto o meno
        return !walletBalance.hasSomeBalance();
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckoutSession createCheckoutSession(CheckoutPayload payload, CheckoutType checkoutType) {
        Account account = AccountContext.getActiveAccount();

        CheckoutSession session = new CheckoutSession();
        session.setAccount(account);
        session.setType(checkoutType);
        session.setPayload(payload);
        session.setAmount(payload.getAmount());
        return checkoutSessionRepository.save(session);

    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrganizationByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.BOOKING_NOT_FOUND));
        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            Field field = booking.getField();
            Account organizationAccount = field.getOrganization().getAccount();
            BigDecimal amount = booking.calculatePrice();
            BigDecimal atlFee = ATLCostants.ORGANIZATION_FEE_ATL;
            if (organizationAccount.getVip() != null && organizationAccount.getVip()) {
                amount = amount.multiply(BigDecimal.valueOf(1).subtract(atlFee));
            }
            TransactionGroup transactionGroup = new TransactionGroup();
            saveTransactionGroup(transactionGroup);
            createTransaction(organizationAccount, amount, amount, BigDecimal.ZERO, BigDecimal.ZERO,
                    transactionGroup, TransactionType.FEE, TransactionSource.PERSONAL_WALLET,
                    TransactionStatus.PENDING, TransactionPendingReason.BOOKING_NOT_STARTED);
            updateTransactionGroupStatus(transactionGroup.getId(), TransactionStatus.CONFIRMED);
        }
    }

    @Transactional(readOnly = true)
    public Set<Long> getAccountIdsByTournamentId(Long tournamentId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndStatus(CheckoutType.TOURNAMENT_FEE,
                tournamentId, PurchaseStatus.CONFIRMED);
        Set<Long> accountIds = new HashSet<>();
        for (Purchase purchase : purchases) {
            accountIds.add(purchase.getAccount().getId());
        }
        return accountIds;
    }
}
