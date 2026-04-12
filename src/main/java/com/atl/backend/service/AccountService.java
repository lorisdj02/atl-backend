package com.atl.backend.service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.AccountDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Review;
import com.atl.backend.model.Team;
import com.atl.backend.model.enums.AccountStatus;
import com.atl.backend.model.enums.GrowthStepType;
import com.atl.backend.model.enums.ReviewStatus;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.AccountRepository;
import com.atl.backend.repository.CoordinatorRepository;
import com.atl.backend.repository.FriendInviteRepository;
import com.atl.backend.repository.GrowthStepRepository;
import com.atl.backend.repository.OrganizationRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.RefereeRepository;
import com.atl.backend.repository.ReviewRepository;
import com.atl.backend.request.UpdateAccountInfosRequest;
import com.atl.backend.response.LoginResponse;

import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    @Lazy
    private PaymentService paymentService;

    @Autowired
    @Lazy
    private TeamService teamService;

    @Autowired
    @Lazy
    private BookingService bookingService;

    @Autowired
    @Lazy
    private TournamentService tournamentService;

    @Autowired
    @Lazy
    private MatchService matchService;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private CoordinatorRepository coordinatorRepository;
    @Autowired
    private GrowthStepRepository growthStepRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FriendInviteRepository friendInviteRepository;
    

    public Account registerAccount(Account account, Optional<String> OrgName) {
        saveAccount(account);
        switch (account.getRole()) {
            case PLAYER -> {
                Player player = new Player();
                player.setAccount(account);
                player.setPoints(0);
                savePlayer(player);
            }
            case ORGANIZATION -> {
                if (OrgName.isEmpty())
                    throw new IllegalArgumentException(ErrorsString.ORGANIZATION_NEED_ORGANIZATION_NAME);
                Organization organization = new Organization();
                organization.setAccount(account);
                organization.setOrgName(OrgName.get());
                saveOrganization(organization);
            }
            case REFEREE -> {
                Referee referee = new Referee();
                referee.setAccount(account);
                saveReferee(referee);
            }
            case COORDINATOR -> {
                Coordinator coordinator = new Coordinator();
                coordinator.setAccount(account);
                saveCoordinator(coordinator);
            }
            default -> throw new BadRequestException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        return account;
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public Referee saveReferee(Referee referee) {
        return refereeRepository.save(referee);
    }

    public Organization saveOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Integer id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> getAccountByFirebaseUid(String firebaseUid) {
        return accountRepository.findByFirebaseUid(firebaseUid);
    }

    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Coordinator saveCoordinator(Coordinator coordinator) {
        return coordinatorRepository.save(coordinator);
    }

    public GrowthStep saveGrowthStep(GrowthStep growthStep) {
        return growthStepRepository.save(growthStep);
    }

    public boolean existsByPlayerId(Integer playerId) {
        return playerRepository.existsByPlayerId(playerId);
    }

    public boolean existsByOrganizationId(Integer orgId) {
        return organizationRepository.existsByOrganizationId(orgId);
    }

    public boolean existsByRefereeId(Integer refereeId) {
        return refereeRepository.existsByRefereeId(refereeId);
    }

    public boolean existsByCoordinatorId(Integer coordinatorId) {
        return coordinatorRepository.existsByCoordinatorId(coordinatorId);
    }

    @Transactional(readOnly = true)
    public Short getAccountLevel() {
        Integer accountId = AccountContext.getAccountId();
        Optional<Account> accountOptional = getAccountById(accountId);
        if (accountOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        Account account = accountOptional.get();
        switch (account.getRole()) {
            case COORDINATOR -> {
                Optional<Coordinator> coordinatorOptional = getCoordinatorByAccountId(accountId);
                if (coordinatorOptional.isEmpty()) {
                    throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
                }
                return coordinatorOptional.get().getLevel();
            }
            case REFEREE -> {
                Optional<Referee> refereeOptional = getRefereeByAccountId(accountId);
                if (refereeOptional.isEmpty()) {
                    throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
                }
                return refereeOptional.get().getLevel();
            }
            default -> throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
    }

    // Soft delete: set status to PURGED instead of deleting the record
    public void softDeleteAccount(Integer id) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setStatus(AccountStatus.PURGED);
            account.setUsername("deleted_user_" + account.getId());
            account.setEmail("deleted_email_" + account.getId());
            account.setPhone("deleted_phone_" + account.getId());
            account.setName("deleted_name_" + account.getId());
            account.setSurname("deleted_surname_" + account.getId());
            account.setVip(false);
            account.setVipType(null);
            account.setVipStart(null);
            account.setVipEnd(null);
            account.setRole(null);
            accountRepository.save(account);
        }
    }

    public void deleteAllAccounts() {
        playerRepository.deleteAll();
        organizationRepository.deleteAll();
        refereeRepository.deleteAll();
        coordinatorRepository.deleteAll();
        accountRepository.deleteAll();
    }

    public void deleteAccount(Integer id) {
        accountRepository.deleteById(id);
        playerRepository.deleteByAccount_Id(id);
        organizationRepository.deleteByAccount_Id(id);
        refereeRepository.deleteByAccount_Id(id);
        coordinatorRepository.deleteByAccount_Id(id);
    }

    public void deleteAllGrowthSteps() {
        growthStepRepository.deleteAll();
    }

    public Optional<Player> getPlayerByPlayerId(Integer playerId) {
        return playerRepository.findById(playerId);
    }

    public Optional<Player> getPlayerByAccountId(Integer accountId) {
        return playerRepository.findByAccount_Id(accountId);
    }

    public Optional<Organization> getOrganizationByAccountId(Integer accountId) {
        return organizationRepository.findByAccount_Id(accountId);
    }

    public Optional<Organization> getOrganizationByOrganizationId(Integer orgId) {
        return organizationRepository.findByOrganizationId(orgId);
    }

    public Optional<Referee> getRefereeByAccountId(Integer accountId) {
        return refereeRepository.findByAccount_Id(accountId);
    }

    public Optional<Referee> getRefereeByRefereeId(Integer refereeId) {
        return refereeRepository.findById(refereeId);
    }

    public Optional<Coordinator> getCoordinatorByAccountId(Integer accountId) {
        return coordinatorRepository.findByAccount_Id(accountId);
    }

    public Optional<Coordinator> getCoordinatorByCoordinatorId(Integer coordinatorId) {
        return coordinatorRepository.findById(coordinatorId);
    }

    public List<GrowthStep> getGrowthStepsByType(GrowthStepType type) {
        return growthStepRepository.findByType(type);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public List<Referee> getAllReferees() {
        return refereeRepository.findAll();
    }

    public List<Coordinator> getAllCoordinators() {
        return coordinatorRepository.findAll();
    }

    public List<GrowthStep> getAllGrowthSteps() {
        return growthStepRepository.findAll();
    }

    public void checkPlayerExists(Integer playerId) {
        if (!existsByPlayerId(playerId)) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
    }

    public void checkOrganizationExists(Integer orgId) {
        if (!existsByOrganizationId(orgId)) {
            throw new NotFoundException(ErrorsString.ORGANIZATION_NOT_FOUND);
        }
    }

    public void checkRefereeExists(Integer refereeId) {
        if (!existsByRefereeId(refereeId)) {
            throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
        }
    }

    public void checkCoordinatorExists(Integer coordinatorId) {
        if (!existsByCoordinatorId(coordinatorId)) {
            throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
        }
    }

    public List<Review> getReviewsByReviewerAccountId(Integer reviewerAccountId) {
        return reviewRepository.findByReviewerAccount_Id(reviewerAccountId);
    }

    public List<Review> getPendingReviewsByReviewerAccountId(Integer reviewerAccountId) {
        return reviewRepository.findByReviewerAccount_IdAndStatus(reviewerAccountId, ReviewStatus.PENDING);
    }

    public String getReferralCodeByAccountId(Integer accountId) {
        Optional<Account> accountOptional = getAccountById(accountId);
        if (accountOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        Account account = accountOptional.get();
        // TODO: implement referral code generation logic, for now we return the
        // username as referral code
        return account.getUsername();
    }

    public BigDecimal getCoordinatorPayPerMatchByCoordinatorId(Integer coordinatorId) {
        Optional<Coordinator> coordinatorOptional = getCoordinatorByCoordinatorId(coordinatorId);
        if (coordinatorOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
        }
        Coordinator coordinator = coordinatorOptional.get();
        Short level = coordinator.getLevel();
        if (level == null) {
            level = 1; // Default level if not set
        }

        BigDecimal pay = growthStepRepository.findCoordinatorPayPerMatchByLevel(level);
        return pay;
    }

    public Short getMedianRefereeLevel() {
        List<Short> levels = refereeRepository.findAllLevelsOrdered();
        if (levels == null || levels.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
        }
        int n = levels.size();
        int index = (n - 1) / 2; // odd -> middle; even -> lower middle
        return levels.get(index);
    }

    public Integer getRedeemedInvitesByAccountId(Integer accountId) {
        return friendInviteRepository.countByInviterAccount_IdAndRedeemedTrue(accountId);
    }

    public Integer getTotalInvitesByAccountId(Integer accountId) {
        return friendInviteRepository.countByInviterAccount_Id(accountId);
    }

    @Transactional(readOnly = true)
    public LoginResponse handleLogin() {
        // AuthFilter fa già tutto il lavoro di autenticazione
        Account account = AccountContext.getActiveAccount();
        switch (account.getRole()) {
            case PLAYER:
                Optional<Team> teamOpt = teamService.getTeamByPlayerIdAndSportCategory(AccountContext.getPlayerId(),
                        account.getFavoriteSportCategory());
                return LoginResponse.builder()
                        .account(AccountDTO.fromAccount(account))
                        .roleId(AccountContext.getPlayerId())
                        .teamId(teamOpt.isPresent() ? teamOpt.get().getId() : null)
                        .build();
            default:
                return LoginResponse.builder()
                        .account(AccountDTO.fromAccount(account))
                        .roleId(AccountContext.getRoleId())
                        .build();
        }
    }

    @Transactional(readOnly = true)
    public boolean hasNotifications() {
        Integer accountId = AccountContext.getAccountId();
        List<Review> reviews = getReviewsByReviewerAccountId(accountId);
        List<Review> pendingReviews = getPendingReviewsByReviewerAccountId(accountId);
        return !reviews.isEmpty() || !pendingReviews.isEmpty();
    }

    @Transactional(rollbackFor = Exception.class)
    public Account updateAccountInfos(UpdateAccountInfosRequest updateAccountInfosRequest) {
        Account account = AccountContext.getActiveAccount();
        if (updateAccountInfosRequest.getUsername() != null) {
            account.setUsername(updateAccountInfosRequest.getUsername());
        }
        if (updateAccountInfosRequest.getPhone() != null) {
            account.setPhone(updateAccountInfosRequest.getPhone());
        }
        if (updateAccountInfosRequest.getBirthdate() != null) {
            LocalDate birthdate;
            try {
                birthdate = LocalDate.parse(updateAccountInfosRequest.getBirthdate());
            } catch (Exception e) {
                throw new BadRequestException(ErrorsString.INVALID_BIRTHDATE_FORMAT);
            }
            account.setBirthdate(birthdate);
        }
        if (updateAccountInfosRequest.getImageUrl() != null) {
            account.setImageUrl(updateAccountInfosRequest.getImageUrl());
        }
        accountRepository.save(account);

        return account;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountAndRelatedData() {
        Account account = AccountContext.getActiveAccount();
        Integer accountId = account.getId();
        paymentService.isWalletEmptyByAccountId(accountId);
        List<Review> reviews = getReviewsByReviewerAccountId(accountId);
        if (!reviews.isEmpty()) {
            throw new ConflictException(ErrorsString.ACCOUNT_HAS_REVIEWS);
        }
        switch (account.getRole()) {
            case PLAYER:
                teamService.leaveAllTeams();
                bookingService.leaveAllBookings();
                break;
            case COORDINATOR:
                Coordinator coordinator = AccountContext.getActiveCoordinator();
                if(tournamentService.hasCoordinatorActiveTournamentsByCoordinatorId(coordinator.getCoordinatorId())){
                    throw new ConflictException(ErrorsString.COORDINATOR_HAS_ACTIVE_TOURNAMENTS);
                }
                break;
            case REFEREE:
                if(matchService.hasRefereeActiveMatchesByRefereeId(AccountContext.getRefereeId())){
                    throw new ConflictException(ErrorsString.REFEREE_HAS_ACTIVE_MATCHES);
                }
            default:
                break;
        }

        softDeleteAccount(accountId);
    }
}
