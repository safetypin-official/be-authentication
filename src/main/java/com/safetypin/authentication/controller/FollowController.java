package com.safetypin.authentication.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.safetypin.authentication.dto.FollowStats;
import com.safetypin.authentication.dto.FollowerNotificationDTO;
import com.safetypin.authentication.dto.UserFollowResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.JwtService;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final FollowService followService;
    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String STATUS_SUCCESS = "success";

    @Autowired
    public FollowController(FollowService followService, JwtService jwtService) {
        this.followService = followService;
        this.jwtService = jwtService;
    }

    @PostMapping("/{userIdToFollow}")
    public ResponseEntity<Void> followUser(
            @PathVariable UUID userIdToFollow,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse user = jwtService.getUserFromJwtToken(token);
        UUID currentUserId = user.getId();

        followService.followUser(currentUserId, userIdToFollow);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userIdToUnfollow}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable UUID userIdToUnfollow,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse user = jwtService.getUserFromJwtToken(token);
        UUID currentUserId = user.getId();

        followService.unfollowUser(currentUserId, userIdToUnfollow);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowers(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse currentUser = jwtService.getUserFromJwtToken(token);
        UUID viewerId = currentUser.getId();
        
        List<UserFollowResponse> followers = followService.getFollowers(userId, viewerId);
        ApiResponse<List<UserFollowResponse>> response = ApiResponse.<List<UserFollowResponse>>builder()
                .status(STATUS_SUCCESS)
                .data(followers)
                .message("Followers retrieved successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowing(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse currentUser = jwtService.getUserFromJwtToken(token);
        UUID viewerId = currentUser.getId();
        
        List<UserFollowResponse> following = followService.getFollowing(userId, viewerId);
        ApiResponse<List<UserFollowResponse>> response = ApiResponse.<List<UserFollowResponse>>builder()
                .status(STATUS_SUCCESS)
                .data(following)
                .message("Following list retrieved successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<FollowStats>> getFollowStats(
            @PathVariable UUID userId,
            @RequestHeader(value = "Authorization") String authHeader) {

        boolean isFollowing = false;

        try {
            String token = authHeader.replace(BEARER_PREFIX, "");
            UserResponse user = jwtService.getUserFromJwtToken(token);
            UUID currentUserId = user.getId();

            isFollowing = followService.isFollowing(currentUserId, userId);
        } catch (Exception e) {
            // Invalid token, keep isFollowing as false
        }

        FollowStats stats = FollowStats.builder()
                .followersCount(followService.getFollowersCount(userId))
                .followingCount(followService.getFollowingCount(userId))
                .isFollowing(isFollowing)
                .build();
        ApiResponse<FollowStats> response = ApiResponse.<FollowStats>builder()
                .status(STATUS_SUCCESS)
                .data(stats)
                .message("Follow statistics retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get recent followers from the last 30 days for the current authenticated user
     * 
     * @param authHeader Authorization header containing the JWT token
     * @return List of recent followers with information about when they followed
     */
    @GetMapping("/notifications/recent-followers")
    public ResponseEntity<List<FollowerNotificationDTO>> getRecentFollowers(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse user = jwtService.getUserFromJwtToken(token);
        UUID currentUserId = user.getId();

        List<FollowerNotificationDTO> recentFollowers = followService.getRecentFollowers(currentUserId);
        return ResponseEntity.ok(recentFollowers);
    }
}