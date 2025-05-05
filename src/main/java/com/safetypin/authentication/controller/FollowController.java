package com.safetypin.authentication.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.safetypin.authentication.constants.ApiConstants;
import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.util.JwtUtils;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final FollowService followService;
    private final JwtUtils jwtUtils;

    @Autowired
    public FollowController(FollowService followService, JwtUtils jwtUtils) {
        this.followService = followService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/{userIdToFollow}")
    public ResponseEntity<Void> followUser(
            @PathVariable UUID userIdToFollow,
            @RequestHeader("Authorization") String authHeader) {
        
        UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
        UUID currentUserId = user.getId();

        followService.followUser(currentUserId, userIdToFollow);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userIdToUnfollow}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable UUID userIdToUnfollow,
            @RequestHeader("Authorization") String authHeader) {

        UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
        UUID currentUserId = user.getId();

        followService.unfollowUser(currentUserId, userIdToUnfollow);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowers(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        
        UserResponse currentUser = jwtUtils.parseUserFromAuthHeader(authHeader);
        UUID viewerId = currentUser.getId();
        
        List<UserFollowResponse> followers = followService.getFollowers(userId, viewerId);
        ApiResponse<List<UserFollowResponse>> response = ApiResponse.<List<UserFollowResponse>>builder()
                .status(ApiConstants.STATUS_SUCCESS)
                .success(true)
                .data(followers)
                .message("Followers retrieved successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<ApiResponse<List<UserFollowResponse>>> getFollowing(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        
        UserResponse currentUser = jwtUtils.parseUserFromAuthHeader(authHeader);
        UUID viewerId = currentUser.getId();
        
        List<UserFollowResponse> following = followService.getFollowing(userId, viewerId);
        ApiResponse<List<UserFollowResponse>> response = ApiResponse.<List<UserFollowResponse>>builder()
                .status(ApiConstants.STATUS_SUCCESS)
                .success(true)
                .data(following)
                .message("Following list retrieved successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<FollowStats>> getFollowStats(
            @PathVariable UUID userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        boolean isFollowing = false;

        if (authHeader != null && !authHeader.isEmpty()) {
            try {
                UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
                UUID currentUserId = user.getId();
                isFollowing = followService.isFollowing(currentUserId, userId);
            } catch (Exception e) {
                // Invalid token, keep isFollowing as false
            }
        }

        // Get followers and following counts
        long followersCount = followService.getFollowersCount(userId);
        long followingCount = followService.getFollowingCount(userId);

        FollowStats stats = FollowStats.builder()
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
        
        ApiResponse<FollowStats> response = ApiResponse.<FollowStats>builder()
                .status(ApiConstants.STATUS_SUCCESS)
                .success(true)
                .data(stats)
                .message(ApiConstants.MSG_FOLLOW_STATS)
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

        UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
        UUID currentUserId = user.getId();

        List<FollowerNotificationDTO> recentFollowers = followService.getRecentFollowers(currentUserId);
        return ResponseEntity.ok(recentFollowers);
    }
}