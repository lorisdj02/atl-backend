package com.atl.backend.controller;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}/level")
    public ResponseEntity<Short> getAccountLevel(@PathVariable Integer accountId) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        return ResponseEntity.ok(accountService.getAccountLevel(accountId));
    }


}
