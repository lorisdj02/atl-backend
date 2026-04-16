package com.atl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.CheckoutSessionDTO;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.response.CheckoutResponse;
import com.atl.backend.service.orchestrator.PurchasesOrchestrator;
import com.atl.backend.service.paymentServices.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PurchasesOrchestrator purchasesOrchestrator;

    @GetMapping("/checkout-session/{sessionId}")
    public ResponseEntity<CheckoutResponse> getCheckoutScreen(@PathVariable Integer sessionId) {
        CheckoutResponse response = paymentService.getCheckoutScreen(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutSessionDTO> createCheckoutSession(@RequestBody CheckoutPayload payload) {
        CheckoutSession response = purchasesOrchestrator.createCheckoutSession(payload);
        return ResponseEntity.ok(CheckoutSessionDTO.fromCheckoutSession(response));
    }
}
