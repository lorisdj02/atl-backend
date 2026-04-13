package com.atl.backend.service.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.model.Player;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.paymentServices.PaymentRedeemService;
import com.atl.backend.service.paymentServices.PaymentRefundService;
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
    private BookingService bookingService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallups(Integer tournamentId, Integer teamId) {
        tournamentParticipantService.deleteCallupsCheck(tournamentId, teamId);

        paymentRefundService.refundPurchaseForCallups(tournamentId,
                tournamentParticipantService.getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId, teamId));

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
        paymentRefundService.refundLobbyCreationByBookingId(bookingId);
        paymentRefundService.refundLobbyParticipationByBookingIdAndAccountId(bookingId, activePlayer.getAccount().getId());
    }
}
