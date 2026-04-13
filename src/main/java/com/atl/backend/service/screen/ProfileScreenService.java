package com.atl.backend.service.screen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.AccountDTO;
import com.atl.backend.dto.FootballTeamStatDTO;
import com.atl.backend.dto.GrowthStepDTO;
import com.atl.backend.dto.RefereeLicenseDTO;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.dto.SportDTO;
import com.atl.backend.dto.TransactionDTO;
import com.atl.backend.dto.WalletDTO;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeLicense;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Review;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.enums.GrowthStepType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.InviteFriendsResponse;
import com.atl.backend.response.MyGrowthResponse;
import com.atl.backend.response.PersonalPlayerStatsResponse;
import com.atl.backend.response.RefereeLicensesResponse;
import com.atl.backend.response.ReviewsResponse;
import com.atl.backend.response.WalletResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.BookingService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.paymentServices.PaymentService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.exception.*;

@Service
public class ProfileScreenService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SportService sportService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private TournamentParticipantService tournamentParticipantService;

    @Transactional(readOnly = true)
    public MyGrowthResponse getMyGrowthScreen() {
        Integer accountId = AccountContext.getAccountId();
        Optional<Account> optionalAccount = accountService.getAccountById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }

        Account account = optionalAccount.get();
        List<GrowthStep> growthSteps = new ArrayList<>();
        Short level = 0;
        Map<Short, Map<SportCategory, BigDecimal>> minRefereePayPerCategoryPerLevel = new HashMap<>();
        Map<Short, Map<SportCategory, BigDecimal>> maxRefereePayPerCategoryPerLevel = new HashMap<>();

        switch (account.getRole()) {
            case COORDINATOR:
                Optional<Coordinator> coordinator = accountService.getCoordinatorByAccountId(accountId);
                if (coordinator.isEmpty()) {
                    throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
                }

                growthSteps = accountService.getGrowthStepsByType(GrowthStepType.COORDINATOR);
                level = coordinator.get().getLevel();
                break;
            case REFEREE:
                Optional<Referee> referee = accountService.getRefereeByAccountId(accountId);
                if (referee.isEmpty()) {
                    throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
                }
                growthSteps = accountService.getGrowthStepsByType(GrowthStepType.REFEREE);
                level = referee.get().getLevel();
                // I livelli sono da 1 a 4
                for (Short i = 1; i <= 4; i++) {
                    Map<SportCategory, BigDecimal> minRefereePayPerCategory = new HashMap<>();
                    Map<SportCategory, BigDecimal> maxRefereePayPerCategory = new HashMap<>();
                    for (SportCategory sportCategory : SportCategory.values()) {
                        List<Sport> sports = sportService.getSportsBySportCategory(sportCategory);
                        for (Sport sport : sports) {
                            List<RefereeRole> refereeRoles = sportService
                                    .getRefereeRolesBySportIdAndLevel(sport.getId(), i);
                            for (RefereeRole refereeRole : refereeRoles) {
                                if (!minRefereePayPerCategory.containsKey(sportCategory) || refereeRole.getPayPerMatch()
                                        .compareTo(minRefereePayPerCategory.get(sportCategory)) < 0) {
                                    minRefereePayPerCategory.put(sportCategory, refereeRole.getPayPerMatch());
                                }
                                if (!maxRefereePayPerCategory.containsKey(sportCategory) || refereeRole.getPayPerMatch()
                                        .compareTo(maxRefereePayPerCategory.get(sportCategory)) > 0) {
                                    maxRefereePayPerCategory.put(sportCategory, refereeRole.getPayPerMatch());
                                }
                            }
                        }
                    }
                    minRefereePayPerCategoryPerLevel.put(i, minRefereePayPerCategory);
                    maxRefereePayPerCategoryPerLevel.put(i, maxRefereePayPerCategory);
                }
                break;
            default:
                throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        return MyGrowthResponse.builder().growthSteps(growthSteps.stream().map(GrowthStepDTO::fromGrowthStep)
                .toList()).level(level).minRefereePayPerCategoryPerLevel(minRefereePayPerCategoryPerLevel)
                .maxRefereePayPerCategoryPerLevel(maxRefereePayPerCategoryPerLevel).build();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletScreen() {
        Integer accountId = AccountContext.getAccountId();

        List<Transaction> transactions = paymentService.getTransactionsByAccountId(accountId);
        Optional<Wallet> wallet = paymentService.getWalletByAccountId(accountId);
        if (wallet.isEmpty()) {
            throw new NotFoundException(ErrorsString.WALLET_NOT_FOUND);
        }
        return WalletResponse.builder()
                .transactionsHistory(transactions.stream().map(TransactionDTO::fromTransaction).toList())
                .wallet(WalletDTO.fromWallet(wallet.get())).build();
    }

    @Transactional(readOnly = true)
    public RefereeLicensesResponse getRefereeLicensesScreen() {
        Referee referee = AccountContext.getActiveReferee();
        List<RefereeLicense> refereeLicenses = sportService.getRefereeLicensesByRefereeId(referee.getRefereeId());
        List<Sport> sports = sportService.getAllSports();
        return RefereeLicensesResponse.builder()
                .refereeLicenses(refereeLicenses.stream().map(RefereeLicenseDTO::fromRefereeLicense).toList())
                .sports(sports.stream().map(SportDTO::fromSport).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public PersonalPlayerStatsResponse getPersonalPlayerStatsScreen() {
        Player player = AccountContext.getActivePlayer();
        FootballTeamStat footballStats = teamService.getCumulativeFootballTeamStatsByPlayerId(player.getPlayerId());
        Map<String, Integer> numTournamentPerCategory = new HashMap<>();
        Map<String, Integer> numPrivateBookingPerCategory = new HashMap<>();
        Map<String, Integer> numLobbyJoinedPerCategory = new HashMap<>();
        for (SportCategory sportCategory : SportCategory.values()) {
            numTournamentPerCategory.put(sportCategory.name(),
                    tournamentParticipantService.countTournamentsByPlayerIdAndSportCategory(player.getPlayerId(), sportCategory));
            numPrivateBookingPerCategory.put(sportCategory.name(),
                    bookingService.countPrivateBookingsByPlayerIdAndSportCategory(player.getPlayerId(), sportCategory));
            numLobbyJoinedPerCategory.put(sportCategory.name(),
                    bookingService.countLobbyParticipationByPlayerIdAndSportCategory(player.getPlayerId(),
                            sportCategory));
        }
        return PersonalPlayerStatsResponse.builder()
                .footballStats(FootballTeamStatDTO.fromFootballTeamStat(footballStats))
                .numTournamentPerCategory(numTournamentPerCategory)
                .numPrivateBookingPerCategory(numPrivateBookingPerCategory)
                .numLobbyJoinedPerCategory(numLobbyJoinedPerCategory)
                .build();
    }

    @Transactional(readOnly = true)
    public ReviewsResponse getReviewsScreen() {
        Account account = AccountContext.getActiveAccount();
        List<Review> reviews = accountService.getReviewsByReviewerAccountId(account.getId());
        Map<Integer, Account> accountPerReview = new HashMap<>();
        Map<Integer, String> matchTeamsStringPerReview = new HashMap<>();
        for (Review review : reviews) {
            accountPerReview.put(review.getId(), review.getReviewedAccount());

            if (review.getMatch() != null) {
                switch (review.getSportCategory()) {
                    case FOOTBALL:
                        Optional<FootballMatch> footballMatch = matchService
                                .getFootballMatchById(review.getMatch().getId());
                        if (footballMatch.isEmpty()) {
                            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                        }
                        FootballMatch match = footballMatch.get();
                        matchTeamsStringPerReview.put(review.getId(),
                                match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName());
                        break;
                    default:
                        break;
                }
            }
        }
        return ReviewsResponse.builder()
                .reviews(reviews.stream().map(ReviewDTO::fromReview).toList())
                .accountPerReview(accountPerReview.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> AccountDTO.fromAccount(e.getValue()))))
                .matchTeamsStringPerReview(matchTeamsStringPerReview)
                .build();
    }

    @Transactional(readOnly = true)
    public InviteFriendsResponse getInviteFriendsScreen() {
        Account account = AccountContext.getActiveAccount();
        BigDecimal inviteBalance = paymentService.getInviteBalanceByAccountId(account.getId());
        String referrealCode = accountService.getReferralCodeByAccountId(account.getId());
        Integer numRedeemed = accountService.getRedeemedInvitesByAccountId(account.getId());
        Integer numInvites = accountService.getTotalInvitesByAccountId(account.getId());
        return InviteFriendsResponse.builder()
                .inviteBalance(inviteBalance)
                .referralCode(referrealCode)
                .numRedeemed(numRedeemed)
                .numInvites(numInvites)
                .build();
    }

}
