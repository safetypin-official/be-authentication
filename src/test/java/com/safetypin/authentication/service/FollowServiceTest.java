package com.safetypin.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.safetypin.authentication.dto.FollowerNotificationDTO;
import com.safetypin.authentication.dto.UserFollowResponse;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FollowService followService;

    private UUID followerId;
    private UUID followingId;
    private User follower;
    private User following;
    private Follow follow;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followingId = UUID.randomUUID();

        follower = new User();
        follower.setId(followerId);
        follower.setName("Follower User");

        following = new User();
        following.setId(followingId);
        following.setName("Following User");

        follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void followUser_Success() {
        // Arrange
        when(userService.findById(followerId)).thenReturn(Optional.of(follower));
        when(userService.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        // Act
        Follow result = followService.followUser(followerId, followingId);

        // Assert
        assertNotNull(result);
        assertEquals(followerId, result.getFollowerId());
        assertEquals(followingId, result.getFollowingId());
        verify(userService, times(1)).findById(followerId);
        verify(userService, times(1)).findById(followingId);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    void followUser_FollowerNotFound() {
        // Arrange
        when(userService.findById(followerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> followService.followUser(followerId, followingId));

        verify(userService, times(1)).findById(followerId);
        verify(userService, never()).findById(followingId);
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_FollowingNotFound() {
        // Arrange
        when(userService.findById(followerId)).thenReturn(Optional.of(follower));
        when(userService.findById(followingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> followService.followUser(followerId, followingId));

        verify(userService, times(1)).findById(followerId);
        verify(userService, times(1)).findById(followingId);
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_SelfFollow() {
        // Arrange - Using same ID for follower and following
        UUID sameId = UUID.randomUUID();
        User user = new User();
        user.setId(sameId);

        // Need to mock the userService to return the user when checking if exists
        when(userService.findById(sameId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> followService.followUser(sameId, sameId));

        // Update verification to expect findById to be called twice
        verify(userService, times(2)).findById(sameId);
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_AlreadyFollowing() {
        // Arrange
        when(userService.findById(followerId)).thenReturn(Optional.of(follower));
        when(userService.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> followService.followUser(followerId, followingId));

        verify(userService, times(1)).findById(followerId);
        verify(userService, times(1)).findById(followingId);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollowUser_Success() {
        // Arrange
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);
        doNothing().when(followRepository).deleteByFollowerIdAndFollowingId(followerId, followingId);

        // Act
        followService.unfollowUser(followerId, followingId);

        // Assert
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
        verify(followRepository, times(1)).deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Test
    void unfollowUser_NotFollowing() {
        // Arrange
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> followService.unfollowUser(followerId, followingId));

        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
        verify(followRepository, never()).deleteByFollowerIdAndFollowingId(any(), any());
    }

    @Test
    void isFollowing_True() {
        // Arrange
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

        // Act
        boolean result = followService.isFollowing(followerId, followingId);

        // Assert
        assertTrue(result);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Test
    void isFollowing_False() {
        // Arrange
        when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);

        // Act
        boolean result = followService.isFollowing(followerId, followingId);

        // Assert
        assertFalse(result);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Test
    void getFollowing_ReturnsUsers() {
        // Arrange
        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);

        Follow follow2 = new Follow();
        UUID followingId2 = UUID.randomUUID();
        follow2.setFollowerId(followerId);
        follow2.setFollowingId(followingId2);

        List<Follow> follows = Arrays.asList(follow1, follow2);
        List<UUID> followingIds = Arrays.asList(followingId, followingId2);

        User following2 = new User();
        following2.setId(followingId2);
        following2.setName("Another Following User");

        when(followRepository.findByFollowerId(followerId)).thenReturn(follows);
        when(userService.findAllById(followingIds)).thenReturn(Arrays.asList(following, following2));

        // Act
        List<User> result = followService.getFollowing(followerId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(following.getId())));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(following2.getId())));
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, times(1)).findAllById(anyList());
    }

    @Test
    void getFollowing_EmptyList() {
        // Arrange
        when(followRepository.findByFollowerId(followerId)).thenReturn(List.of());

        // Act
        List<User> result = followService.getFollowing(followerId);

        // Assert
        assertTrue(result.isEmpty());
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, never()).findAllById(anyList());
    }

    @Test
    void getFollowers_ReturnsUsers() {
        // Arrange
        UUID follower2Id = UUID.randomUUID();

        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);

        Follow follow2 = new Follow();
        follow2.setFollowerId(follower2Id);
        follow2.setFollowingId(followingId);

        List<Follow> follows = Arrays.asList(follow1, follow2);
        List<UUID> followerIds = Arrays.asList(followerId, follower2Id);

        User follower2 = new User();
        follower2.setId(follower2Id);
        follower2.setName("Another Follower User");

        when(followRepository.findByFollowingId(followingId)).thenReturn(follows);
        when(userService.findAllById(followerIds)).thenReturn(Arrays.asList(follower, follower2));

        // Act
        List<User> result = followService.getFollowers(followingId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(follower.getId())));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(follower2.getId())));
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, times(1)).findAllById(anyList());
    }

    @Test
    void getFollowers_EmptyList() {
        // Arrange
        when(followRepository.findByFollowingId(followingId)).thenReturn(List.of());

        // Act
        List<User> result = followService.getFollowers(followingId);

        // Assert
        assertTrue(result.isEmpty());
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, never()).findAllById(anyList());
    }

    @Test
    void getFollowingCount_ReturnsCount() {
        // Arrange
        when(followRepository.countByFollowerId(followerId)).thenReturn(5L);

        // Act
        long result = followService.getFollowingCount(followerId);

        // Assert
        assertEquals(5L, result);
        verify(followRepository, times(1)).countByFollowerId(followerId);
    }

    @Test
    void getFollowersCount_ReturnsCount() {
        // Arrange
        when(followRepository.countByFollowingId(followingId)).thenReturn(10L);

        // Act
        long result = followService.getFollowersCount(followingId);

        // Assert
        assertEquals(10L, result);
        verify(followRepository, times(1)).countByFollowingId(followingId);
    }

    // --- Tests for getRecentFollowers ---

    @Test
    void getRecentFollowers_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UUID follower1Id = UUID.randomUUID();
        User follower1 = new User();
        follower1.setId(follower1Id);
        follower1.setName("Follower One");
        follower1.setProfilePicture("pic1.jpg");

        UUID follower2Id = UUID.randomUUID();
        User follower2 = new User();
        follower2.setId(follower2Id);
        follower2.setName("Follower Two");
        follower2.setProfilePicture("pic2.jpg");

        LocalDateTime now = LocalDateTime.now();
        Follow follow1 = new Follow();
        follow1.setFollowerId(follower1Id);
        follow1.setFollowingId(userId);
        follow1.setCreatedAt(now.minusDays(5)); // 5 days ago

        Follow follow2 = new Follow();
        follow2.setFollowerId(follower2Id);
        follow2.setFollowingId(userId);
        follow2.setCreatedAt(now.minusDays(15)); // 15 days ago

        List<Follow> recentFollows = Arrays.asList(follow1, follow2); // Assume repo returns ordered desc
        List<UUID> followerIds = Arrays.asList(follower1Id, follower2Id);
        List<User> followers = Arrays.asList(follower1, follower2);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(followRepository.findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class)))
                .thenReturn(recentFollows);
        when(userService.findAllById(followerIds)).thenReturn(followers);

        // Act
        List<FollowerNotificationDTO> result = followService.getRecentFollowers(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first follower (most recent)
        FollowerNotificationDTO dto1 = result.get(0);
        assertEquals(follower1Id, dto1.getUserId());
        assertEquals("Follower One", dto1.getName());
        assertEquals("pic1.jpg", dto1.getProfilePicture());
        assertEquals(follow1.getCreatedAt(), dto1.getFollowedAt());
        assertEquals(5, dto1.getDaysAgo()); // Approximate check

        // Verify second follower
        FollowerNotificationDTO dto2 = result.get(1);
        assertEquals(follower2Id, dto2.getUserId());
        assertEquals("Follower Two", dto2.getName());
        assertEquals("pic2.jpg", dto2.getProfilePicture());
        assertEquals(follow2.getCreatedAt(), dto2.getFollowedAt());
        assertEquals(15, dto2.getDaysAgo()); // Approximate check

        verify(userService, times(1)).findById(userId);
        verify(followRepository, times(1)).findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class));
        verify(userService, times(1)).findAllById(followerIds);
    }

    @Test
    void getRecentFollowers_UserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> followService.getRecentFollowers(userId));

        verify(userService, times(1)).findById(userId);
        verify(followRepository, never()).findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(any(), any());
        verify(userService, never()).findAllById(anyList());
    }

    @Test
    void getRecentFollowers_NoRecentFollowers() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(followRepository.findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // Return empty list

        // Act
        List<FollowerNotificationDTO> result = followService.getRecentFollowers(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userService, times(1)).findById(userId);
        verify(followRepository, times(1)).findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class));
        verify(userService, never()).findAllById(anyList()); // Should not be called if no follows found
    }

    @Test
    void getRecentFollowers_FollowerDetailsNotFound() {
        // Arrange - Simulate a scenario where userService.findAllById doesn't return
        // all requested users
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UUID follower1Id = UUID.randomUUID();
        UUID follower2Id = UUID.randomUUID(); // This follower's details won't be found

        LocalDateTime now = LocalDateTime.now();
        Follow follow1 = new Follow();
        follow1.setFollowerId(follower1Id);
        follow1.setFollowingId(userId);
        follow1.setCreatedAt(now.minusDays(5));

        Follow follow2 = new Follow(); // Follow from the missing user
        follow2.setFollowerId(follower2Id);
        follow2.setFollowingId(userId);
        follow2.setCreatedAt(now.minusDays(10));

        List<Follow> recentFollows = Arrays.asList(follow1, follow2);
        List<UUID> followerIds = Arrays.asList(follower1Id, follower2Id);

        User follower1Details = new User(); // Only details for follower1 are available
        follower1Details.setId(follower1Id);
        follower1Details.setName("Follower One");

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(followRepository.findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class)))
                .thenReturn(recentFollows);
        // Mock userService to return only one user's details
        when(userService.findAllById(followerIds)).thenReturn(Collections.singletonList(follower1Details));

        // Act
        List<FollowerNotificationDTO> result = followService.getRecentFollowers(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one DTO should be created
        assertEquals(follower1Id, result.get(0).getUserId());
        assertEquals("Follower One", result.get(0).getName());

        verify(userService, times(1)).findById(userId);
        verify(followRepository, times(1)).findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId),
                any(LocalDateTime.class));
        verify(userService, times(1)).findAllById(followerIds);
    }

    @Test
    void getFollowingWithFollowStatus_ReturnsUserFollowResponses() {
        // Arrange
        UUID viewerId = UUID.randomUUID();
        
        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);

        Follow follow2 = new Follow();
        UUID followingId2 = UUID.randomUUID();
        follow2.setFollowerId(followerId);
        follow2.setFollowingId(followingId2);

        List<Follow> follows = Arrays.asList(follow1, follow2);
        List<UUID> followingIds = Arrays.asList(followingId, followingId2);

        User following2 = new User();
        following2.setId(followingId2);
        following2.setName("Another Following User");
        following2.setProfilePicture("pic2.jpg");

        // Set up user details for the users being followed
        following.setProfilePicture("pic1.jpg");

        when(followRepository.findByFollowerId(followerId)).thenReturn(follows);
        when(userService.findAllById(followingIds)).thenReturn(Arrays.asList(following, following2));
        
        // ViewerId follows following1 but not following2
        when(followRepository.existsByFollowerIdAndFollowingId(viewerId, followingId)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(viewerId, followingId2)).thenReturn(false);

        // Act
        List<UserFollowResponse> result = followService.getFollowing(followerId, viewerId);

        // Assert
        assertEquals(2, result.size());
        
        // Check first user
        UserFollowResponse firstResponse = result.stream()
            .filter(r -> r.getUserId().equals(followingId))
            .findFirst()
            .orElse(null);
        assertNotNull(firstResponse);
        assertEquals(following.getName(), firstResponse.getName());
        assertEquals(following.getProfilePicture(), firstResponse.getProfilePicture());
        assertTrue(firstResponse.isFollowing()); // ViewerId follows this user
        
        // Check second user
        UserFollowResponse secondResponse = result.stream()
            .filter(r -> r.getUserId().equals(followingId2))
            .findFirst()
            .orElse(null);
        assertNotNull(secondResponse);
        assertEquals(following2.getName(), secondResponse.getName());
        assertEquals(following2.getProfilePicture(), secondResponse.getProfilePicture());
        assertFalse(secondResponse.isFollowing()); // ViewerId doesn't follow this user
        
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, times(1)).findAllById(followingIds);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(viewerId, followingId);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(viewerId, followingId2);
    }

    @Test
    void getFollowingWithFollowStatus_EmptyList() {
        // Arrange
        UUID viewerId = UUID.randomUUID();
        when(followRepository.findByFollowerId(followerId)).thenReturn(List.of());

        // Act
        List<UserFollowResponse> result = followService.getFollowing(followerId, viewerId);

        // Assert
        assertTrue(result.isEmpty());
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, never()).findAllById(anyList());
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
    }

    @Test
    void getFollowersWithFollowStatus_ReturnsUserFollowResponses() {
        // Arrange
        UUID viewerId = UUID.randomUUID();
        UUID follower2Id = UUID.randomUUID();

        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);

        Follow follow2 = new Follow();
        follow2.setFollowerId(follower2Id);
        follow2.setFollowingId(followingId);

        List<Follow> follows = Arrays.asList(follow1, follow2);
        List<UUID> followerIds = Arrays.asList(followerId, follower2Id);

        User follower2 = new User();
        follower2.setId(follower2Id);
        follower2.setName("Another Follower User");
        follower2.setProfilePicture("pic2.jpg");
        
        // Set up follower details
        follower.setProfilePicture("pic1.jpg");

        when(followRepository.findByFollowingId(followingId)).thenReturn(follows);
        when(userService.findAllById(followerIds)).thenReturn(Arrays.asList(follower, follower2));
        
        // ViewerId follows follower1 but not follower2
        when(followRepository.existsByFollowerIdAndFollowingId(viewerId, followerId)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(viewerId, follower2Id)).thenReturn(false);

        // Act
        List<UserFollowResponse> result = followService.getFollowers(followingId, viewerId);

        // Assert
        assertEquals(2, result.size());
        
        // Check first follower
        UserFollowResponse firstResponse = result.stream()
            .filter(r -> r.getUserId().equals(followerId))
            .findFirst()
            .orElse(null);
        assertNotNull(firstResponse);
        assertEquals(follower.getName(), firstResponse.getName());
        assertEquals(follower.getProfilePicture(), firstResponse.getProfilePicture());
        assertTrue(firstResponse.isFollowing()); // ViewerId follows this user
        
        // Check second follower
        UserFollowResponse secondResponse = result.stream()
            .filter(r -> r.getUserId().equals(follower2Id))
            .findFirst()
            .orElse(null);
        assertNotNull(secondResponse);
        assertEquals(follower2.getName(), secondResponse.getName());
        assertEquals(follower2.getProfilePicture(), secondResponse.getProfilePicture());
        assertFalse(secondResponse.isFollowing()); // ViewerId doesn't follow this user
        
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, times(1)).findAllById(followerIds);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(viewerId, followerId);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(viewerId, follower2Id);
    }

    @Test
    void getFollowersWithFollowStatus_EmptyList() {
        // Arrange
        UUID viewerId = UUID.randomUUID();
        when(followRepository.findByFollowingId(followingId)).thenReturn(List.of());

        // Act
        List<UserFollowResponse> result = followService.getFollowers(followingId, viewerId);

        // Assert
        assertTrue(result.isEmpty());
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, never()).findAllById(anyList());
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
    }
}