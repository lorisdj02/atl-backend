package com.atl.backend.service.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.model.Player;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.PaymentService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class PurchasesOrchestrator {
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private BookingService bookingService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallups(Integer tournamentId, Integer teamId) {
        tournamentService.deleteCallups(tournamentId, teamId);

        paymentService.refundPurchaseForCallups(tournamentId,
                tournamentService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId, teamId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void startBooking(Integer bookingId) {
        bookingService.startBooking(bookingId);
        paymentService.redeemBookingPurchasesByBookingId(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundLobbyBooking(Integer bookingId) {
        bookingService.refundLobbyBooking(bookingId);
        paymentService.refundLobbyCreationByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundPrivateBooking(Integer bookingId) {
        bookingService.refundPrivateBooking(bookingId);
        paymentService.refundPrivateBookingByBookingId(bookingId);
        bookingService.setBookingRefunded(bookingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leaveLobbyBooking(Integer bookingId) {
        Player activePlayer = AccountContext.getActivePlayer();
        bookingService.leaveLobbyBooking(bookingId);
        paymentService.refundLobbyCreationByBookingId(bookingId);
        paymentService.refundLobbyParticipationByBookingIdAndAccountId(bookingId, activePlayer.getAccount().getId());
    }
}
