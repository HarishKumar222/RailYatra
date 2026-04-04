package com.railyatra.controller;

import com.railyatra.dto.request.BookingRequest;
import com.railyatra.dto.response.ApiResponse;
import com.railyatra.dto.response.BookingResponse;
import com.railyatra.repository.UserRepository;
import com.railyatra.service.BookingService;
import com.railyatra.service.PDFService;
import com.railyatra.service.WaitlistPredictorService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PDFService pdfService;
    private final WaitlistPredictorService wlPredictor;
    private final UserRepository userRepository;

    private final Map<String, Bucket> bookingBuckets = new ConcurrentHashMap<>();

    private Bucket bucket(String email) {
        return bookingBuckets.computeIfAbsent(email, k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1))))
                .build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody BookingRequest req, Authentication auth) {
        if (!bucket(auth.getName()).tryConsume(1))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error("Booking too fast! Please wait a moment."));
        Long userId = getUserId(auth);
        BookingResponse booking = bookingService.createBooking(req, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(booking, "Booking created"));
    }

    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<ApiResponse<BookingResponse>> pnrStatus(@PathVariable String pnr) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getPNRStatus(pnr), "PNR Status"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id), "Booking details"));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> myBookings(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.getUserBookings(getUserId(auth)), "Your bookings"));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
            bookingService.cancelBooking(id, getUserId(auth)), "Booking cancelled"));
    }

    @GetMapping("/{id}/ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long id) {
        byte[] pdf = pdfService.generateTicket(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/wl-predict")
    public ResponseEntity<ApiResponse<Map<String, Object>>> wlPredict(
            @RequestParam Long trainId,
            @RequestParam String journeyDate,
            @RequestParam String classType,
            @RequestParam int wlNumber) {
        double prob = wlPredictor.predictConfirmationProbability(
            trainId, LocalDate.parse(journeyDate), classType, wlNumber);
        String label = wlPredictor.getPredictionLabel(prob);
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("probability", prob, "label", label,
                   "percentage", (int)(prob * 100) + "%"),
            "WL Prediction"));
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow().getId();
    }
}
