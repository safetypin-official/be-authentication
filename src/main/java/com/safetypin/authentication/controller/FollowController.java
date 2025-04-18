package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.FollowStats;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final FollowService followService;
    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";
    
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
    public ResponseEntity<List<UserPostResponse>> getFollowers(@PathVariable UUID userId) {
        List<User> followers = followService.getFollowers(userId);
        List<UserPostResponse> response = followers.stream()
            .map(user -> UserPostResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .build())
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserPostResponse>> getFollowing(@PathVariable UUID userId) {
        List<User> following = followService.getFollowing(userId);
        List<UserPostResponse> response = following.stream()
            .map(user -> UserPostResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .build())
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/{userId}")
    public ResponseEntity<FollowStats> getFollowStats(
            @PathVariable UUID userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        boolean isFollowing = false;
        
        if (authHeader != null && !authHeader.isEmpty()) {
            try {
                String token = authHeader.replace(BEARER_PREFIX, "");
                UserResponse user = jwtService.getUserFromJwtToken(token);
                UUID currentUserId = user.getId();
                
                isFollowing = followService.isFollowing(currentUserId, userId);
            } catch (Exception e) {
                // Invalid token, keep isFollowing as false
            }
        }
        
        FollowStats stats = FollowStats.builder()
            .followersCount(followService.getFollowersCount(userId))
            .followingCount(followService.getFollowingCount(userId))
            .isFollowing(isFollowing)
            .build();
        
        return ResponseEntity.ok(stats);
    }
}