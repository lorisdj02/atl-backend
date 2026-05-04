package com.atl.backend.service.paymentServices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.Purchase;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;
import com.atl.backend.repository.PurchaseRepository;
import com.atl.backend.service.bookingServices.BookingService;

@Service
public class PaymentRedeemService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PaymentRefundService paymentRefundService;
    @Autowired
    private BookingService bookingService;

    private Purchase getExistingPurchase(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.PURCHASE_NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    public void redeemPurchase(Long purchaseId) {

        Purchase purchase = getExistingPurchase(purchaseId);
        if (purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw new ConflictException(ErrorsString.PURCHASE_NOT_REDEEMABLE);
        }
        purchase.setStatus(PurchaseStatus.REDEEMED);
        purchase.setRedeemedAt(Instant.now());
        purchase.setUpdatedAt(Instant.now());
        paymentRefundService.setTransactionsNotRefundableByTransactionGroupId(purchase.getTransactionGroup().getId());
        purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void redeemBookingPurchasesByBookingId(Long bookingId) {
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

    public void redeemTournamentFeePurchasesByTournamentId(Long tournamentId) {
        List<Purchase> purchases = purchaseRepository.findByTypeAndObjectIdAndStatus(
                CheckoutType.TOURNAMENT_FEE, tournamentId, PurchaseStatus.CONFIRMED);

        for (Purchase purchase : purchases) {
            redeemPurchase(purchase.getId());
        }
    }
}
