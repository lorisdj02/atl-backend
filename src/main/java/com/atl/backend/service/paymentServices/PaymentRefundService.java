package com.atl.backend.service.paymentServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.PaymentRequiredException;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.repository.TransactionGroupRepository;
import com.atl.backend.repository.TransactionRepository;
import com.atl.backend.repository.WalletRepository;

@Service
public class PaymentRefundService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionGroupRepository transactionGroupRepository;

    private Transaction craftRefundTransaction(Transaction originalTransaction) {
        Transaction refundTransaction = new Transaction();
        refundTransaction.setAccount(originalTransaction.getAccount());
        refundTransaction.setAmount(originalTransaction.getNetAmount());
        refundTransaction.setNetAmount(originalTransaction.getNetAmount());
        refundTransaction.setTax(BigDecimal.ZERO);
        refundTransaction.setFee(BigDecimal.ZERO);
        refundTransaction.setTransactionGroup(originalTransaction.getTransactionGroup());
        refundTransaction.setType(TransactionType.REFUND);
        refundTransaction.setSource(originalTransaction.getSource());
        refundTransaction.setStatus(TransactionStatus.CONFIRMED);
        refundTransaction.setRelatedTransaction(originalTransaction);
        return refundTransaction;
    }

    private Purchase getExistingPurchase(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND));
    }

    private void checkTransactionGroup(Long transactionGroupId) {
        Optional<TransactionGroup> optTransactionGroup = transactionGroupRepository.findById(transactionGroupId);
        if (optTransactionGroup.isEmpty()) {
            throw new PaymentRequiredException(ErrorsString.PAYMENT_REQUIRED);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public BigDecimal refundPurchase(Long purchaseId) {
        Purchase purchase = getExistingPurchase(purchaseId);

        if (purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw new ConflictException(ErrorsString.PURCHASE_NOT_REFUNDABLE);
        }
        Wallet wallet = walletRepository.findByAccount_Id(purchase.getAccount().getId())
                .orElseThrow(() -> new NotFoundException(ErrorsString.WALLET_NOT_FOUND));
        TransactionGroup transactionGroup = purchase.getTransactionGroup();
        List<Transaction> transactions = transactionRepository.findByTransactionGroup_Id(transactionGroup.getId());
        Integer numberOfRefundedTransactions = 0;
        BigDecimal totalRefundedAmount = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getStatus() == TransactionStatus.CONFIRMED && transaction.getRefundable()) {

                if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ConflictException(ErrorsString.NEGATIVE_AMOUNT_TRANSACTION);
                }

                Transaction refundTransaction = craftRefundTransaction(transaction);
                transactionRepository.save(refundTransaction);

                totalRefundedAmount = totalRefundedAmount.add(transaction.getNetAmount());
                transaction.setStatus(TransactionStatus.REFUNDED);
                transaction.setRefundable(false);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                numberOfRefundedTransactions++;
            }
        }
        wallet.setUpdatedAt(LocalDateTime.now());
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
        purchase.setUpdatedAt(LocalDateTime.now());
        transactionGroup.setUpdatedAt(LocalDateTime.now());
        transactionGroupRepository.save(transactionGroup);
        purchaseRepository.save(purchase);
        return totalRefundedAmount;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPurchaseForPlayerIds(Long tournamentId, List<Long> calluppedAccountIds) {
        for (Long accountId : calluppedAccountIds) {
            List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                    CheckoutType.TOURNAMENT_FEE, tournamentId, accountId, PurchaseStatus.CONFIRMED);
            for (Purchase purchase : purchases) {
                refundPurchase(purchase.getId());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyParticipationByBookingIdAndAccountId(Long bookingId, Long accountId) {
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
    public void refundLobbyCreationByBookingId(Long bookingId) {
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
    public void refundLobbyParticipationByAccountId(Long bookingId, Long accountId) {
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
    public void refundPrivateBookingByBookingId(Long bookingId) {
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
    public void setTransactionsNotRefundableByTransactionGroupId(Long transactionGroupId) {
        checkTransactionGroup(transactionGroupId);
        List<Transaction> transactions = transactionRepository.findByTransactionGroup_Id(transactionGroupId);
        for (Transaction transaction : transactions) {
            transaction.setRefundable(false);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
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

    public BigDecimal refundTournamentFeeByTournamentIdAndAccountId(Long tournamentId, Long accountId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndAccount_IdAndStatus(
                CheckoutType.TOURNAMENT_FEE, tournamentId, accountId, PurchaseStatus.CONFIRMED);
        BigDecimal totalRefundedAmount = BigDecimal.ZERO;
        for (Purchase purchase : purchases) {
            totalRefundedAmount = totalRefundedAmount.add(refundPurchase(purchase.getId()));
        }
        return totalRefundedAmount;
    }
}
