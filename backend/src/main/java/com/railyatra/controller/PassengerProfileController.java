package com.railyatra.controller;

import com.railyatra.dto.response.ApiResponse;
import com.railyatra.entity.PassengerProfile;
import com.railyatra.repository.UserRepository;
import com.railyatra.service.PassengerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PassengerProfileController {

    private final PassengerProfileService profileService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PassengerProfile>>> getAll(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
            profileService.getProfiles(uid(auth)), "Profiles"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PassengerProfile>> add(
            @RequestBody PassengerProfile profile, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
            profileService.addProfile(uid(auth), profile), "Profile saved"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id, Authentication auth) {
        profileService.deleteProfile(id, uid(auth));
        return ResponseEntity.ok(ApiResponse.success("Deleted", "Profile removed"));
    }

    private Long uid(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow().getId();
    }
}
