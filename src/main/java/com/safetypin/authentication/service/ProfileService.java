package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProfileService {

    private final UserService userService;
    private final JwtService jwtService;
    private final FollowService followService;

    @Autowired
    public ProfileService(UserService userService, JwtService jwtService, FollowService followService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.followService = followService;
    }

    public ProfileResponse getProfile(UUID userId) {
        return getProfile(userId, null);
    }

    public ProfileResponse getProfile(UUID userId, UUID currentUserId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        // If currentUserId is provided, check if the user is following the profile
        boolean isFollowing = false;

        if (currentUserId != null) {
            isFollowing = followService.isFollowing(currentUserId, userId);
        }

        return ProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isVerified(user.isVerified())
                .instagram(user.getInstagram())
                .twitter(user.getTwitter())
                .line(user.getLine())
                .tiktok(user.getTiktok())
                .discord(user.getDiscord())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .followersCount(followService.getFollowersCount(userId))
                .followingCount(followService.getFollowingCount(userId))
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request, String token) {
        // Verify the JWT token and get the user
        try {
            UUID tokenUserId = UUID.fromString(jwtService.parseToken(token).getSubject());

            // Check if token user ID matches the requested user ID
            if (!tokenUserId.equals(userId)) {
                throw new InvalidCredentialsException("You are not authorized to update this profile");
            }

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

            return ProfileResponse.builder()
                    .id(savedUser.getId())
                    .role(savedUser.getRole() != null ? savedUser.getRole().name() : null)
                    .isVerified(savedUser.isVerified())
                    .instagram(savedUser.getInstagram())
                    .twitter(savedUser.getTwitter())
                    .line(savedUser.getLine())
                    .tiktok(savedUser.getTiktok())
                    .discord(savedUser.getDiscord())
                    .name(savedUser.getName())
                    .profilePicture(savedUser.getProfilePicture())
                    .profileBanner(savedUser.getProfileBanner())
                    .followersCount(followService.getFollowersCount(userId))
                    .followingCount(followService.getFollowingCount(userId))
                    .isFollowing(false)
                    .build();

        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid or expired token");
        }
    }

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
