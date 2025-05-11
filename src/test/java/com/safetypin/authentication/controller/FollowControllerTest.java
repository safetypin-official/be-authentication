package com.safetypin.authentication.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.safetypin.authentication.dto.FollowStats;
import com.safetypin.authentication.dto.FollowerNotificationDTO;
import com.safetypin.authentication.dto.UserFollowResponse;
import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.JwtService;

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
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        
        UserFollowResponse user1Response = UserFollowResponse.builder()
                .userId(user1.getId())
                .name(user1.getName())
                .profilePicture(user1.getProfilePicture())
                .isFollowing(true)
                .build();
        
        UserFollowResponse user2Response = UserFollowResponse.builder()
                .userId(user2.getId())
                .name(user2.getName())
                .profilePicture(user2.getProfilePicture())
                .isFollowing(false)
                .build();
        
        List<UserFollowResponse> followerResponses = Arrays.asList(user1Response, user2Response);
        when(followService.getFollowers(targetUserId, userId)).thenReturn(followerResponses);

        // Act
        ResponseEntity<ApiResponse<List<UserFollowResponse>>> response = followController.getFollowers(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Followers retrieved successfully", response.getBody().getMessage());
        
        List<UserFollowResponse> responseData = response.getBody().getData();
        assertNotNull(responseData);
        assertEquals(2, responseData.size());
        
        assertEquals(user1.getId(), responseData.get(0).getUserId());
        assertEquals(user1.getName(), responseData.get(0).getName());
        assertEquals(user1.getProfilePicture(), responseData.get(0).getProfilePicture());
        assertTrue(responseData.get(0).isFollowing());
        
        assertEquals(user2.getId(), responseData.get(1).getUserId());
        assertEquals(user2.getName(), responseData.get(1).getName());
        assertEquals(user2.getProfilePicture(), responseData.get(1).getProfilePicture());
        assertFalse(responseData.get(1).isFollowing());

        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, times(1)).getFollowers(targetUserId, userId);
    }

    @Test
    void getFollowing_ReturnsFollowingList() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        
        UserFollowResponse user1Response = UserFollowResponse.builder()
                .userId(user1.getId())
                .name(user1.getName())
                .profilePicture(user1.getProfilePicture())
                .isFollowing(true)
                .build();
        
        UserFollowResponse user2Response = UserFollowResponse.builder()
                .userId(user2.getId())
                .name(user2.getName())
                .profilePicture(user2.getProfilePicture())
                .isFollowing(false)
                .build();
        
        List<UserFollowResponse> followingResponses = Arrays.asList(user1Response, user2Response);
        when(followService.getFollowing(targetUserId, userId)).thenReturn(followingResponses);

        // Act
        ResponseEntity<ApiResponse<List<UserFollowResponse>>> response = followController.getFollowing(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Following list retrieved successfully", response.getBody().getMessage());
        
        List<UserFollowResponse> responseData = response.getBody().getData();
        assertNotNull(responseData);
        assertEquals(2, responseData.size());
        
        assertEquals(user1.getId(), responseData.get(0).getUserId());
        assertEquals(user1.getName(), responseData.get(0).getName());
        assertEquals(user1.getProfilePicture(), responseData.get(0).getProfilePicture());
        assertTrue(responseData.get(0).isFollowing());
        
        assertEquals(user2.getId(), responseData.get(1).getUserId());
        assertEquals(user2.getName(), responseData.get(1).getName());
        assertEquals(user2.getProfilePicture(), responseData.get(1).getProfilePicture());
        assertFalse(responseData.get(1).isFollowing());

        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, times(1)).getFollowing(targetUserId, userId);
    }

    @Test
    void getFollowStats_WithAuthHeader_ReturnsStatsWithIsFollowing() {
        // Arrange
        when(jwtService.getUserFromJwtToken("mock-token")).thenReturn(userResponse);
        when(followService.isFollowing(userId, targetUserId)).thenReturn(true);
        when(followService.getFollowersCount(targetUserId)).thenReturn(5L);
        when(followService.getFollowingCount(targetUserId)).thenReturn(10L);

        // Act
        ResponseEntity<ApiResponse<FollowStats>> response = followController.getFollowStats(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Follow statistics retrieved successfully", response.getBody().getMessage());
        
        FollowStats stats = response.getBody().getData();
        assertNotNull(stats);
        assertEquals(5L, stats.getFollowersCount());
        assertEquals(10L, stats.getFollowingCount());
        assertTrue(stats.isFollowing());

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
        ResponseEntity<ApiResponse<FollowStats>> response = followController.getFollowStats(targetUserId, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Follow statistics retrieved successfully", response.getBody().getMessage());
        
        FollowStats stats = response.getBody().getData();
        assertNotNull(stats);
        assertEquals(5L, stats.getFollowersCount());
        assertEquals(10L, stats.getFollowingCount());
        assertFalse(stats.isFollowing());

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
        ResponseEntity<ApiResponse<FollowStats>> response = followController.getFollowStats(targetUserId, authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Follow statistics retrieved successfully", response.getBody().getMessage());
        
        FollowStats stats = response.getBody().getData();
        assertNotNull(stats);
        assertEquals(5L, stats.getFollowersCount());
        assertEquals(10L, stats.getFollowingCount());
        assertFalse(stats.isFollowing());

        verify(jwtService, times(1)).getUserFromJwtToken("mock-token");
        verify(followService, never()).isFollowing(any(), any());
        verify(followService, times(1)).getFollowersCount(targetUserId);
        verify(followService, times(1)).getFollowingCount(targetUserId);
    }

    // --- Tests for getRecentFollowers ---

    @Test
    void getRecentFollowers_Success() {
        // Arrange
        String token = "mock-token";
        String authHeaderWithBearer = "Bearer " + token;
        UUID currentUserId = UUID.randomUUID();
        UserResponse currentUser = UserResponse.builder().id(currentUserId).build();

        FollowerNotificationDTO dto1 = FollowerNotificationDTO.builder()
                .userId(UUID.randomUUID())
                .name("Follower 1")
                .profilePicture("pic1.jpg")
                .followedAt(LocalDateTime.now().minusDays(2))
                .daysAgo(2)
                .build();
        FollowerNotificationDTO dto2 = FollowerNotificationDTO.builder()
                .userId(UUID.randomUUID())
                .name("Follower 2")
                .profilePicture("pic2.jpg")
                .followedAt(LocalDateTime.now().minusDays(10))
                .daysAgo(10)
                .build();
        List<FollowerNotificationDTO> recentFollowers = Arrays.asList(dto1, dto2);

        when(jwtService.getUserFromJwtToken(token)).thenReturn(currentUser);
        when(followService.getRecentFollowers(currentUserId)).thenReturn(recentFollowers);

        // Act
        ResponseEntity<List<FollowerNotificationDTO>> response = followController
                .getRecentFollowers(authHeaderWithBearer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(dto1, response.getBody().get(0));
        assertEquals(dto2, response.getBody().get(1));

        verify(jwtService, times(1)).getUserFromJwtToken(token);
        verify(followService, times(1)).getRecentFollowers(currentUserId);
    }

    @Test
    void getRecentFollowers_ServiceReturnsEmptyList() {
        // Arrange
        String token = "mock-token";
        String authHeaderWithBearer = "Bearer " + token;
        UUID currentUserId = UUID.randomUUID();
        UserResponse currentUser = UserResponse.builder().id(currentUserId).build();

        when(jwtService.getUserFromJwtToken(token)).thenReturn(currentUser);
        when(followService.getRecentFollowers(currentUserId)).thenReturn(List.of()); // Service returns empty list

        // Act
        ResponseEntity<List<FollowerNotificationDTO>> response = followController
                .getRecentFollowers(authHeaderWithBearer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(jwtService, times(1)).getUserFromJwtToken(token);
        verify(followService, times(1)).getRecentFollowers(currentUserId);
    }
}