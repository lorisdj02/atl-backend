package com.atl.backend.service.orchestrator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.FriendInvite;
import com.atl.backend.model.Match;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeAssignment;
import com.atl.backend.model.Review;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.TransactionGroup;
import com.atl.backend.model.enums.FootballEventType;
import com.atl.backend.model.enums.ReviewStatus;
import com.atl.backend.model.enums.ReviewType;
import com.atl.backend.model.enums.TransactionPendingReason;
import com.atl.backend.model.enums.TransactionSource;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.enums.TransactionType;
import com.atl.backend.request.AssignTournamentPrizesRequest;
import com.atl.backend.service.accountServices.AccountService;
import com.atl.backend.service.accountServices.CoordinatorService;
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.accountServices.ReviewService;
import com.atl.backend.service.matchServices.FootballMatchEventService;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.paymentServices.PaymentService;
import com.atl.backend.service.teamServices.TeamAuthService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;

@Service
public class ReviewsOrchestrator {
        @Autowired
        private FootballMatchEventService footballMatchEventService;
        @Autowired
        private TeamAuthService teamAuthService;
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
        @Autowired
        private PlayerService playerService;
        @Autowired
        private AccountService accountService;

        @Transactional(rollbackFor = Exception.class)
        public Review commitReview(ReviewDTO reviewDTO) {
                Review review = reviewService.commitReview(reviewDTO);
                if (review.getLinkedTransaction() != null) {
                        paymentService.updateTransactionGroupStatus(
                                        review.getLinkedTransaction().getTransactionGroup().getId(),
                                        TransactionStatus.CONFIRMED);
                }
                return review;
        }

