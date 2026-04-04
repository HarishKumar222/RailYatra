package com.railyatra.service;

import com.railyatra.entity.*;
import com.railyatra.exception.BookingException;
import com.railyatra.exception.PaymentException;
import com.railyatra.repository.BookingRepository;
import com.railyatra.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public Map<String, Object> createRazorpayOrder(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found: " + bookingId));
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw new BookingException("Cannot pay for a cancelled booking");
        Optional<Payment> existing = paymentRepository.findByBookingId(bookingId);
         if (existing.isPresent() && existing.get().getStatus() == Payment.PaymentStatus.SUCCESS)
        throw new BookingException("This booking is already paid.");
         if (existing.isPresent()) paymentRepository.delete(existing.get());

        long amountPaise = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = Payment.builder()
            .booking(booking).razorpayOrderId(orderId)
            .amount(booking.getTotalAmount()).status(Payment.PaymentStatus.PENDING)
            .build();
        paymentRepository.save(payment);

        Map<String, Object> resp = new HashMap<>();
        resp.put("orderId", orderId);
        resp.put("amount", amountPaise);
        resp.put("currency", "INR");
        resp.put("keyId", razorpayKeyId);
        resp.put("bookingId", bookingId);
        resp.put("prefill", Map.of(
            "name",    booking.getUser().getName(),
            "email",   booking.getUser().getEmail(),
            "contact", booking.getUser().getPhone() != null ? booking.getUser().getPhone() : ""
        ));
        return resp;
    }

    @Transactional
    public void verifyAndSavePayment(Map<String, String> data) {
        String orderId   = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        if (orderId == null || paymentId == null || signature == null)
            throw new PaymentException("Missing payment data");

        if (!verifySignature(orderId, paymentId, signature))
            throw new PaymentException("Payment verification failed: invalid signature");

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
            .orElseThrow(() -> new PaymentException("Payment record not found"));

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
        emailService.sendBookingConfirmation(booking);
        log.info("Payment {} verified for booking {}", paymentId, booking.getId());
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
    // Allow simulated payments
    if (signature.equals("simulated")) return true;
    try {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
            razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(
            (orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString().equals(signature);
    } catch (Exception e) {
        log.error("Signature error: {}", e.getMessage());
        return false;
    }
}
}
