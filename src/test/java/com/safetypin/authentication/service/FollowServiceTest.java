package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        follow.setId(UUID.randomUUID());
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
        assertThrows(ResourceNotFoundException.class, () -> 
            followService.followUser(followerId, followingId));
        
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
        assertThrows(ResourceNotFoundException.class, () -> 
            followService.followUser(followerId, followingId));
        
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
        assertThrows(IllegalArgumentException.class, () -> 
            followService.followUser(sameId, sameId));
        
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
        assertThrows(IllegalArgumentException.class, () -> 
            followService.followUser(followerId, followingId));
        
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
        assertThrows(IllegalArgumentException.class, () -> 
            followService.unfollowUser(followerId, followingId));
        
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

        User following2 = new User();
        following2.setId(followingId2);
        following2.setName("Another Following User");

        when(followRepository.findByFollowerId(followerId)).thenReturn(follows);
        when(userService.findById(followingId)).thenReturn(Optional.of(following));
        when(userService.findById(followingId2)).thenReturn(Optional.of(following2));

        // Act
        List<User> result = followService.getFollowing(followerId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(following.getId(), result.get(0).getId());
        assertEquals(following2.getId(), result.get(1).getId());
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, times(1)).findById(followingId);
        verify(userService, times(1)).findById(followingId2);
    }

    @Test
    void getFollowing_UserNotFound() {
        // Arrange
        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);
        
        Follow follow2 = new Follow();
        UUID followingId2 = UUID.randomUUID();
        follow2.setFollowerId(followerId);
        follow2.setFollowingId(followingId2);
        
        List<Follow> follows = Arrays.asList(follow1, follow2);
        
        when(followRepository.findByFollowerId(followerId)).thenReturn(follows);
        when(userService.findById(followingId)).thenReturn(Optional.of(following));
        when(userService.findById(followingId2)).thenReturn(Optional.empty()); // User not found
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> followService.getFollowing(followerId)
        );
        
        assertTrue(exception.getMessage().contains("User not found: " + followingId2));
        verify(followRepository, times(1)).findByFollowerId(followerId);
        verify(userService, times(1)).findById(followingId);
        verify(userService, times(1)).findById(followingId2);
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

        User follower2 = new User();
        follower2.setId(follower2Id);
        follower2.setName("Another Follower User");

        when(followRepository.findByFollowingId(followingId)).thenReturn(follows);
        when(userService.findById(followerId)).thenReturn(Optional.of(follower));
        when(userService.findById(follower2Id)).thenReturn(Optional.of(follower2));

        // Act
        List<User> result = followService.getFollowers(followingId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(follower.getId(), result.get(0).getId());
        assertEquals(follower2.getId(), result.get(1).getId());
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, times(1)).findById(followerId);
        verify(userService, times(1)).findById(follower2Id);
    }

    @Test
    void getFollowers_UserNotFound() {
        // Arrange
        UUID follower2Id = UUID.randomUUID();
        
        Follow follow1 = new Follow();
        follow1.setFollowerId(followerId);
        follow1.setFollowingId(followingId);
        
        Follow follow2 = new Follow();
        follow2.setFollowerId(follower2Id);
        follow2.setFollowingId(followingId);
        
        List<Follow> follows = Arrays.asList(follow1, follow2);
        
        when(followRepository.findByFollowingId(followingId)).thenReturn(follows);
        when(userService.findById(followerId)).thenReturn(Optional.of(follower));
        when(userService.findById(follower2Id)).thenReturn(Optional.empty()); // User not found
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> followService.getFollowers(followingId)
        );
        
        assertTrue(exception.getMessage().contains("User not found: " + follower2Id));
        verify(followRepository, times(1)).findByFollowingId(followingId);
        verify(userService, times(1)).findById(followerId);
        verify(userService, times(1)).findById(follower2Id);
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
}