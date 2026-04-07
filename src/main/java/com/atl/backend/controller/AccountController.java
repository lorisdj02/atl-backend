package com.atl.backend.controller;

import com.atl.backend.dto.AccountDTO;
import com.atl.backend.model.Account;
import com.atl.backend.request.UpdateAccountInfosRequest;
import com.atl.backend.response.LoginResponse;
import com.atl.backend.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account/level")
    public ResponseEntity<Short> getAccountLevel() {
        // Ottieni l'accountId dal contesto di autenticazione
        return ResponseEntity.ok(accountService.getAccountLevel());
    }

    @PostMapping("/account/login")
    public ResponseEntity<LoginResponse> handleLogin() {
        return ResponseEntity.ok(accountService.handleLogin());
    }

    @GetMapping("/account/has-notifications")
    public ResponseEntity<Boolean> hasNotifications() {
        return ResponseEntity.ok(accountService.hasNotifications());
    }

    @PatchMapping("/account/update-infos")
    public ResponseEntity<AccountDTO> updateAccountInfos(@RequestBody UpdateAccountInfosRequest updateAccountInfos) {
        Account response = accountService.updateAccountInfos(updateAccountInfos);
        return ResponseEntity.ok(AccountDTO.fromAccount(response));
    }
}
