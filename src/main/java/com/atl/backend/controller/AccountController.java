package com.atl.backend.controller;

import com.atl.backend.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;


    @GetMapping("/level")
    public ResponseEntity<Short> getAccountLevel() {
        // Ottieni l'accountId dal contesto di autenticazione
        return ResponseEntity.ok(accountService.getAccountLevel());
    }


}
