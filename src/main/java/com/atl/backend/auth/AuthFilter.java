package com.atl.backend.auth;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//TODO: implementare la logica reale. Adesso token = Username
@Component
public class AuthFilter extends OncePerRequestFilter {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private CoordinatorService coordinatorService;
    @Autowired
    private RefereeService refereeService;
    @Autowired
    private OrganizationService organizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Ottieni l'header di autenticazione
            String authHeader = request.getHeader("Authorization");
            //System.out.println("[DEBUG AUTH] Auth Header: " + authHeader); // Debug: stampa l'header di autenticazione
            AccountContextData contextData = new AccountContextData();
            contextData.setAccountService(accountService);
            contextData.setPlayerService(playerService);
            contextData.setCoordinatorService(coordinatorService);
            contextData.setRefereeService(refereeService);
            contextData.setOrganizationService(organizationService);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String firebaseUid = validateAndExtractUid(token);

                // Recupera l'account associato al Firebase UID, attento al metodo
                // TODO validateAndExtractUid che deve restituire il Firebase UID
                Optional<Account> accountOpt = accountService.getAccountByUsername(firebaseUid);
                Account account;
                if (accountOpt.isEmpty()) {
                    Optional<Account> accountOpt2 = accountService.getAccountByEmail(firebaseUid);
                    if (accountOpt2.isEmpty()) {
                        throw new ForbiddenException(ErrorsString.AUTHENTICATION_REQUIRED);
                    }
                    account = accountOpt2.get();
                } else {
                    account = accountOpt.get();
                }

                contextData.setAccountId(account.getId());
                contextData.setAccountRole(account.getRole());
                contextData.setFirebaseUid(firebaseUid);

                switch (account.getRole()) {
                    case PLAYER -> {
                        Optional<Player> playerOpt = playerService.getPlayerByAccountId(account.getId());
                        if (playerOpt.isEmpty()) {
                            throw new ForbiddenException(ErrorsString.PLAYER_NOT_FOUND);
                        }
                        Player player = playerOpt.get();
                        contextData.setPlayerId(player.getPlayerId());
                    }
                    case COORDINATOR -> {
                        Optional<Coordinator> coordinatorOpt = coordinatorService
                                .getCoordinatorByAccountId(account.getId());
                        if (coordinatorOpt.isEmpty()) {
                            throw new ForbiddenException(ErrorsString.COORDINATOR_NOT_FOUND);
                        }
                        Coordinator coordinator = coordinatorOpt.get();
                        contextData.setCoordinatorId(coordinator.getCoordinatorId());
                    }
                    case REFEREE -> {
                        Optional<Referee> refereeOpt = refereeService.getRefereeByAccountId(account.getId());
                        if (refereeOpt.isEmpty()) {
                            throw new ForbiddenException(ErrorsString.REFEREE_NOT_FOUND);
                        }
                        Referee referee = refereeOpt.get();
                        contextData.setRefereeId(referee.getRefereeId());
                    }
                    case ORGANIZATION -> {
                        Optional<Organization> organizationOpt = organizationService
                                .getOrganizationByAccountId(account.getId());
                        if (organizationOpt.isEmpty()) {
                            throw new ForbiddenException(ErrorsString.ORGANIZATION_NOT_FOUND);
                        }
                        Organization organization = organizationOpt.get();
                        contextData.setOrganizationId(organization.getOrganizationId());
                    }
                    default -> {
                    }
                }

            } else {
                // Se non c'è header di autenticazione, consideriamo l'utente come non
                // autenticato
                contextData.setAccountRole(AccountRole.GUEST);
            }

            AccountContext.set(contextData);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
            response.getWriter().close();
            return;
        } finally {
            AccountContext.clear();
        }

    }

    private String validateAndExtractUid(String token) {
        // Implementa la logica per validare il token e estrarre il Firebase UID
        // Ad esempio, puoi decodificare il token e restituire il Firebase UID associato
        return token; // In questo esempio, restituiamo il token, che sarà uguale all'username
    }

    public Account getAccountFromToken(String token) {
        // Implementa la logica per ottenere l'account associato al token
        // Ad esempio, puoi decodificare il token e recuperare l'account dal database
        return null; // Sostituisci con la logica reale
    }

}
