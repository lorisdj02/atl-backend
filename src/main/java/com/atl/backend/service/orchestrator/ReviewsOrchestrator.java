package com.atl.backend.service.orchestrator;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Match;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeAssignment;
import com.atl.backend.model.Review;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.enums.ReviewStatus;
import com.atl.backend.model.enums.ReviewType;
import com.atl.backend.model.enums.TransactionPendingReason;
import com.atl.backend.model.enums.TransactionSource;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.accountServices.CoordinatorService;
import com.atl.backend.service.accountServices.ReviewService;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.paymentServices.PaymentService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class ReviewsOrchestrator {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private TournamentParticipantService tournamentParticipantService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private CoordinatorService coordinatorService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private MatchRefereeService matchRefereeService;

    @Transactional(rollbackFor = Exception.class)
    public void commitReview(ReviewDTO reviewDTO) {
        Review review = reviewService.commitReview(reviewDTO);
        if (review.getLinkedTransaction() != null) {
            paymentService.updateTransactionGroupStatus(review.getLinkedTransaction().getTransactionGroup().getId(),
                    TransactionStatus.CONFIRMED);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void startTournament(Integer tournamentId) {
        Tournament tournament = tournamentService.startTournament(tournamentId);
        List<Integer> participantIds = tournamentParticipantService.getCalluppedPlayerIdsByTournamentId(tournamentId);
        BigDecimal weight = BigDecimal.ZERO;

        Coordinator reviewedCoordinator = tournament.getCoordinator();
        for (Integer participantId : participantIds) {
            Callup callup = tournamentParticipantService.getCallupByPlayerIdAndTournamentId(participantId,
                    tournament.getId())
                    .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT));
            if (teamService.isPlayerCaptainOrViceCaptain(callup.getTeam().getId(), participantId)) {
                weight = BigDecimal.ONE;
            } else {
                weight = BigDecimal.valueOf(0.5);
            }
            Player reviewerPlayer = callup.getPlayer();
            reviewService.createReview(reviewerPlayer.getAccount(), reviewedCoordinator.getAccount(), tournament, null,
                    ReviewType.PLAYER_TO_COORDINATOR, weight,
                    null);
        }

        // TODO: devi fare anche finishTournament per creare le transazioni per i vincitori
    }

    @Transactional(rollbackFor = Exception.class)
    public void startMatch(Integer matchId) {
        Match match = matchService.startMatch(matchId);
        Tournament tournament = match.getTournament();
        Coordinator tournamentCoordinator = tournament.getCoordinator();
        List<RefereeAssignment> refereeAssignments = matchRefereeService.getRefereeAssignmentsForMatch(matchId);

        TransactionGroup transactionGroup;
        BigDecimal amount;
        Transaction linkedTransaction;
        for (RefereeAssignment assignment : refereeAssignments) {
            Referee matchReferee = assignment.getReferee();
            amount = matchRefereeService.getRefereeRolePayPerMatch(tournament.getSport().getId(),
                    assignment.getRole(), matchReferee.getLevel());
            // REFEREE TO COORDINATOR REVIEW CREATION AND PAYMENT TRANSACTION CREATION
            transactionGroup = new TransactionGroup();
            paymentService.saveTransactionGroup(transactionGroup);
            linkedTransaction = paymentService.createTransaction(matchReferee.getAccount(), amount, amount,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO, transactionGroup, TransactionType.FEE, TransactionSource.PERSONAL_WALLET,
                    TransactionStatus.PENDING, TransactionPendingReason.REVIEW_REQUIRED);
            reviewService.createReview(matchReferee.getAccount(), tournamentCoordinator.getAccount(), tournament,
                    match,
                    ReviewType.REFEREE_TO_COORDINATOR, BigDecimal.ONE,
                    linkedTransaction);

            amount = coordinatorService
                    .getCoordinatorPayPerMatchByCoordinatorId(tournamentCoordinator.getCoordinatorId());
            transactionGroup = new TransactionGroup();
            paymentService.saveTransactionGroup(transactionGroup);
            linkedTransaction = paymentService.createTransaction(tournamentCoordinator.getAccount(), amount, amount,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO, transactionGroup, TransactionType.FEE, TransactionSource.PERSONAL_WALLET,
                    TransactionStatus.PENDING, TransactionPendingReason.REVIEW_REQUIRED);
            reviewService.createReview(tournamentCoordinator.getAccount(), matchReferee.getAccount(), tournament,
                    match,
                    ReviewType.COORDINATOR_TO_REFEREE, BigDecimal.ONE,
                    linkedTransaction);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishMatch(Integer matchId) {
        Match match = matchService.finishMatch(matchId);
        Tournament tournament = match.getTournament();
        Coordinator tournamentCoordinator = tournament.getCoordinator();
        List<RefereeAssignment> refereeAssignments = matchRefereeService.getRefereeAssignmentsForMatch(matchId);

        Review refereeToCoordinatorReview;
        Review coordinatorToRefereeReview;
        for (RefereeAssignment assignment : refereeAssignments) {
            Referee referee = assignment.getReferee();
            refereeToCoordinatorReview = reviewService
                    .getReviewByReviewerAccountIdAndReviewedAccountIdAndTournamentIdAndMatchId(
                            referee.getAccount().getId(), tournamentCoordinator.getAccount().getId(),
                            tournament.getId(),
                            matchId);
            refereeToCoordinatorReview.setStatus(ReviewStatus.PENDING);
            reviewService.saveReview(refereeToCoordinatorReview);

            coordinatorToRefereeReview = reviewService
                    .getReviewByReviewerAccountIdAndReviewedAccountIdAndTournamentIdAndMatchId(
                            tournamentCoordinator.getAccount().getId(), referee.getAccount().getId(),
                            tournament.getId(),
                            matchId);
            coordinatorToRefereeReview.setStatus(ReviewStatus.PENDING);
            reviewService.saveReview(coordinatorToRefereeReview);
        }
    }
}
