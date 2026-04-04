package com.railyatra.controller;

import com.railyatra.dto.response.ApiResponse;
import com.railyatra.entity.Train;
import com.railyatra.repository.*;
import com.railyatra.service.BookingService;
import com.railyatra.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    
    private final BookingService bookingService;

    @GetMapping("/analytics")
public ResponseEntity<ApiResponse<Map<String, Object>>> analytics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalUsers",        userRepository.count());
    stats.put("totalTrains",       trainRepository.count());
    stats.put("totalBookings",     bookingRepository.count());
    stats.put("confirmedBookings", bookingRepository.countConfirmedBookings());
    try {
        BigDecimal revenue = bookingRepository.sumTotalRevenue();
        stats.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
    } catch (Exception e) {
        stats.put("totalRevenue", BigDecimal.ZERO);
    }
    return ResponseEntity.ok(ApiResponse.success(stats, "Analytics"));
}
    @GetMapping("/trains")
public ResponseEntity<ApiResponse<?>> allTrains() {
    return ResponseEntity.ok(ApiResponse.success(
        trainRepository.findAll(), "All trains"));
}

    @PostMapping("/trains")
    @CacheEvict(value = {"trains", "popular-routes", "stations"}, allEntries = true)
    public ResponseEntity<ApiResponse<Train>> addTrain(@RequestBody Train train) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(trainRepository.save(train), "Train added"));
    }

    @PutMapping("/trains/{id}")
    @CacheEvict(value = "trains", key = "#id")
    public ResponseEntity<ApiResponse<Train>> updateTrain(
            @PathVariable Long id, @RequestBody Train train) {
        train.setId(id);
        return ResponseEntity.ok(ApiResponse.success(trainRepository.save(train), "Train updated"));
    }

    @DeleteMapping("/trains/{id}")
    @CacheEvict(value = "trains", key = "#id")
    public ResponseEntity<ApiResponse<String>> deleteTrain(@PathVariable Long id) {
        trainRepository.findById(id).ifPresent(t -> { t.setIsActive(false); trainRepository.save(t); });
        return ResponseEntity.ok(ApiResponse.success("Deactivated", "Train deactivated"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<?>> allBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        var bookings = bookingRepository.findAllByOrderByBookedAtDesc(
        PageRequest.of(page, size));
        var mapped = bookings.map(bookingService::mapToResponse);
        return ResponseEntity.ok(ApiResponse.success(mapped, "All bookings"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> allUsers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAll(), "All users"));
    }
}
