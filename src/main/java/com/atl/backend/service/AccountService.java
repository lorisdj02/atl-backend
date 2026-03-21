package com.atl.backend.service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.enums.AccountStatus;
import com.atl.backend.repository.AccountRepository;
import com.atl.backend.repository.CoordinatorRepository;
import com.atl.backend.repository.OrganizationRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.RefereeRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final RefereeRepository refereeRepository;
    private final CoordinatorRepository coordinatorRepository;

    public AccountService(AccountRepository accountRepository, PlayerRepository playerRepository,
            OrganizationRepository organizationRepository, RefereeRepository refereeRepository,
            CoordinatorRepository coordinatorRepository) {
        this.accountRepository = accountRepository;
        this.playerRepository = playerRepository;
        this.organizationRepository = organizationRepository;
        this.refereeRepository = refereeRepository;
        this.coordinatorRepository = coordinatorRepository;
    }

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
        }
        return account;
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Integer id) {
        return accountRepository.findById(id).orElse(null);
    }

    public Account getAccountByFirebaseUid(String firebaseUid) {
        return accountRepository.findByFirebaseUid(firebaseUid).orElse(null);
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Coordinator saveCoordinator(Coordinator coordinator) {
        return coordinatorRepository.save(coordinator);
    }

    public Referee saveReferee(Referee referee) {
        return refereeRepository.save(referee);
    }

    public Organization saveOrganization(Organization organization) {
        return organizationRepository.save(organization);
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
}
