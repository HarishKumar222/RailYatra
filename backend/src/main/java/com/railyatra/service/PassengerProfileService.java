package com.railyatra.service;

import com.railyatra.entity.PassengerProfile;
import com.railyatra.entity.User;
import com.railyatra.exception.BookingException;
import com.railyatra.exception.ResourceNotFoundException;
import com.railyatra.repository.PassengerProfileRepository;
import com.railyatra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerProfileService {

    private final PassengerProfileRepository profileRepository;
    private final UserRepository userRepository;

    private static final int FREE_LIMIT    = 3;
    private static final int PREMIUM_LIMIT = 10;

    public List<PassengerProfile> getProfiles(Long userId) {
        return profileRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    public PassengerProfile addProfile(Long userId, PassengerProfile profile) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        int limit = Boolean.TRUE.equals(user.getIsPremium()) ? PREMIUM_LIMIT : FREE_LIMIT;
        int count = profileRepository.countByUserId(userId);
        if (count >= limit)
            throw new BookingException("Profile limit reached (" + limit + ")." +
                (Boolean.TRUE.equals(user.getIsPremium()) ? "" : " Upgrade to Premium for more."));
        profile.setUser(user);
        return profileRepository.save(profile);
    }

    public void deleteProfile(Long profileId, Long userId) {
        PassengerProfile p = profileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        if (!p.getUser().getId().equals(userId))
            throw new BookingException("Unauthorized");
        profileRepository.delete(p);
    }
}
