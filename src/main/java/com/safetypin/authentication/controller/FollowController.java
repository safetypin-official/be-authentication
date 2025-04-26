package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.FollowStats;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final FollowService followService;
    
    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }
    
    @PostMapping("/{userIdToFollow}")
    public ResponseEntity<Void> followUser(@PathVariable UUID userIdToFollow) {
        UUID currentUserId = getCurrentUserId();
        followService.followUser(currentUserId, userIdToFollow);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @DeleteMapping("/{userIdToUnfollow}")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID userIdToUnfollow) {
        UUID currentUserId = getCurrentUserId();
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
    public ResponseEntity<FollowStats> getFollowStats(@PathVariable UUID userId) {
        boolean isFollowing = false;

        try {
            UUID currentUserId = getCurrentUserId();
            isFollowing = followService.isFollowing(currentUserId, userId);
        } catch (Exception e) {
            // User not authenticated, keep isFollowing as false
        }
        
        FollowStats stats = FollowStats.builder()
            .followersCount(followService.getFollowersCount(userId))
            .followingCount(followService.getFollowingCount(userId))
            .isFollowing(isFollowing)
            .build();
        
        return ResponseEntity.ok(stats);
    }
    
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserResponse userDetails = (UserResponse) authentication.getPrincipal();
        return userDetails.getId();
    }
}