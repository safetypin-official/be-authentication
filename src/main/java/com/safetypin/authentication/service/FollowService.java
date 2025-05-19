package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.FollowerNotificationDTO;
import com.safetypin.authentication.dto.UserFollowResponse;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
     *
     * @param followerId  ID of the user who wants to follow
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
     *
     * @param followerId  ID of the user who wants to unfollow
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
     *
     * @param followerId  ID of the potential follower
     * @param followingId ID of the potential followed user
     * @return true if following, false otherwise
     */
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * Get all users that a user is following
     *
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
     * Get all users that a user is following, with follow status for viewer
     *
     * @param userId   ID of the user
     * @param viewerId ID of the user viewing the list
     * @return List of users that the user is following with follow status
     */
    public List<UserFollowResponse> getFollowing(UUID userId, UUID viewerId) {
        List<UUID> followingIds = followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowingId)
                .toList();

        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<User> followingUsers = userService.findAllById(followingIds);

        return followingUsers.stream()
                .map(user -> UserFollowResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .profilePicture(user.getProfilePicture())
                        .isFollowing(isFollowing(viewerId, user.getId()))
                        .build())
                .toList();
    }

    /**
     * Get all followers of a user
     *
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
     * Get all followers of a user, with follow status for viewer
     *
     * @param userId   ID of the user
     * @param viewerId ID of the user viewing the list
     * @return List of users that follow the user with follow status
     */
    public List<UserFollowResponse> getFollowers(UUID userId, UUID viewerId) {
        List<UUID> followerIds = followRepository.findByFollowingId(userId)
                .stream()
                .map(Follow::getFollowerId)
                .toList();

        if (followerIds.isEmpty()) {
            return List.of();
        }

        List<User> followers = userService.findAllById(followerIds);

        return followers.stream()
                .map(user -> UserFollowResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .profilePicture(user.getProfilePicture())
                        .isFollowing(isFollowing(viewerId, user.getId()))
                        .build())
                .toList();
    }

    /**
     * Get the count of users a user is following
     *
     * @param userId ID of the user
     * @return count of users being followed
     */
    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

    /**
     * Get the count of followers for a user
     *
     * @param userId ID of the user
     * @return count of followers
     */
    public long getFollowersCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }

    /**
     * Get follower counts for multiple users in a single operation
     *
     * @param userIds List of user IDs to get follower counts for
     * @return Map of user IDs to their follower counts
     */
    public Map<UUID, Long> getFollowersCountBatch(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> results = followRepository.countFollowersByUserIds(userIds);
        Map<UUID, Long> followerCountMap = new HashMap<>();

        // Process query results and build the map
        for (Object[] result : results) {
            UUID userId = (UUID) result[0];
            Long count = ((Number) result[1]).longValue();
            followerCountMap.put(userId, count);
        }

        // Ensure all requested userIds have an entry, even if they have no followers
        for (UUID userId : userIds) {
            followerCountMap.putIfAbsent(userId, 0L);
        }

        return followerCountMap;
    }

    /**
     * Get recent followers for a user from the last 30 days
     *
     * @param userId ID of the user
     * @return List of follower notifications with user info and how long ago they
     * followed
     */
    public List<FollowerNotificationDTO> getRecentFollowers(UUID userId) {
        // Check if user exists
        userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get followers from the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Follow> recentFollows = followRepository.findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(userId,
                thirtyDaysAgo);

        if (recentFollows.isEmpty()) {
            return new ArrayList<>();
        }

        // Get follower user IDs
        List<UUID> followerIds = recentFollows.stream()
                .map(Follow::getFollowerId)
                .toList();

        // Get follower user details
        List<User> followers = userService.findAllById(followerIds);

        // Create a map for quick lookup of user details by ID
        Map<UUID, User> followerMap = followers.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Create DTOs with follower info and time information
        return recentFollows.stream()
                .map(follow -> {
                    User follower = followerMap.get(follow.getFollowerId());
                    if (follower == null) {
                        return null; // Skip if user not found (should not happen normally)
                    }

                    long daysAgo = ChronoUnit.DAYS.between(follow.getCreatedAt(), LocalDateTime.now());

                    return FollowerNotificationDTO.builder()
                            .userId(follower.getId())
                            .name(follower.getName())
                            .profilePicture(follower.getProfilePicture())
                            .followedAt(follow.getCreatedAt())
                            .daysAgo(daysAgo)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }
}