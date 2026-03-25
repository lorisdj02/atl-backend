package com.atl.backend.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.service.AccountService;

import lombok.Data;

@Data
public class AccountContextData {
    private Integer accountId;
    private AccountRole accountRole;
    private String firebaseUid;

    private Integer playerId;
    private Integer coordinatorId;
    private Integer refereeId;
    private Integer organizationId;

    private Player activePlayer;
    private Referee activeReferee;
    private Coordinator activeCoordinator;
    private Organization activeOrganization;

    @Autowired
    private AccountService accountService;

    public Player getActivePlayer() {
        if (accountRole != AccountRole.PLAYER) {
            throw new ForbiddenException(ErrorsString.PLAYER_ROLE_REQUIRED);
        }
        if (activePlayer == null) {
            Optional<Player> playerOpt = accountService.getPlayerByPlayerId(playerId);
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
            Optional<Referee> refereeOpt = accountService.getRefereeByRefereeId(refereeId);
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
            Optional<Coordinator> coordinatorOpt = accountService.getCoordinatorByCoordinatorId(coordinatorId);
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
            Optional<Organization> organizationOpt = accountService.getOrganizationByOrganizationId(organizationId);
            if (organizationOpt.isEmpty()) {
                throw new ForbiddenException(ErrorsString.ORGANIZATION_NOT_FOUND);
            }
            activeOrganization = organizationOpt.get();
        }

        return activeOrganization;
    }
}
