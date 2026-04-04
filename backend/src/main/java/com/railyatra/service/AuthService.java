package com.railyatra.service;

import com.railyatra.dto.request.LoginRequest;
import com.railyatra.dto.request.RegisterRequest;
import com.railyatra.dto.response.AuthResponse;
import com.railyatra.entity.User;
import com.railyatra.exception.BookingException;
import com.railyatra.repository.UserRepository;
import com.railyatra.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BookingException("Email already registered: " + req.getEmail());
        }
        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .role(User.Role.USER)
            .isPremium(false)
            .build();
        userRepository.save(user);
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        return buildResponse(user, ud);
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        return buildResponse(user, ud);
    }

    private AuthResponse buildResponse(User user, UserDetails ud) {
        return AuthResponse.builder()
            .accessToken(jwtProvider.generateToken(ud))
            .refreshToken(jwtProvider.generateRefreshToken(ud))
            .tokenType("Bearer")
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .isPremium(user.getIsPremium())
            .build();
    }
}
