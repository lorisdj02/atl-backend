package com.atl.backend.controller;

import com.atl.backend.dto.AccountDTO;
import com.atl.backend.dto.RefereeLicenseDTO;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.model.Account;
import com.atl.backend.model.RefereeLicense;
import com.atl.backend.model.Review;
import com.atl.backend.request.RegisterAccountRequest;
import com.atl.backend.request.UpdateAccountInfosRequest;
import com.atl.backend.response.LoginResponse;
import com.atl.backend.service.accountServices.AccountService;
import com.atl.backend.service.accountServices.RefereeService;
import com.atl.backend.service.accountServices.ReviewService;

import com.atl.backend.service.orchestrator.ReviewsOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    @Autowired
    private ReviewsOrchestrator reviewsOrchestrator;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RefereeService refereeService;

    @GetMapping("/account/level")
    public ResponseEntity<Short> getAccountLevel() {
        // Ottieni l'accountId dal contesto di autenticazione
        return ResponseEntity.ok(accountService.getAccountLevel());
    }

    @PostMapping("/login")
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

    @DeleteMapping("/account/delete")
    public ResponseEntity<Void> deleteAccount() {
        accountService.deleteAccountAndRelatedData();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/referee/insert-referee-license")
    public ResponseEntity<RefereeLicenseDTO> insertRefereeLicense(@RequestBody RefereeLicenseDTO refereeLicense) {
        RefereeLicense insertedRefereeLicense = refereeService.insertRefereeLicense(refereeLicense);
        return ResponseEntity.ok(RefereeLicenseDTO.fromRefereeLicense(insertedRefereeLicense));
    }

    @DeleteMapping("/referee/delete-referee-license/{sportId}")
    public ResponseEntity<Void> deleteRefereeLicense(@PathVariable Long sportId) {
        refereeService.deleteRefereeLicense(sportId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/review/commit")
    public ResponseEntity<ReviewDTO> commitReview(@RequestBody ReviewDTO reviewDTO) {
        Review review = reviewsOrchestrator.commitReview(reviewDTO);
        return ResponseEntity.ok(ReviewDTO.fromReview(review));
    }

    @PostMapping("/account/register")
    public ResponseEntity<AccountDTO> registerAccount(@RequestBody RegisterAccountRequest accountDTO) {
        Account createdAccount = accountService.registerAccount(accountDTO);
        return ResponseEntity.ok(AccountDTO.fromAccount(createdAccount));
    }

}
