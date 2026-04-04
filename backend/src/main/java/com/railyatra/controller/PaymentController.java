package com.railyatra.controller;

import com.railyatra.dto.response.ApiResponse;
import com.railyatra.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @RequestParam Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
            paymentService.createRazorpayOrder(bookingId), "Order created"));
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> verify(
            @RequestBody Map<String, String> paymentData) {
        paymentService.verifyAndSavePayment(paymentData);
        return ResponseEntity.ok(ApiResponse.success("Verified", "Payment successful"));
    }
}
