package com.atl.backend.service.accountServices;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.AccountDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.model.City;
import com.atl.backend.model.FriendInvite;
import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.Review;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.Wallet;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.model.enums.AccountStatus;
import com.atl.backend.model.enums.GrowthStepType;
import com.atl.backend.model.enums.ReviewStatus;
import com.atl.backend.repository.AccountRepository;
import com.atl.backend.repository.CityRepository;
import com.atl.backend.repository.FriendInviteRepository;
import com.atl.backend.repository.GrowthStepRepository;
import com.atl.backend.repository.ReviewRepository;
import com.atl.backend.repository.WalletRepository;
import com.atl.backend.request.RegisterAccountRequest;
import com.atl.backend.request.UpdateAccountInfosRequest;
import com.atl.backend.response.HasNotificationsResponse;
import com.atl.backend.response.LoginResponse;
import com.atl.backend.service.matchServices.MatchRefereeService;
import com.atl.backend.service.paymentServices.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private MatchRefereeService matchRefereeService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GrowthStepRepository growthStepRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FriendInviteRepository friendInviteRepository;
    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private PlayerService playerService;
    @Autowired
    private CoordinatorService coordinatorService;
    @Autowired
    private RefereeService refereeService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private WalletRepository walletRepository;

    public Optional<Account> getInviterAccount(String inviteCode) {
        // Adesso invite code = username, quindi cerco l'account con quel username
        Optional<Account> accountOptional = accountRepository.findByUsername(inviteCode);
        return accountOptional;
    }

    public Optional<FriendInvite> addInvite(String inviteCode, Account invitedAccount, Transaction inviterTransaction) {
        // Adesso invite code = username, quindi cerco l'account con quel username
        Optional<Account> inviterAccountOptional = getInviterAccount(inviteCode);
        if (inviterAccountOptional.isEmpty()) {
            return Optional.empty();
        }
        FriendInvite friendInvite = new FriendInvite();
        friendInvite.setInvitedAccount(invitedAccount);
        friendInvite.setInviterAccount(inviterAccountOptional.get());
        friendInvite.setInviterTransaction(inviterTransaction);
        friendInvite.setRedeemed(false);
        return Optional.of(friendInviteRepository.save(friendInvite));
    }

    public Optional<FriendInvite> redeemInvite(Account InvitedAccount) {
        Optional<FriendInvite> friendInviteOptional = friendInviteRepository
                .findByInvitedAccount_IdAndRedeemedFalse(InvitedAccount.getId());
        if (friendInviteOptional.isEmpty()) {
            return Optional.empty();
        }
        FriendInvite friendInvite = friendInviteOptional.get();
        // Se l'invito è già stato riscattato, non facciamo nulla
        if (friendInvite.getRedeemed() == true) {
            return Optional.empty();
        }
        friendInvite.setRedeemed(true);
        friendInviteRepository.save(friendInvite);
        return Optional.of(friendInvite);
    }

    @Transactional(rollbackFor = Exception.class)
    public Account registerAccount(RegisterAccountRequest request) {
        // TODO: aggiungere una sorta di controllo contro parole offensive

        if (request.getUsername() == null || request.getEmail() == null || request.getRole() == null) {
            throw new BadRequestException(ErrorsString.MISSING_REQUIRED_FIELDS);
        }
        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException(ErrorsString.USERNAME_ALREADY_EXISTS);
        }
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException(ErrorsString.EMAIL_ALREADY_EXISTS);
        }

        AccountRole role;
        try {
            role = AccountRole.valueOf(request.getRole());
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }

        if (role == AccountRole.ORGANIZATION || role == AccountRole.ADMIN) {
            throw new BadRequestException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setRole(role);
        account.setPhone(request.getPhone());
        try {
            account.setBirthdate(LocalDate.parse(request.getBirthdate()));
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_BIRTHDATE_FORMAT);
        }
        account.setImageUrl(request.getProfilePictureUrl());
        Account savedAccount = saveAccount(account);
        switch (role) {
            case PLAYER -> {
                playerService.registerPlayer(savedAccount);
            }
            case REFEREE -> {
                refereeService.registerReferee(savedAccount);
            }
            case COORDINATOR -> {
                coordinatorService.registerCoordinator(savedAccount);
            }
            default -> throw new BadRequestException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        return savedAccount;
    }

    public Account registerMockAccount(Account account, Optional<String> OrgName) {
        saveAccount(account);
        switch (account.getRole()) {
            case PLAYER -> {
                playerService.registerPlayer(account);
            }
            case ORGANIZATION -> {
                organizationService.registerOrganization(account, OrgName);
            }
            case REFEREE -> {
                refereeService.registerReferee(account);
            }
            case COORDINATOR -> {
                coordinatorService.registerCoordinator(account);
            }
            default -> throw new BadRequestException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        Wallet wallet = new Wallet();
        wallet.setAccount(account);
        walletRepository.save(wallet);
        return account;
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Long id) {
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

    public GrowthStep saveGrowthStep(GrowthStep growthStep) {
        return growthStepRepository.save(growthStep);
    }

    @Transactional(readOnly = true)
    public Short getAccountLevel() {
        Long accountId = AccountContext.getAccountId();
        Optional<Account> accountOptional = getAccountById(accountId);
        if (accountOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        Account account = accountOptional.get();
        switch (account.getRole()) {
            case COORDINATOR -> {
                return coordinatorService.getCoordinatorLevelByAccountId(accountId);
            }
            case REFEREE -> {
                return refereeService.getRefereeLevelByAccountId(accountId);
            }
            default -> {
                return Short.valueOf("0");
            }
        }
    }

    // Soft delete: set status to PURGED instead of deleting the record
    public void softDeleteAccount(Long id) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setStatus(AccountStatus.PURGED);
            account.setUsername("deleted_user_" + account.getId());
            account.setEmail("deleted_email_" + account.getId());
            account.setPhone("deleted_phone_" + account.getId());
            account.setName("deleted_name_" + account.getId());
            account.setSurname("deleted_surname_" + account.getId());
            account.setIsVip(false);
            account.setVipType(null);
            account.setVipStart(null);
            account.setVipEnd(null);
            account.setUpdatedAt(Instant.now());
            accountRepository.save(account);
        }
    }

    public void deleteAllAccounts() {
        playerService.deleteAllPlayers();
        organizationService.deleteAllOrganizations();
        refereeService.deleteAllReferees();
        coordinatorService.deleteAllCoordinators();
        accountRepository.deleteAll();
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
        playerService.deletePlayerByAccountId(id);
        organizationService.deleteOrganizationByAccountId(id);
        refereeService.deleteRefereeByAccountId(id);
        coordinatorService.deleteCoordinatorByAccountId(id);
    }

    public void deleteAllGrowthSteps() {
        growthStepRepository.deleteAll();
    }

    public List<GrowthStep> getGrowthStepsByType(GrowthStepType type) {
        return growthStepRepository.findByType(type);
    }

    public List<GrowthStep> getAllGrowthSteps() {
        return growthStepRepository.findAll();
    }

    public List<Review> getReviewsByReviewerAccountId(Long reviewerAccountId) {
        return reviewRepository.findByReviewerAccount_Id(reviewerAccountId);
    }

    public List<Review> getPendingReviewsByReviewerAccountId(Long reviewerAccountId) {
        return reviewRepository.findByReviewerAccount_IdAndStatus(reviewerAccountId, ReviewStatus.PENDING);
    }

    public String getReferralCodeByAccountId(Long accountId) {
        Optional<Account> accountOptional = getAccountById(accountId);
        if (accountOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        Account account = accountOptional.get();
        // TODO: implement referral code generation logic, for now we return the
        // username as referral code
        return account.getUsername();
    }

    public Integer getRedeemedInvitesByAccountId(Long accountId) {
        return friendInviteRepository.countByInviterAccount_IdAndRedeemedTrue(accountId);
    }

    public Integer getTotalInvitesByAccountId(Long accountId) {
        return friendInviteRepository.countByInviterAccount_Id(accountId);
    }

    @Transactional(readOnly = true)
    public LoginResponse handleLogin() {
        // AuthFilter fa già tutto il lavoro di autenticazione
        Account account = AccountContext.getActiveAccount();
        switch (account.getRole()) {
            case PLAYER:
                return playerService.handlePlayerLogin();
            default:
                return LoginResponse.builder()
                        .account(AccountDTO.fromAccount(account))
                        .roleId(AccountContext.getRoleId())
                        .build();
        }
    }

    @Transactional(readOnly = true)
    public HasNotificationsResponse hasNotifications() {
        Account account = AccountContext.getActiveAccount();
        // List<Review> reviews = getReviewsByReviewerAccountId(accountId);
        List<Review> pendingReviews = getPendingReviewsByReviewerAccountId(account.getId());
        // return !reviews.isEmpty() || !pendingReviews.isEmpty();

        return HasNotificationsResponse.builder()
                .hasUnreadNotifications(!pendingReviews.isEmpty())
                .isVip(account.getIsVip())
                .build();
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
        if(updateAccountInfosRequest.getPreferredCityId() != null){
            City preferredCity = cityRepository.findById(updateAccountInfosRequest.getPreferredCityId())
                    .orElseThrow(() -> new NotFoundException(ErrorsString.CITY_NOT_FOUND));
            account.setPreferredCity(preferredCity);
        }

        accountRepository.save(account);

        return account;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountAndRelatedData() {
        Account account = AccountContext.getActiveAccount();
        Long accountId = account.getId();
        paymentService.isWalletEmptyByAccountId(accountId);
        List<Review> reviews = getReviewsByReviewerAccountId(accountId);
        if (!reviews.isEmpty()) {
            throw new ConflictException(ErrorsString.ACCOUNT_HAS_REVIEWS);
        }
        switch (account.getRole()) {
            case PLAYER:
                playerService.deletePlayerAndRelatedData();
                break;
            case COORDINATOR:
                coordinatorService.deleteCoordinatorAndRelatedData();
                break;
            case REFEREE:
                if (matchRefereeService.hasRefereeActiveMatchesByRefereeId(AccountContext.getRefereeId())) {
                    throw new ConflictException(ErrorsString.REFEREE_HAS_ACTIVE_MATCHES);
                }
            default:
                break;
        }

        softDeleteAccount(accountId);
    }

}
