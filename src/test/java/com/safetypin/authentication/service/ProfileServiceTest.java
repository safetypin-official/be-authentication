package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.repository.ProfileViewRepository;
import com.safetypin.authentication.repository.ProfileViewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProfileViewRepository profileViewRepository;

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
    void updateProfile_UserExists_UpdatesProfile() {
        // Arrange
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

        verify(userService, times(1)).findById(userId);
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userService.findById(userId)).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.updateProfile(userId, request, validToken)
        );

        assertTrue(exception.getMessage().contains("User not found with id"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userService, times(1)).findById(userId);
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

    @Test
    void getAllProfiles_ReturnsAllUsersAsUserPostResponses() {
        // Arrange
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("User 1");
        user1.setProfilePicture("pic1.jpg");
        user1.setProfileBanner("banner1.jpg");

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("User 2");
        user2.setProfilePicture("pic2.jpg");
        user2.setProfileBanner("banner2.jpg");

        List<User> users = Arrays.asList(user1, user2);
        when(userService.findAllUsers()).thenReturn(users);

        // Act
        List<UserPostResponse> result = profileService.getAllProfiles();

        // Assert
        assertEquals(2, result.size());
        
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user1.getName(), result.get(0).getName());
        assertEquals(user1.getProfilePicture(), result.get(0).getProfilePicture());
        assertEquals(user1.getProfileBanner(), result.get(0).getProfileBanner());
        
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(user2.getName(), result.get(1).getName());
        assertEquals(user2.getProfilePicture(), result.get(1).getProfilePicture());
        assertEquals(user2.getProfileBanner(), result.get(1).getProfileBanner());
        
        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void updateProfile_UserWithNullRole_ReturnsProfileResponseWithNullRole() {
        // Arrange
        // Create a user with null role
        User userWithNullRole = new User();
        userWithNullRole.setId(userId);
        userWithNullRole.setRole(null); // null role
        userWithNullRole.setVerified(true);

        when(userService.findById(userId)).thenReturn(Optional.of(userWithNullRole));
        when(userService.save(any(User.class))).thenReturn(userWithNullRole);

        UpdateProfileRequest request = new UpdateProfileRequest();
        
        // Act
        ProfileResponse response = profileService.updateProfile(userId, request, validToken);

        // Assert
        assertNotNull(response);
        assertNull(response.getRole());

        verify(userService, times(1)).findById(userId);
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    void getProfile_UserWithNullRole_ReturnsProfileResponseWithNullRole() {
        // Arrange
        User userWithNullRole = new User();
        userWithNullRole.setId(userId);
        userWithNullRole.setRole(null); // null role
        userWithNullRole.setVerified(true);
        
        when(userService.findById(userId)).thenReturn(Optional.of(userWithNullRole));

        // Act
        ProfileResponse response = profileService.getProfile(userId);

        // Assert
        assertNotNull(response);
        assertNull(response.getRole());
        assertTrue(response.isVerified());
        
        verify(userService, times(1)).findById(userId);
    }
}
