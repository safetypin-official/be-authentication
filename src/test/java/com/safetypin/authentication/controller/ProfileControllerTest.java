package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.AuthResponse;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

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
        testAuthHeader = "Bearer test-token";

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

        // Set up test update request
        testUpdateRequest = new UpdateProfileRequest();
        testUpdateRequest.setInstagram("newuser");
        testUpdateRequest.setTwitter("newuser");
        testUpdateRequest.setLine("newuser");
        testUpdateRequest.setTiktok("newuser");
        testUpdateRequest.setDiscord("newuser#5678");
    }

    // GET PROFILE TESTS

    @Test
    void getProfile_Success() {
        // Arrange
        when(profileService.getProfile(testUserId)).thenReturn(testProfileResponse);

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
        when(profileService.getProfile(testUserId))
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
        when(profileService.getProfile(testUserId))
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

    // UPDATE PROFILE TESTS

    @Test
    void updateProfile_Success() {
        // Arrange
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class), eq("test-token")))
                .thenReturn(testProfileResponse);

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateProfile(testUserId, testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Profile updated successfully", body.getMessage());
        assertEquals(testProfileResponse, body.getData());
    }

    @Test
    void updateProfile_Unauthorized() {
        // Arrange
        String errorMessage = "You are not authorized to update this profile";
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class), eq("test-token")))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateProfile(testUserId, testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updateProfile_NotFound() {
        // Arrange
        String errorMessage = "User not found with id " + testUserId;
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class), eq("test-token")))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateProfile(testUserId, testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updateProfile_InternalServerError() {
        // Arrange
        String errorMessage = "Database update failed";
        when(profileService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class), eq("test-token")))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response =
                profileController.updateProfile(testUserId, testUpdateRequest, testAuthHeader);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Error updating profile: " + errorMessage, body.getMessage());
        assertNull(body.getData());
    }
}
