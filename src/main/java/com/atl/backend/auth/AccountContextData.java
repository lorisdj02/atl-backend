package com.atl.backend.auth;

import java.util.Optional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.service.accountServices.AccountService;
import com.atl.backend.service.accountServices.CoordinatorService;
import com.atl.backend.service.accountServices.OrganizationService;
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.accountServices.RefereeService;

import lombok.Data;

@Data
public class AccountContextData {
    private Long accountId;
    private AccountRole accountRole;
    private String firebaseUid;

    private Long playerId;
    private Long coordinatorId;
    private Long refereeId;
    private Long organizationId;

    private Account activeAccount;
    private Player activePlayer;
    private Referee activeReferee;
    private Coordinator activeCoordinator;
    private Organization activeOrganization;

    private AccountService accountService;

    private PlayerService playerService;

    private CoordinatorService coordinatorService;

    private RefereeService refereeService;

    private OrganizationService organizationService;

    public Player getActivePlayer() {
        if (accountRole != AccountRole.PLAYER) {
            throw new ForbiddenException(ErrorsString.PLAYER_ROLE_REQUIRED);
        }
        if (activePlayer == null) {
            Optional<Player> playerOpt = playerService.getPlayerByPlayerId(playerId);
            if (playerOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.PLAYER_NOT_FOUND);
            }
            activePlayer = playerOpt.get();
        }
        return activePlayer;
    }

    public Referee getActiveReferee() {
        if (accountRole != AccountRole.REFEREE) {
            throw new ForbiddenException(ErrorsString.REFEREE_ROLE_REQUIRED);
        }
        if (activeReferee == null) {
            Optional<Referee> refereeOpt = refereeService.getRefereeByRefereeId(refereeId);
            if (refereeOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.REFEREE_NOT_FOUND);
            }
            activeReferee = refereeOpt.get();
        }

        return activeReferee;
    }

    public Coordinator getActiveCoordinator() {
        if (accountRole != AccountRole.COORDINATOR) {
            throw new ForbiddenException(ErrorsString.COORDINATOR_ROLE_REQUIRED);
        }
        if (activeCoordinator == null) {
            Optional<Coordinator> coordinatorOpt = coordinatorService.getCoordinatorByCoordinatorId(coordinatorId);
            if (coordinatorOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.COORDINATOR_NOT_FOUND);
            }
            activeCoordinator = coordinatorOpt.get();
        }
        return activeCoordinator;
    }

    public Organization getActiveOrganization() {
        if (accountRole != AccountRole.ORGANIZATION) {
            throw new ForbiddenException(ErrorsString.ORGANIZATION_ROLE_REQUIRED);
        }
        if (activeOrganization == null) {
            Optional<Organization> organizationOpt = organizationService
                    .getOrganizationByOrganizationId(organizationId);
            if (organizationOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.ORGANIZATION_NOT_FOUND);
            }
            activeOrganization = organizationOpt.get();
        }

        return activeOrganization;
    }

    public Account getActiveAccount() {
        if (accountRole == AccountRole.GUEST) {
            throw new ForbiddenException(ErrorsString.AUTHENTICATION_REQUIRED);
        }

        if (activeAccount == null) {
            Optional<Account> accountOpt = accountService.getAccountById(accountId);
            if (accountOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.ACCOUNT_NOT_FOUND);
            }
            activeAccount = accountOpt.get();
        }
        return activeAccount;
    }
}
