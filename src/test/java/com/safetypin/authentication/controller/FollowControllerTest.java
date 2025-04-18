package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.FollowStats;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    @Mock
    private FollowService followService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FollowController followController;

    private UUID userId;
    private UUID targetUserId;
    private String authHeader;
    private UserResponse userResponse;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        authHeader = "Bearer mock-token";
        
        userResponse = UserResponse.builder()
            .id(userId)
            .name("Test User")
            .build();

        user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("User 1");
        user1.setProfilePicture("pic1.jpg");
        user1.setProfileBanner("banner1.jpg");

        user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("User 2");
        user2.setProfilePicture("pic2.jpg");
        user2.setProfileBanner("banner2.jpg");
    }

    @Test
    void followUser_ReturnsCreated() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        
        // If followUser returns a Follow object, don't use doNothing
        Follow follow = new Follow();
        follow.setFollowerId(userId);
        follow.setFollowingId(targetUserId);
        when(followService.followUser(userId, targetUserId)).thenReturn(follow);

        // Act
        ResponseEntity<Void> response = followController.followUser(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, times(1)).followUser(userId, targetUserId);
    }

    @Test
    void unfollowUser_ReturnsNoContent() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        doNothing().when(followService).unfollowUser(userId, targetUserId);

        // Act
        ResponseEntity<Void> response = followController.unfollowUser(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, times(1)).unfollowUser(userId, targetUserId);
    }

    @Test
    void getFollowers_ReturnsFollowersList() {
        // Arrange
        List<User> followers = Arrays.asList(user1, user2);
        when(followService.getFollowers(targetUserId)).thenReturn(followers);

        // Act
        ResponseEntity<List<UserPostResponse>> response = followController.getFollowers(targetUserId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        assertEquals(user1.getId(), response.getBody().get(0).getId());
        assertEquals(user1.getName(), response.getBody().get(0).getName());
        assertEquals(user1.getProfilePicture(), response.getBody().get(0).getProfilePicture());
        assertEquals(user1.getProfileBanner(), response.getBody().get(0).getProfileBanner());
        
        assertEquals(user2.getId(), response.getBody().get(1).getId());
        assertEquals(user2.getName(), response.getBody().get(1).getName());
        assertEquals(user2.getProfilePicture(), response.getBody().get(1).getProfilePicture());
        assertEquals(user2.getProfileBanner(), response.getBody().get(1).getProfileBanner());
        
        verify(followService, times(1)).getFollowers(targetUserId);
    }

    @Test
    void getFollowing_ReturnsFollowingList() {
        // Arrange
        List<User> following = Arrays.asList(user1, user2);
        when(followService.getFollowing(targetUserId)).thenReturn(following);

        // Act
        ResponseEntity<List<UserPostResponse>> response = followController.getFollowing(targetUserId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        assertEquals(user1.getId(), response.getBody().get(0).getId());
        assertEquals(user1.getName(), response.getBody().get(0).getName());
        assertEquals(user1.getProfilePicture(), response.getBody().get(0).getProfilePicture());
        assertEquals(user1.getProfileBanner(), response.getBody().get(0).getProfileBanner());
        
        assertEquals(user2.getId(), response.getBody().get(1).getId());
        assertEquals(user2.getName(), response.getBody().get(1).getName());
        assertEquals(user2.getProfilePicture(), response.getBody().get(1).getProfilePicture());
        assertEquals(user2.getProfileBanner(), response.getBody().get(1).getProfileBanner());
        
        verify(followService, times(1)).getFollowing(targetUserId);
    }

    @Test
    void getFollowStats_WithAuthHeader_ReturnsStatsWithIsFollowing() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        when(followService.isFollowing(userId, targetUserId)).thenReturn(true);
        when(followService.getFollowersCount(targetUserId)).thenReturn(5L);
        when(followService.getFollowingCount(targetUserId)).thenReturn(10L);

        // Act
        ResponseEntity<FollowStats> response = followController.getFollowStats(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getFollowersCount());
        assertEquals(10L, response.getBody().getFollowingCount());
        assertTrue(response.getBody().isFollowing());
        
        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, times(1)).isFollowing(userId, targetUserId);
        verify(followService, times(1)).getFollowersCount(targetUserId);
        verify(followService, times(1)).getFollowingCount(targetUserId);
    }

    @Test
    void getFollowStats_WithoutAuthHeader_ReturnsStatsWithoutIsFollowing() {
        // Arrange
        when(followService.getFollowersCount(targetUserId)).thenReturn(5L);
        when(followService.getFollowingCount(targetUserId)).thenReturn(10L);

        // Act
        ResponseEntity<FollowStats> response = followController.getFollowStats(targetUserId, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getFollowersCount());
        assertEquals(10L, response.getBody().getFollowingCount());
        assertFalse(response.getBody().isFollowing());
        
        verify(jwtService, never()).getUserFromJwtToken(any());
        verify(followService, never()).isFollowing(any(), any());
        verify(followService, times(1)).getFollowersCount(targetUserId);
        verify(followService, times(1)).getFollowingCount(targetUserId);
    }

    @Test
    void getFollowStats_WithInvalidAuthHeader_HandlesException() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenThrow(new RuntimeException("Invalid token"));
        when(followService.getFollowersCount(targetUserId)).thenReturn(5L);
        when(followService.getFollowingCount(targetUserId)).thenReturn(10L);

        // Act
        ResponseEntity<FollowStats> response = followController.getFollowStats(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getFollowersCount());
        assertEquals(10L, response.getBody().getFollowingCount());
        assertFalse(response.getBody().isFollowing());
        
        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, never()).isFollowing(any(), any());
        verify(followService, times(1)).getFollowersCount(targetUserId);
        verify(followService, times(1)).getFollowingCount(targetUserId);
    }
}