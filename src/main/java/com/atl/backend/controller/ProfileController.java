package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.response.MyGrowthResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.ProfileScreenService;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileScreenService profileScreenService;
    private final AccountService accountService;
    
    public ProfileController(ProfileScreenService profileScreenService, AccountService accountService) {
        this.profileScreenService = profileScreenService;
        this.accountService = accountService;
    }

    @RequestMapping("/{accountId}/growth-screen")
    public ResponseEntity<MyGrowthResponse> getMyGrowthResponse(@PathVariable Integer accountId){
        Optional<Account> account = accountService.getAccountById(accountId);
        if(account.isEmpty()){
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }
        MyGrowthResponse response = profileScreenService.getMyGrowthResponse(accountId);
        return ResponseEntity.ok(response);
    }
}
