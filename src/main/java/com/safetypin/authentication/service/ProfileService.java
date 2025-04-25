package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.ProfileViewDTO;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.ProfileViewRepository;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProfileService {

    private final UserService userService;

    private final ProfileViewRepository profileViewRepository;

    @Autowired
    public ProfileService(UserService userService, ProfileViewRepository profileViewRepository, UserRepository userRepository) {
        this.userService = userService;
        this.profileViewRepository = profileViewRepository;
    }

    // TODO: Change all invocations to the other one (getProfile(UUID, String)) instead
    // DEPRECATED
    public ProfileResponse getProfile(UUID userId) {
        return getProfile(userId, null);
    }

    public ProfileResponse getProfile(UUID userId, UUID viewerId) {
        

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        return ProfileResponse.fromUser(user);
    }

    // TODO: Change all invocations to the other one (updateProfile(UUID, UpdateProfileRequest)) instead
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request, String token) {
        return updateProfile(userId, request);
    }

    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        // Extract usernames from social media URLs and update fields
        user.setInstagram(extractInstagramUsername(request.getInstagram()));
        user.setTwitter(extractTwitterUsername(request.getTwitter()));
        user.setLine(extractLineUsername(request.getLine()));
        user.setTiktok(extractTiktokUsername(request.getTiktok()));
        user.setDiscord(extractDiscordId(request.getDiscord()));
        user.setProfilePicture(request.getProfilePicture());
        user.setProfileBanner(request.getProfileBanner());

        User savedUser = userService.save(user);

        return ProfileResponse.fromUser(savedUser);
    }

    // Get all profiles
    public List<UserPostResponse> getAllProfiles() {
        List<User> users = userService.findAllUsers();

        return users.stream()
            .map(user -> UserPostResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .build())
            .toList();
    }



    // Helper methods to extract usernames from social media URLs

    private String extractInstagramUsername(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // Pattern to match instagram.com/username or instagram.com/@username
        Pattern pattern = Pattern.compile("instagram\\.com/(?:@)?(\\w+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // If no URL pattern found, return the original input (assuming it's just the username)
        return input.trim();
    }

    private String extractTwitterUsername(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // Pattern to match twitter.com/username or twitter.com/@username
        Pattern pattern = Pattern.compile("twitter\\.com/(?:@)?(\\w+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // If no URL pattern found, return the original input (assuming it's just the username)
        return input.trim();
    }

    private String extractLineUsername(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // Line typically uses IDs directly, so we'll just clean the input
        return input.trim();
    }

    private String extractTiktokUsername(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // Pattern to match tiktok.com/@username or tiktok.com/username
        Pattern pattern = Pattern.compile("tiktok\\.com/(?:@)?(\\w+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // If no URL pattern found, return the original input (assuming it's just the username)
        return input.trim();
    }

    private String extractDiscordId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // For Discord, we just store the Discord ID/username as provided
        return input.trim();
    }
}
