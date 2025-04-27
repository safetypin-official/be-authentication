package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.ProfileViewDTO;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.ProfileViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private static final String USER_NOT_FOUND = "User not found with id ";
    private static final String VIEWER_NOT_FOUND = "Viewer not found with id ";

    private final UserService userService;
    private final ProfileViewRepository profileViewRepository;
    private final FollowService followService;

    @Autowired
    public ProfileService(UserService userService, ProfileViewRepository profileViewRepository, FollowService followService) {
        this.userService = userService;
        this.profileViewRepository = profileViewRepository;
        this.followService = followService;
    }

    public ProfileResponse getProfile(UUID userId, UUID viewerId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + userId));

        // Profile view tracking
        // Check if the viewer is the different as the user being viewed and not null
        if (viewerId != null && !viewerId.equals(userId)) {
            // Check if viewerId is a valid user
            User viewer = userService.findById(viewerId)
                    .orElseThrow(() -> new ResourceNotFoundException(VIEWER_NOT_FOUND + viewerId));

            Optional<ProfileView> profileViewOpt = profileViewRepository.findByUser_IdAndViewer_Id(userId, viewerId);
            ProfileView profileView;
            // Check if the profile view record already exists
            if (profileViewOpt.isPresent()) {
                profileView = profileViewOpt.get();
            } else {
                // Create a new profile view record if it doesn't exist
                profileView = new ProfileView();
                profileView.setUser(user);
                profileView.setViewer(viewer);
            }
            // Set viewedAt to the current date
            profileView.setViewedAt(LocalDateTime.now());
            profileViewRepository.save(profileView);
        }

        // If viewerId is provided, check if the user is following the profile
        boolean isFollowing = false;
        if (viewerId != null) {
            isFollowing = followService.isFollowing(viewerId, userId);
        }

        return ProfileResponse.fromUserAndFollowStatus(
                user,
                followService.getFollowersCount(userId),
                followService.getFollowingCount(userId),
                isFollowing
        );
    }

    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + userId));

        // Update fields only if they are provided (not null) in the request
        if (request.getInstagram() != null) {
            user.setInstagram(extractInstagramUsername(request.getInstagram()));
        }
        if (request.getTwitter() != null) {
            user.setTwitter(extractTwitterUsername(request.getTwitter()));
        }
        if (request.getLine() != null) {
            user.setLine(extractLineUsername(request.getLine()));
        }
        if (request.getTiktok() != null) {
            user.setTiktok(extractTiktokUsername(request.getTiktok()));
        }
        if (request.getDiscord() != null) {
            user.setDiscord(extractDiscordId(request.getDiscord()));
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        if (request.getProfileBanner() != null) {
            user.setProfileBanner(request.getProfileBanner());
        }

        User savedUser = userService.save(user);

        return ProfileResponse.fromUserAndFollowStatus(
                savedUser,
                followService.getFollowersCount(userId),
                followService.getFollowingCount(userId),
                false
        );
    }

    // Get all profiles
    public List<UserPostResponse> getAllProfiles() {
        List<User> users = userService.findAllUsers();

        return users.stream()
                .map(user -> UserPostResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .profilePicture(user.getProfilePicture())
                        .profileBanner(user.getProfileBanner())
                        .build())
                .toList();
    }

    // If user is premium, return all profile views to that user
    public List<ProfileViewDTO> getProfileViews(UUID userId) throws InvalidCredentialsException {
        // Check if the user is premium
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + userId));
        if (!user.getRole().toString().contains("PREMIUM")) {
            throw new InvalidCredentialsException("You need to be a premium user to view profile views.");
        }

        List<ProfileView> profileViews = profileViewRepository.findByUser_Id(userId);

        return profileViews.stream()
            .map(profileView -> ProfileViewDTO.builder()
                .viewerUserId(profileView.getViewer().getId())
                .name(profileView.getViewer().getName())
                .profilePicture(profileView.getViewer().getProfilePicture())
                .viewedAt(profileView.getViewedAt())
                .build())
            .toList();
    }


    public Map<UUID, PostedByData> getUsersBatch(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of(); // Return empty map if input is empty
        }
        List<User> users = userService.findAllById(userIds);
        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> PostedByData.builder()
                                .userId(user.getId()) // Changed from id to userId
                                .name(user.getName())
                                .profilePicture(user.getProfilePicture())
                                .build()));
    }



    // Helper methods to extract usernames from social media URLs

    private String extractInstagramUsername(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        // Corrected Regex: instagram\.com/@?([\w.]+)
        Pattern pattern = Pattern.compile("instagram\\.com/@?([\\w.]+)"); // Removed duplicate .
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
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

        // If no URL pattern found, return the original input (assuming it's just the
        // username)
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
        // Corrected Regex: tiktok\.com/@?([\w.]+)
        Pattern pattern = Pattern.compile("tiktok\\.com/@?([\\w.]+)"); // Removed duplicate .
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
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
