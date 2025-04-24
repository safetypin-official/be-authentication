package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    @Mock
    private UserRepository userRepository;

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

        List<String> testUrls = Arrays.asList(
                "instagram.com/username",
                "instagram.com/@username",
                "username",
                null,
                ""
        );

        // Test with URL format
        String result1 = (String) method.invoke(profileService, testUrls.get(0));
        assertEquals("username", result1);

        // Test with @ in URL
        String result2 = (String) method.invoke(profileService, testUrls.get(1));
        assertEquals("username", result2);

        // Test with just username
        String result3 = (String) method.invoke(profileService, testUrls.get(2));
        assertEquals("username", result3);

        // Test with null
        String result4 = (String) method.invoke(profileService, testUrls.get(3));
        assertNull(result4);

        // Test with empty string
        String result5 = (String) method.invoke(profileService, testUrls.get(4));
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
        
        assertEquals(user1.getId(), result.getFirst().getId());
        assertEquals(user1.getName(), result.getFirst().getName());
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
        
        when(jwtService.parseToken(validToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(userService.findById(userId)).thenReturn(Optional.of(userWithNullRole));
        when(userService.save(any(User.class))).thenReturn(userWithNullRole);

        UpdateProfileRequest request = new UpdateProfileRequest();
        
        // Act
        ProfileResponse response = profileService.updateProfile(userId, request, validToken);

        // Assert
        assertNotNull(response);
        assertNull(response.getRole());
        
        verify(jwtService, times(1)).parseToken(validToken);
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

    @Test
    void getUsersBatch_ReturnsMapOfUUIDToPostedByData() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        User user1 = new User();
        user1.setId(userId1);
        user1.setName("User 1");
        user1.setProfilePicture("pic1.jpg");

        User user2 = new User();
        user2.setId(userId2);
        user2.setName("User 2");
        user2.setProfilePicture("pic2.jpg");

        List<UUID> userIds = Arrays.asList(userId1, userId2);
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAllById(userIds)).thenReturn(users);

        // Act
        Map<UUID, PostedByData> result = profileService.getUsersBatch(userIds);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.containsKey(userId1));
        assertTrue(result.containsKey(userId2));

        assertEquals("User 1", result.get(userId1).getName());
        assertEquals("pic1.jpg", result.get(userId1).getProfilePicture());

        assertEquals("User 2", result.get(userId2).getName());
        assertEquals("pic2.jpg", result.get(userId2).getProfilePicture());

        verify(userRepository, times(1)).findAllById(userIds);
    }
}
