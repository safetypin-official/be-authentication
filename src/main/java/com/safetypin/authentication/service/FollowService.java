package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserService userService;
    
    @Autowired
    public FollowService(FollowRepository followRepository, UserService userService) {
        this.followRepository = followRepository;
        this.userService = userService;
    }
    
    /**
     * Follow a user
     * @param followerId ID of the user who wants to follow
     * @param followingId ID of the user to be followed
     * @return the created Follow entity
     */
    @Transactional
    public Follow followUser(UUID followerId, UUID followingId) {
        // Validate both users exist
        userService.findById(followerId)
            .orElseThrow(() -> new ResourceNotFoundException("Follower user not found"));
        userService.findById(followingId)
            .orElseThrow(() -> new ResourceNotFoundException("User to follow not found"));
            
        // Prevent users from following themselves
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }
        
        // Check if already following
        if (isFollowing(followerId, followingId)) {
            throw new IllegalArgumentException("Already following this user");
        }
        
        // Create new follow relationship
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreatedAt(LocalDateTime.now());
        
        return followRepository.save(follow);
    }
    
    /**
     * Unfollow a user
     * @param followerId ID of the user who wants to unfollow
     * @param followingId ID of the user to be unfollowed
     */
    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        // Check if the follow relationship exists
        if (!isFollowing(followerId, followingId)) {
            throw new IllegalArgumentException("Not following this user");
        }
        
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    /**
     * Check if a user is following another user
     * @param followerId ID of the potential follower
     * @param followingId ID of the potential followed user
     * @return true if following, false otherwise
     */
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    /**
     * Get all users that a user is following
     * @param userId ID of the user
     * @return List of users that the user is following
     */
    public List<User> getFollowing(UUID userId) {
        List<UUID> followingIds = followRepository.findByFollowerId(userId)
            .stream()
            .map(Follow::getFollowingId)
            .toList();
            
        if (followingIds.isEmpty()) {
            return List.of();
        }
        
        return userService.findAllById(followingIds);
    }
    
    /**
     * Get all followers of a user
     * @param userId ID of the user
     * @return List of users that follow the user
     */
    public List<User> getFollowers(UUID userId) {
        List<UUID> followerIds = followRepository.findByFollowingId(userId)
            .stream()
            .map(Follow::getFollowerId)
            .toList();
            
        if (followerIds.isEmpty()) {
            return List.of();
        }
        
        return userService.findAllById(followerIds);
    }
    
    /**
     * Get the count of users a user is following
     * @param userId ID of the user
     * @return count of users being followed
     */
    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }
    
    /**
     * Get the count of followers for a user
     * @param userId ID of the user
     * @return count of followers
     */
    public long getFollowersCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }
}