        @Transactional(rollbackFor = Exception.class)
        public void createReviewForTournament(Long tournamentId) {
                Tournament tournament = tournamentService.getTournamentById(tournamentId)
                                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
                List<Long> participantIds = tournamentParticipantService
                                .getCalluppedPlayerIdsByTournamentId(tournament.getId());
                BigDecimal weight = BigDecimal.ZERO;

                Coordinator reviewedCoordinator = tournament.getCoordinator();
                for (Long participantId : participantIds) {
                        Callup callup = tournamentParticipantService.getCallupByPlayerIdAndTournamentId(participantId,
                                        tournament.getId())
                                        .orElseThrow(() -> new NotFoundException(
                                                        ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT));
                        if (teamService.isPlayerCaptainOrViceCaptain(callup.getTeam().getId(), participantId)) {
                                weight = BigDecimal.ONE;
                        } else {
                                weight = BigDecimal.valueOf(0.5);
                        }
                        Player reviewerPlayer = callup.getPlayer();
                        reviewService.createReview(reviewerPlayer.getAccount(), reviewedCoordinator.getAccount(),
                                        tournament, null,
                                        ReviewType.PLAYER_TO_COORDINATOR, weight,
                                        null, tournament.getSport().getSportCategory());
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public void startTournament(Long tournamentId) {
                Tournament tournament = tournamentService.startTournament(tournamentId);
                createReviewForTournament(tournament.getId());

                Set<Long> playerIds = paymentService.getAccountIdsByTournamentId(tournamentId);

                for (Long playerId : playerIds) {
                        Player player = playerService.getPlayerByPlayerId(playerId)
                                        .orElseThrow(() -> new BadRequestException(ErrorsString.PLAYER_NOT_FOUND));
                        Optional<FriendInvite> friendInvite = accountService.redeemInvite(player.getAccount());
                        if (friendInvite.isPresent()) {
                                paymentService.updateTransactionGroupStatus(
                                                friendInvite.get().getInviterTransaction().getTransactionGroup()
                                                                .getId(),
                                                TransactionStatus.CONFIRMED);
                        }
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public void finishTournament(Long tournamentId) {
                Tournament tournament = tournamentService.finishTournament(tournamentId);
                Team winningTeam = tournamentParticipantService.getWinningTeamInTournament(tournamentId)
                                .orElseThrow(() -> new NotFoundException(ErrorsString.WINNING_TEAM_NOT_FOUND));
                Team finalistTeam = null;
                List<Long> winningPlayerIds = tournamentParticipantService
                                .getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId, winningTeam.getId());

                List<Long> finalistPlayerIds = new ArrayList<>();
                if (tournament.getPrize2() != null && tournament.getPrize2().compareTo(BigDecimal.ZERO) > 0) {
                        finalistTeam = tournamentParticipantService.getFinalistTeamInTournament(tournamentId)
                                        .orElseThrow(() -> new NotFoundException(ErrorsString.FINALIST_TEAM_NOT_FOUND));
                        finalistPlayerIds = tournamentParticipantService
                                        .getCalluppedPlayerIdsByTournamentIdAndTeamId(tournamentId,
                                                        finalistTeam.getId());
                }

                List<Long> callups = tournamentParticipantService.getCalluppedPlayerIdsByTournamentId(tournamentId);

                for (Long playerId : callups) {
                        if (!winningPlayerIds.contains(playerId)
                                        && (finalistTeam == null || !finalistPlayerIds.contains(playerId))) {
                                Player reviewerPlayer = playerService.getPlayerByPlayerId(playerId)
                                                .orElseThrow(() -> new BadRequestException(
                                                                ErrorsString.PLAYER_NOT_FOUND));
                                Review review = reviewService
                                                .getReviewByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(
                                                                reviewerPlayer.getAccount().getId(),
                                                                tournament.getCoordinator().getAccount().getId(),
                                                                tournamentId)
                                                .orElseThrow(() -> new NotFoundException(
                                                                ErrorsString.REVIEW_NOT_FOUND));
                                review.setStatus(ReviewStatus.PENDING);
                                reviewService.saveReview(review);
                        }
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public void assignTournamentPrizes(Long tournamentId, AssignTournamentPrizesRequest request) {

                if (tournamentId != null && !tournamentId.equals(request.getTournamentId())) {
                        throw new BadRequestException(ErrorsString.TOURNAMENT_ID_MISMATCH);
                }

                Tournament tournament = tournamentService.getTournamentById(request.getTournamentId())
                                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
                Team winningTeam = tournamentParticipantService.getWinningTeamInTournament(request.getTournamentId())
                                .orElseThrow(() -> new NotFoundException(ErrorsString.WINNING_TEAM_NOT_FOUND));
                Team finalistTeam = null;

                if (tournamentService.arePrizesAssignedInTournament(tournamentId)) {
                        throw new ConflictException(ErrorsString.CANNOT_ASSIGN_PRIZES);
                }

                if (tournament.getPrize2() != null && tournament.getPrize2().compareTo(BigDecimal.ZERO) > 0) {
                        finalistTeam = tournamentParticipantService
                                        .getFinalistTeamInTournament(request.getTournamentId())
                                        .orElseThrow(() -> new NotFoundException(ErrorsString.FINALIST_TEAM_NOT_FOUND));
                }

                Player requestingPlayer = AccountContext.getActivePlayer();

                Team consideredTeam = teamService.getTeamByPlayerIdAndSportCategory(requestingPlayer.getPlayerId(),
                                tournament.getSport().getSportCategory())
                                .orElseThrow(() -> new NotFoundException(ErrorsString.TEAM_NOT_FOUND));

                if (consideredTeam.getId() != winningTeam.getId()
                                && (finalistTeam == null || consideredTeam.getId() != finalistTeam.getId())) {
                        throw new ConflictException(ErrorsString.PLAYER_NOT_IN_WINNING_OR_FINALIST_TEAM);
                }

                teamAuthService.checkManageTeamInTournament(consideredTeam.getId(), tournament.getId());

                Boolean isWinningTeam = consideredTeam.getId() == winningTeam.getId();

                BigDecimal totalPrize = request.getPrizePerPlayer().values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (isWinningTeam && tournament.getPrize1().compareTo(totalPrize) < 0) {
                        throw new BadRequestException(ErrorsString.PRIZE_DISTRIBUTION_EXCEEDS_TOTAL_PRIZE);
                } else if (!isWinningTeam && tournament.getPrize2() != null
                                && tournament.getPrize2().compareTo(totalPrize) < 0) {
                        throw new BadRequestException(ErrorsString.PRIZE_DISTRIBUTION_EXCEEDS_TOTAL_PRIZE);
                }
                List<Long> consideredPlayerIds = isWinningTeam ? tournamentParticipantService
                                .getCalluppedPlayerIdsByTournamentIdAndTeamId(request.getTournamentId(),
                                                winningTeam.getId())
                                : tournamentParticipantService
                                                .getCalluppedPlayerIdsByTournamentIdAndTeamId(request.getTournamentId(),
                                                                finalistTeam != null ? finalistTeam.getId() : -1);
                if (!consideredPlayerIds.contains(requestingPlayer.getPlayerId())) {
                        throw new ConflictException(ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT);
                }
                for (Long winningPlayerId : consideredPlayerIds) {
                        BigDecimal prize = request.getPrizePerPlayer().get(winningPlayerId.toString());
                        // Create transaction for the winning player
                        TransactionGroup transactionGroup = new TransactionGroup();
                        Player player = playerService.getPlayerByPlayerId(winningPlayerId)
                                        .orElseThrow(() -> new NotFoundException(ErrorsString.PLAYER_NOT_FOUND));
                        paymentService.saveTransactionGroup(transactionGroup);
                        Transaction linkedTransaction = paymentService.createTransaction(player.getAccount(),
                                        prize,
                                        prize, BigDecimal.ZERO, BigDecimal.ZERO,
                                        transactionGroup, TransactionType.PRIZE, TransactionSource.PRIZE_WALLET,
                                        TransactionStatus.PENDING, TransactionPendingReason.REVIEW_REQUIRED);
                        Review review = reviewService
                                        .getReviewByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(
                                                        player.getAccount().getId(),
                                                        tournament.getCoordinator().getAccount().getId(),
                                                        request.getTournamentId())
                                        .orElseThrow(() -> new NotFoundException(ErrorsString.REVIEW_NOT_FOUND));
                        reviewService.updateReview(review.getId(), linkedTransaction, ReviewStatus.PENDING);
                }

                Tournament savedTournament = tournamentService.assignPrizeInTournament(request.getTournamentId(),
                                isWinningTeam, !isWinningTeam);
                if (savedTournament.arePrizesAssigned()) {
                        tournamentService.closeTournament(request.getTournamentId());
                        List<Long> participantIds = tournamentParticipantService
                                        .getCalluppedPlayerIdsByTournamentId(request.getTournamentId());

                        Coordinator reviewedCoordinator = tournament.getCoordinator();
                        for (Long participantId : participantIds) {
                                Callup callup = tournamentParticipantService
                                                .getCallupByPlayerIdAndTournamentId(participantId,
                                                                tournament.getId())
                                                .orElseThrow(() -> new NotFoundException(
                                                                ErrorsString.PLAYER_NOT_CALLED_UP_IN_TOURNAMENT));
                                Player reviewerPlayer = callup.getPlayer();
                                Optional<Review> review = reviewService
                                                .getReviewByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(
                                                                reviewerPlayer.getAccount().getId(),
                                                                reviewedCoordinator.getAccount().getId(),
                                                                tournament.getId());
                                if (review.isPresent()) {
                                        Review existingReview = review.get();
                                        if (existingReview.getLinkedTransaction() == null) {
                                                reviewService.updateReview(existingReview.getId(), null,
                                                                ReviewStatus.PENDING);
                                        }
                                }
                        }
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public void startMatchReviews(Long matchId) {
                // Match match = matchService.startMatch(matchId);
                Match match = matchService.getMatchById(matchId)
                                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                Tournament tournament = match.getTournament();
                Coordinator tournamentCoordinator = tournament.getCoordinator();
                List<RefereeAssignment> refereeAssignments = matchRefereeService.getRefereeAssignmentsForMatch(matchId);

                TransactionGroup transactionGroup;
                BigDecimal amount;
                Transaction linkedTransaction;
                Boolean isFinal = match.getGroupName() != null && match.getGroupName().equals("FI");
                for (RefereeAssignment assignment : refereeAssignments) {
                        Referee matchReferee = assignment.getReferee();
                        // CONTROLLA SE FINALE DUE VOLTE
                        amount = matchRefereeService.getRefereeRolePayPerMatch(tournament.getSport().getId(),
                                        assignment.getRole(), matchReferee.getLevel())
                                        .multiply(isFinal ? BigDecimal.valueOf(2) : BigDecimal.ONE);
                        // REFEREE TO COORDINATOR REVIEW CREATION AND PAYMENT TRANSACTION CREATION
                        transactionGroup = new TransactionGroup();
                        paymentService.saveTransactionGroup(transactionGroup);
                        linkedTransaction = paymentService.createTransaction(matchReferee.getAccount(), amount, amount,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO, transactionGroup, TransactionType.FEE,
                                        TransactionSource.PERSONAL_WALLET,
                                        TransactionStatus.PENDING, TransactionPendingReason.REVIEW_REQUIRED);
                        reviewService.createReview(matchReferee.getAccount(), tournamentCoordinator.getAccount(),
                                        tournament,
                                        match,
                                        ReviewType.REFEREE_TO_COORDINATOR, BigDecimal.ONE,
                                        linkedTransaction, match.getSportCategory());

                        // COORDINATOR TO REFEREE REVIEW CREATION AND PAYMENT TRANSACTION CREATION
                        amount = coordinatorService
                                        .getCoordinatorPayPerMatchByCoordinatorId(
                                                        tournamentCoordinator.getCoordinatorId())
                                        .multiply(isFinal ? BigDecimal.valueOf(2) : BigDecimal.ONE);
                        transactionGroup = new TransactionGroup();
                        paymentService.saveTransactionGroup(transactionGroup);
                        linkedTransaction = paymentService.createTransaction(tournamentCoordinator.getAccount(), amount,
                                        amount,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO, transactionGroup, TransactionType.FEE,
                                        TransactionSource.PERSONAL_WALLET,
                                        TransactionStatus.PENDING, TransactionPendingReason.REVIEW_REQUIRED);
                        reviewService.createReview(tournamentCoordinator.getAccount(), matchReferee.getAccount(),
                                        tournament,
                                        match,
                                        ReviewType.COORDINATOR_TO_REFEREE, BigDecimal.ONE,
                                        linkedTransaction, match.getSportCategory());
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public void finishMatchReviews(Long matchId) {
                // Match match = matchService.finishMatch(matchId);
                Match match = matchService.getMatchById(matchId)
                                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                Tournament tournament = match.getTournament();
                Coordinator tournamentCoordinator = tournament.getCoordinator();
                List<RefereeAssignment> refereeAssignments = matchRefereeService.getRefereeAssignmentsForMatch(matchId);

                Review refereeToCoordinatorReview;
                Review coordinatorToRefereeReview;
                for (RefereeAssignment assignment : refereeAssignments) {
                        Referee referee = assignment.getReferee();
                        refereeToCoordinatorReview = reviewService
                                        .getReviewByReviewerAccountIdAndReviewedAccountIdAndTournamentIdAndMatchId(
                                                        referee.getAccount().getId(),
                                                        tournamentCoordinator.getAccount().getId(),
                                                        tournament.getId(),
                                                        matchId);
                        reviewService.updateReview(refereeToCoordinatorReview.getId(), null, ReviewStatus.PENDING);

                        coordinatorToRefereeReview = reviewService
                                        .getReviewByReviewerAccountIdAndReviewedAccountIdAndTournamentIdAndMatchId(
                                                        tournamentCoordinator.getAccount().getId(),
                                                        referee.getAccount().getId(),
                                                        tournament.getId(),
                                                        matchId);
                        reviewService.updateReview(coordinatorToRefereeReview.getId(), null, ReviewStatus.PENDING);
                }
        }

        @Transactional(rollbackFor = Exception.class)
        public FootballMatchEvent insertFootballMatchEvent(Long matchId, FootballMatchEventDTO footballMatchEventDTO) {
                FootballMatchEvent event = footballMatchEventService.insertFootballMatchEvent(matchId,
                                footballMatchEventDTO);
                if (event.getType() == FootballEventType.START) {
                        startMatchReviews(matchId);
                } else if (event.getType() == FootballEventType.FINISH) {
                        finishMatchReviews(matchId);
                }
                return event;
        }
}
