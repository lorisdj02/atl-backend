package com.atl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.CheckoutResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.PaymentService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.screen.MatchScreenService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/checkout-session/{sessionId}")
    public ResponseEntity<CheckoutResponse> getCheckoutScreen(@PathVariable Integer sessionId) {
        CheckoutResponse response = paymentService.getCheckoutScreen(sessionId);
        return ResponseEntity.ok(response);
    }
}
