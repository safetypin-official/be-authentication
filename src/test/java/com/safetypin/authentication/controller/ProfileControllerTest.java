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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private String testToken;
    private UserResponse testUserResponse;
    private List<UserPostResponse> testAllProfiles;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testToken = "test-token";
        testAuthHeader = "Bearer " + testToken;

        // Set up test user response from JWT
        testUserResponse = UserResponse.builder()
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

        // Set up test profiles list
        UserPostResponse profile1 = UserPostResponse.builder()
                .id(testUserId)
                .name("testuser")
                .profilePicture("https://example.com/profile1.jpg")
                .profileBanner("https://example.com/banner1.jpg")
                .build();

        UserPostResponse profile2 = UserPostResponse.builder()
                .id(UUID.randomUUID())
                .name("otheruser")
                .profilePicture("https://example.com/profile2.jpg")
                .profileBanner("https://example.com/banner2.jpg")
                .build();

        testAllProfiles = Arrays.asList(profile1, profile2);

        // Configure JWT service mock
        when(jwtService.getUserFromJwtToken(testToken)).thenReturn(testUserResponse);
    }

    // GET PROFILE TESTS

    @Test
    void getProfile_Success() {
        // Arrange
        when(profileService.getProfile(testUserId, null)).thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId);

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
        when(profileService.getProfile(testUserId, null))
                .thenThrow(new ResourceNotFoundException("User not found with id " + testUserId));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId);

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
        when(profileService.getProfile(testUserId, null))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getProfile(testUserId);

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

    // GET ALL PROFILES TESTS

    @Test
    void getAllProfiles_Success() {
        // Arrange
        when(profileService.getAllProfiles()).thenReturn(testAllProfiles);

        // Act
        ResponseEntity<AuthResponse> response = profileController.getAllProfiles();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("All profiles retrieved successfully", body.getMessage());
        assertEquals(testAllProfiles, body.getData());
    }

    @Test
    void getAllProfiles_InternalServerError() {
        // Arrange
        String errorMessage = "Database connection error";
        when(profileService.getAllProfiles())
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = profileController.getAllProfiles();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Error retrieving profiles: " + errorMessage, body.getMessage());
        assertNull(body.getData());
    }
}

