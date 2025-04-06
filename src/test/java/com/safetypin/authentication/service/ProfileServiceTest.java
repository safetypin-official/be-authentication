package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        validToken = "valid.jwt.token";

        testUser = new User();
        testUser.setId(userId);
        testUser.setRole(Role.REGISTERED_USER);
        testUser.setVerified(true);
        testUser.setInstagram("testinsta");
        testUser.setTwitter("testtwitter");
        testUser.setLine("testline");
        testUser.setTiktok("testtiktok");
        testUser.setDiscord("testdiscord");
    }

    @Test
    void getProfile_UserExists_ReturnsProfileResponse() {
        // Arrange
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ProfileResponse response = profileService.getProfile(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("REGISTERED_USER", response.getRole());
        assertTrue(response.isVerified());
        assertEquals("testinsta", response.getInstagram());
        assertEquals("testtwitter", response.getTwitter());
        assertEquals("testline", response.getLine());
        assertEquals("testtiktok", response.getTiktok());
        assertEquals("testdiscord", response.getDiscord());

        verify(userService, times(1)).findById(userId);
    }

    @Test
    void getProfile_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.getProfile(userId)
        );

        assertEquals("User not found with id " + userId, exception.getMessage());
        verify(userService, times(1)).findById(userId);
    }

    @Test
    void updateProfile_ValidTokenAndUser_UpdatesProfile() {
        // Arrange
        when(jwtService.parseToken(validToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(userService.save(any(User.class))).thenReturn(testUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setInstagram("instagram.com/newinsta");
        request.setTwitter("twitter.com/newtwitter");
        request.setLine("newline");
        request.setTiktok("tiktok.com/@newtiktok");
        request.setDiscord("newdiscord#1234");

        // Act
        ProfileResponse response = profileService.updateProfile(userId, request, validToken);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());

        verify(jwtService, times(1)).parseToken(validToken);
        verify(userService, times(1)).findById(userId);
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_TokenUserIdDoesNotMatch_ThrowsInvalidCredentialsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(jwtService.parseToken(validToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(differentUserId.toString());

        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.updateProfile(userId, request, validToken)
        );

        assertEquals("Invalid or expired token", exception.getMessage());
        verify(jwtService, times(1)).parseToken(validToken);
        verify(userService, never()).findById(any());
        verify(userService, never()).save(any());
    }

    @Test
    void updateProfile_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(jwtService.parseToken(validToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(userService.findById(userId)).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.updateProfile(userId, request, validToken)
        );

        assertEquals("Invalid or expired token", exception.getMessage());
        verify(jwtService, times(1)).parseToken(validToken);
        verify(userService, times(1)).findById(userId);
        verify(userService, never()).save(any());
    }

    @Test
    void updateProfile_InvalidToken_ThrowsInvalidCredentialsException() {
        // Arrange
        when(jwtService.parseToken(validToken)).thenThrow(new RuntimeException("Invalid token"));

        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.updateProfile(userId, request, validToken)
        );

        assertEquals("Invalid or expired token", exception.getMessage());
        verify(jwtService, times(1)).parseToken(validToken);
        verify(userService, never()).findById(any());
        verify(userService, never()).save(any());
    }

    @Test
    void extractInstagramUsername_FromUrl_ReturnsUsername() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractInstagramUsername", String.class);
        method.setAccessible(true);

        // Test with URL format
        String result1 = (String) method.invoke(profileService, "instagram.com/username");
        assertEquals("username", result1);

        // Test with @ in URL
        String result2 = (String) method.invoke(profileService, "instagram.com/@username");
        assertEquals("username", result2);

        // Test with just username
        String result3 = (String) method.invoke(profileService, "username");
        assertEquals("username", result3);

        // Test with null
        String result4 = (String) method.invoke(profileService, (Object) null);
        assertNull(result4);

        // Test with empty string
        String result5 = (String) method.invoke(profileService, "");
        assertNull(result5);
    }

    @Test
    void extractTwitterUsername_FromUrl_ReturnsUsername() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractTwitterUsername", String.class);
        method.setAccessible(true);

        // Test with URL format
        String result1 = (String) method.invoke(profileService, "twitter.com/username");
        assertEquals("username", result1);

        // Test with @ in URL
        String result2 = (String) method.invoke(profileService, "twitter.com/@username");
        assertEquals("username", result2);

        // Test with just username
        String result3 = (String) method.invoke(profileService, "username");
        assertEquals("username", result3);

        // Test with null
        String result4 = (String) method.invoke(profileService, (Object) null);
        assertNull(result4);

        // Test with empty string
        String result5 = (String) method.invoke(profileService, "");
        assertNull(result5);
    }

    @Test
    void extractLineUsername_FromInput_ReturnsCleanedInput() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractLineUsername", String.class);
        method.setAccessible(true);

        // Test with normal input
        String result1 = (String) method.invoke(profileService, "lineusername");
        assertEquals("lineusername", result1);

        // Test with whitespace
        String result2 = (String) method.invoke(profileService, " lineusername ");
        assertEquals("lineusername", result2);

        // Test with null
        String result3 = (String) method.invoke(profileService, (Object) null);
        assertNull(result3);

        // Test with empty string
        String result4 = (String) method.invoke(profileService, "");
        assertNull(result4);
    }

    @Test
    void extractTiktokUsername_FromUrl_ReturnsUsername() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractTiktokUsername", String.class);
        method.setAccessible(true);

        // Test with URL format
        String result1 = (String) method.invoke(profileService, "tiktok.com/username");
        assertEquals("username", result1);

        // Test with @ in URL
        String result2 = (String) method.invoke(profileService, "tiktok.com/@username");
        assertEquals("username", result2);

        // Test with just username
        String result3 = (String) method.invoke(profileService, "username");
        assertEquals("username", result3);

        // Test with null
        String result4 = (String) method.invoke(profileService, (Object) null);
        assertNull(result4);

        // Test with empty string
        String result5 = (String) method.invoke(profileService, "");
        assertNull(result5);
    }

    @Test
    void extractDiscordId_FromInput_ReturnsCleanedInput() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractDiscordId", String.class);
        method.setAccessible(true);

        // Test with normal input
        String result1 = (String) method.invoke(profileService, "discorduser#1234");
        assertEquals("discorduser#1234", result1);

        // Test with whitespace
        String result2 = (String) method.invoke(profileService, " discorduser#1234 ");
        assertEquals("discorduser#1234", result2);

        // Test with null
        String result3 = (String) method.invoke(profileService, (Object) null);
        assertNull(result3);

        // Test with empty string
        String result4 = (String) method.invoke(profileService, "");
        assertNull(result4);
    }
}
