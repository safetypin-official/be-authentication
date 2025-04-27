package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ProfileController profileController;

    private UUID testUserId;
    private ProfileResponse testProfileResponse;
    private UpdateProfileRequest testUpdateRequest;
    private String testAuthHeader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        String testToken = "test-token";
        testAuthHeader = "Bearer " + testToken;

        // Set up test user response from JWT
        UserResponse testUserResponse = UserResponse.builder()
                .id(testUserId)
                .name("testuser")
                .email("test@example.com")
                .build();

        // Set up test profile response
        testProfileResponse = ProfileResponse.builder()
                .id(testUserId)
                .role("USER")
                .isVerified(true)
                .instagram("testuser")
                .twitter("testuser")
                .line("testuser")
                .tiktok("testuser")
                .discord("testuser#1234")
                .build();

        // Set up test update request - using setters instead of builder
        testUpdateRequest = new UpdateProfileRequest();
        testUpdateRequest.setInstagram("newuser");
        testUpdateRequest.setTwitter("newuser");
        testUpdateRequest.setLine("newuser");
        testUpdateRequest.setTiktok("newuser");
        testUpdateRequest.setDiscord("newuser#5678");
        testUpdateRequest.setProfilePicture("https://example.com/profile.jpg");
        testUpdateRequest.setProfileBanner("https://example.com/banner.jpg");

        // Configure JWT service mock
        when(jwtService.getUserFromJwtToken(testToken)).thenReturn(testUserResponse);
    }

    // GET PROFILE TESTS

    @Test
    void getProfile_Success() {
        // Arrange
        when(profileService.getProfile(testUserId, testUserId)).thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile retrieved successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
    }

    @Test
    void getProfile_NotFound() {
        // Arrange
        when(profileService.getProfile(testUserId, testUserId))
                .thenThrow(new ResourceNotFoundException("User not found with id " + testUserId));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("User not found with id " + testUserId, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void getProfile_InternalServerError() {
        // Arrange
        String errorMessage = "Database connection error";
        when(profileService.getProfile(testUserId, testUserId))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Error retrieving profile: " + errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    // GET MY PROFILE TESTS

    @Test
    void getMyProfile_Success() {
        // Arrange
        when(profileService.getProfile(testUserId, testUserId)).thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getMyProfile(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile retrieved successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
    }

    @Test
    void getMyProfile_NotFound() {
        // Arrange
        when(profileService.getProfile(testUserId, testUserId))
                .thenThrow(new ResourceNotFoundException("User not found with id " + testUserId));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getMyProfile(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("User not found with id " + testUserId, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void getMyProfile_InternalServerError() {
        // Arrange
        String errorMessage = "Database connection error";
        when(profileService.getProfile(testUserId, testUserId))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getMyProfile(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Error retrieving profile: " + errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    // UPDATE MY PROFILE TESTS

    @Test
    void updateMyProfile_Success() {
        // Arrange
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateMyProfile(testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile updated successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
    }

    @Test
    void updateMyProfile_Unauthorized() {
        // Arrange
        String errorMessage = "You are not authorized to update this profile";
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateMyProfile(testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updateMyProfile_WrongToken() {
        // Arrange
        String errorMessage = "Token expired";
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateMyProfile(testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updateMyProfile_NotFound() {
        // Arrange
        String errorMessage = "User not found with id " + testUserId;
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateMyProfile(testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updateMyProfile_InternalServerError() {
        // Arrange
        String errorMessage = "Database update failed";
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateMyProfile(testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Error updating profile: " + errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    // GET USERS BATCH TEST

    @Test
    void getUsersBatch_Success() {
        // Arrange
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        // Create PostedByData instances using the builder
        PostedByData user1Data = PostedByData.builder()
                .userId(userIds.get(0))
                .name("User1")
                .profilePicture("pic1.jpg")
                .build();
        PostedByData user2Data = PostedByData.builder()
                .userId(userIds.get(1))
                .name("User2")
                .profilePicture("pic2.jpg")
                .build();
        Map<UUID, PostedByData> expectedResponse = Map.of(
                userIds.get(0), user1Data,
                userIds.get(1), user2Data);

        when(profileService.getUsersBatch(userIds)).thenReturn(expectedResponse);

        // Act
        Map<UUID, PostedByData> response = profileController.getUsersBatch(userIds);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertEquals(2, response.size());
    }

    // AUTHENTICATED PROFILE VIEW TESTS

    @Test
    void getProfile_AuthenticatedSuccess() {
        // Arrange
        UUID viewerId = testUserId;
        UUID profileId = UUID.randomUUID();
        when(profileService.getProfile(profileId, viewerId)).thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(profileId, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile retrieved successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
    }

    @Test
    void getProfile_FailedAuthenticationDoesNotAffectOutput() {
        // Arrange
        String invalidAuthHeader = "Bearer invalid-token";
        UUID profileId = UUID.randomUUID();
        when(jwtService.getUserFromJwtToken("invalid-token"))
                .thenThrow(new InvalidCredentialsException("Invalid token"));
        when(profileService.getProfile(profileId, null)).thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(profileId, invalidAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile retrieved successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
        // Verify that profileService.getProfile was called with null viewerId
        verify(profileService).getProfile(profileId, null);
    }

    // PROFILE VIEWS TESTS

    @Test
    void getProfileViews_Success() {
        // Arrange
        List<ProfileViewDTO> testViews = Arrays.asList(
                ProfileViewDTO.builder()
                        .viewerUserId(UUID.randomUUID())
                        .name("Viewer 1")
                        .profilePicture("pic1.jpg")
                        .viewedAt(LocalDateTime.now())
                        .build(),
                ProfileViewDTO.builder()
                        .viewerUserId(UUID.randomUUID())
                        .name("Viewer 2")
                        .profilePicture("pic2.jpg")
                        .viewedAt(LocalDateTime.now())
                        .build()
        );

        when(profileService.getProfileViews(testUserId)).thenReturn(testViews);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfileViews(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile views retrieved successfully", body.getMessage());
        assertEquals(testViews, body.getData());
    }

    @Test
    void getProfileViews_Unauthorized() {
        // Arrange
        String errorMessage = "You need to be a premium user to view profile views.";
        when(profileService.getProfileViews(testUserId))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfileViews(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void getProfileViews_NotFound() {
        // Arrange
        String errorMessage = "User not found";
        when(profileService.getProfileViews(testUserId))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfileViews(testAuthHeader);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }
}
