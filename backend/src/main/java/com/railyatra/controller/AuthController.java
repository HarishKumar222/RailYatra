package com.railyatra.controller;

import com.railyatra.dto.request.LoginRequest;
import com.railyatra.dto.request.RegisterRequest;
import com.railyatra.dto.response.ApiResponse;
import com.railyatra.dto.response.AuthResponse;
import com.railyatra.service.AuthService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    private Bucket bucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(authService.register(req), "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        if (!bucket(request.getRemoteAddr()).tryConsume(1))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error("Too many login attempts. Wait 1 minute."));
        return ResponseEntity.ok(ApiResponse.success(authService.login(req), "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(auth.getName(), "Current user"));
    }
}
