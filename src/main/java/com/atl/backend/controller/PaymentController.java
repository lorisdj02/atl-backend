package com.atl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.dto.CheckoutSessionDTO;
import com.atl.backend.dto.PurchaseDTO;
import com.atl.backend.dto.StripePaymentSheetData;
import com.atl.backend.exception.PaymentRequiredException;
import com.atl.backend.model.CheckoutSession;
import com.atl.backend.model.Purchase;
import com.atl.backend.response.CheckoutResponse;
import com.atl.backend.service.orchestrator.PurchasesOrchestrator;
import com.atl.backend.service.paymentServices.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PurchasesOrchestrator purchasesOrchestrator;

    @GetMapping("/checkout-session/{sessionId}")
    public ResponseEntity<CheckoutResponse> getCheckoutScreen(@PathVariable Long sessionId) {
        CheckoutResponse response = paymentService.getCheckoutScreen(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutSessionDTO> createCheckoutSession(@RequestBody CheckoutPayload payload) {
        CheckoutSession response = purchasesOrchestrator.createCheckoutSession(payload);
        return ResponseEntity.ok(CheckoutSessionDTO.fromCheckoutSession(response));
    }

    // Uso: /api/payments/checkout-session/{sessionId}/pay?useWallet=true
    @PatchMapping("/checkout-session/{sessionId}/pay")
    public ResponseEntity<PurchaseDTO> payCheckoutSession(@PathVariable Long sessionId,
            @RequestParam(required = false) Boolean useWallet, @RequestBody(required = false) String paymentIntentId) {
        Purchase response = purchasesOrchestrator.payCheckoutSession(sessionId, useWallet != null ? useWallet : false, paymentIntentId);
        if (response == null) {
            throw new PaymentRequiredException(ErrorsString.PAYMENT_FAILED);
        }
        return ResponseEntity.ok(PurchaseDTO.fromPurchase(response));
    }

    @PostMapping("/create-payment-intent/{sessionId}")
    public ResponseEntity<StripePaymentSheetData> createPaymentIntent(@PathVariable Long sessionId, @RequestParam(required = false) Boolean useWallet) {
        StripePaymentSheetData response = paymentService.createPaymentIntent(sessionId, useWallet != null ? useWallet : false);
        return ResponseEntity.ok(response);
    }
}
