package com.atl.backend.auth;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.Referee;
import com.atl.backend.model.enums.AccountRole;

import lombok.Data;

@Data
public class AccountContext {
    private static final ThreadLocal<AccountContextData> context = new ThreadLocal<>();

    public static void set(AccountContextData data) {
        context.set(data);
    }

    public static AccountContextData get() {
        AccountContextData data = context.get();
        if (data == null) {
            throw new ForbiddenException(ErrorsString.AUTHENTICATION_REQUIRED);
        }
        return data;
    }

    public static void clear() {
        context.remove();
    }

    public static Long getAccountId() {
        return get().getAccountId();
    }

    public static AccountRole getAccountRole() {
        return get().getAccountRole();
    }

    public static Long getPlayerId() {
        if (getAccountRole() != AccountRole.PLAYER) {
            throw new ForbiddenException(ErrorsString.PLAYER_ROLE_REQUIRED);
        }
        return get().getPlayerId();
    }

    public static Long getCoordinatorId() {
        if (getAccountRole() != AccountRole.COORDINATOR) {
            throw new ForbiddenException(ErrorsString.COORDINATOR_ROLE_REQUIRED);
        }
        return get().getCoordinatorId();
    }

    public static Long getRefereeId() {
        if (getAccountRole() != AccountRole.REFEREE) {
            throw new ForbiddenException(ErrorsString.REFEREE_ROLE_REQUIRED);
        }
        return get().getRefereeId();
    }

    public static Long getOrganizationId(){
            if(getAccountRole() != AccountRole.ORGANIZATION){
                throw new ForbiddenException(ErrorsString.ORGANIZATION_ROLE_REQUIRED);
            }
            return get().getOrganizationId();
    }

    public static Long getRoleId() {
        switch (getAccountRole()) {
            case PLAYER:
                return getPlayerId();
            case COORDINATOR:
                return getCoordinatorId();
            case REFEREE:
                return getRefereeId();
            case ORGANIZATION:
                return getOrganizationId();
            default:
                throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
    }

    public static Account getActiveAccount() {
        return get().getActiveAccount();
    }

    public static Player getActivePlayer() {
        return get().getActivePlayer();
    }

    public static Referee getActiveReferee() {
        return get().getActiveReferee();
    }

    public static Coordinator getActiveCoordinator() {
        return get().getActiveCoordinator();
    }

    public static Organization getActiveOrganization() {
        return get().getActiveOrganization();
    }

    public static boolean isGuest(){
        return get().getAccountRole() == AccountRole.GUEST;
    }

    public static boolean isPlayer() {
        return get().getAccountRole() == AccountRole.PLAYER;
    }

    public static boolean isCoordinator() {
        return get().getAccountRole() == AccountRole.COORDINATOR;
    }

    public static boolean isReferee() {
        return get().getAccountRole() == AccountRole.REFEREE;
    }

    public static boolean isOrganization() {
        return get().getAccountRole() == AccountRole.ORGANIZATION;
        }

    public static boolean isAdmin() {
        return get().getAccountRole() == AccountRole.ADMIN;
    }

}
