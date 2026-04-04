package com.atl.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.response.InviteFriendsResponse;
import com.atl.backend.response.MyGrowthResponse;
import com.atl.backend.response.PersonalPlayerStatsResponse;
import com.atl.backend.response.RefereeLicensesResponse;
import com.atl.backend.response.ReviewsResponse;
import com.atl.backend.response.WalletResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.screen.ProfileScreenService;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    @Autowired
    private ProfileScreenService profileScreenService;
    @Autowired
    private AccountService accountService;

    @GetMapping("/growth-screen")
    public ResponseEntity<MyGrowthResponse> getMyGrowthScreen() {
        MyGrowthResponse response = profileScreenService.getMyGrowthScreen();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getWalletScreen() {
        WalletResponse response = profileScreenService.getWalletScreen();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/referee-licenses")
    public ResponseEntity<RefereeLicensesResponse> getRefereeLicensesScreen() {
        RefereeLicensesResponse response = profileScreenService.getRefereeLicensesScreen();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/personal-player-stats")
    public ResponseEntity<PersonalPlayerStatsResponse> getPersonalPlayerStatsScreen() {
        PersonalPlayerStatsResponse response = profileScreenService.getPersonalPlayerStatsScreen();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews")
    public ResponseEntity<ReviewsResponse> getReviewsScreen() {
        ReviewsResponse response = profileScreenService.getReviewsScreen();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friend-invites")
    public ResponseEntity<InviteFriendsResponse> getInviteFriendsScreen() {
        InviteFriendsResponse response = profileScreenService.getInviteFriendsScreen();
        return ResponseEntity.ok(response);
    }
}
