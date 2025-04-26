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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
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
    private Claims claims; // Mock Claims for token parsing

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowService followService;

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
        testUser.setName("Test User");
        testUser.setRole(Role.REGISTERED_USER);
        testUser.setVerified(true);
        testUser.setInstagram("testinsta");
        testUser.setTwitter("testtwitter");
        testUser.setLine("testline");
        testUser.setTiktok("testtiktok");
        testUser.setDiscord("testdiscord");
        testUser.setProfilePicture("pic.jpg");
        testUser.setProfileBanner("banner.jpg");
    }

    @Nested
    @DisplayName("getProfile Tests")
    class GetProfileTests {
        @Test
        void getProfile_UserExists_ReturnsProfileResponse() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(followService.getFollowersCount(userId)).thenReturn(10L);
        when(followService.getFollowingCount(userId)).thenReturn(20L);

            // Act
            ProfileResponse response = profileService.getProfile(userId);

            // Assert
            assertNotNull(response);
            assertEquals(userId, response.getId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals("REGISTERED_USER", response.getRole());
            assertTrue(response.isVerified());
            assertEquals("testinsta", response.getInstagram());
            assertEquals("testtwitter", response.getTwitter());
            assertEquals("testline", response.getLine());
            assertEquals("testtiktok", response.getTiktok());
            assertEquals("testdiscord", response.getDiscord());
            assertEquals(testUser.getProfilePicture(), response.getProfilePicture());
            assertEquals(testUser.getProfileBanner(), response.getProfileBanner());
        assertEquals(10, response.getFollowersCount());
        assertEquals(20, response.getFollowingCount());
        assertFalse(response.isFollowing());

            verify(userService, times(1)).findById(userId);
        }

        @Test
        void getProfile_UserNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> profileService.getProfile(userId));

            assertEquals("User not found with id " + userId, exception.getMessage());
            verify(userService, times(1)).findById(userId);
        }

        @Test
        void getProfile_UserWithNullRole_ReturnsProfileResponseWithNullRole() {
            // Arrange
            testUser.setRole(null); // Set role to null
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            ProfileResponse response = profileService.getProfile(userId);

            // Assert
            assertNotNull(response);
            assertNull(response.getRole()); // Assert role is null in response
            assertTrue(response.isVerified());
            assertEquals(userId, response.getId());

            verify(userService, times(1)).findById(userId);
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        private UpdateProfileRequest createUpdateRequest() {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setInstagram("instagram.com/newinsta");
            request.setTwitter("twitter.com/newtwitter");
            request.setLine("newline");
            request.setTiktok("tiktok.com/@newtiktok");
            request.setDiscord("newdiscord#1234");
            request.setProfilePicture("new_pic.jpg");
            request.setProfileBanner("new_banner.jpg");
            return request;
        }

        @Test
        void updateProfile_ValidTokenAndUser_UpdatesAndReturnsProfile() {
            // Arrange
            UpdateProfileRequest request = createUpdateRequest();
            when(jwtService.parseToken(validToken)).thenReturn(claims);
            when(claims.getSubject()).thenReturn(userId.toString());
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            // Capture the user passed to save
            when(userService.save(any(User.class))).thenAnswer(invocation -> {
                User userToSave = invocation.getArgument(0);
                // Simulate saving by returning the modified user
                assertEquals("newinsta", userToSave.getInstagram());
                assertEquals("newtwitter", userToSave.getTwitter());
                assertEquals("newline", userToSave.getLine());
                assertEquals("newtiktok", userToSave.getTiktok());
                assertEquals("newdiscord#1234", userToSave.getDiscord());
                assertEquals("new_pic.jpg", userToSave.getProfilePicture());
                assertEquals("new_banner.jpg", userToSave.getProfileBanner());
                return userToSave; // Return the captured and potentially modified user
            });

            // Act
            ProfileResponse response = profileService.updateProfile(userId, request, validToken);

            // Assert
            assertNotNull(response);
            assertEquals(userId, response.getId());
            assertEquals("newinsta", response.getInstagram());
            assertEquals("newtwitter", response.getTwitter());
            assertEquals("newline", response.getLine());
            assertEquals("newtiktok", response.getTiktok());
            assertEquals("newdiscord#1234", response.getDiscord());
            assertEquals("new_pic.jpg", response.getProfilePicture());
            assertEquals("new_banner.jpg", response.getProfileBanner());
            assertEquals("REGISTERED_USER", response.getRole()); // Ensure role is preserved

            verify(jwtService, times(1)).parseToken(validToken);
            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class)); // Verify save was called
        }

        @Test
        void updateProfile_TokenUserIdDoesNotMatch_ThrowsInvalidCredentialsException() {
            // Arrange
            UpdateProfileRequest request = createUpdateRequest();
            UUID differentUserId = UUID.randomUUID();
            when(jwtService.parseToken(validToken)).thenReturn(claims);
            when(claims.getSubject()).thenReturn(differentUserId.toString()); // Different user ID in token

            // Act & Assert
            InvalidCredentialsException exception = assertThrows(
                    InvalidCredentialsException.class,
                    () -> profileService.updateProfile(userId, request, validToken));

            // The outer catch block now throws this specific message
            assertEquals("Invalid or expired token", exception.getMessage());
            verify(jwtService, times(1)).parseToken(validToken);
            // The inner check throws before findById or save is called
            verify(userService, never()).findById(any());
            verify(userService, never()).save(any());
        }

        @Test
        void updateProfile_UserNotFound_ThrowsResourceNotFoundExceptionInsideCatch() {
            // Arrange
            UpdateProfileRequest request = createUpdateRequest();
            when(jwtService.parseToken(validToken)).thenReturn(claims);
            when(claims.getSubject()).thenReturn(userId.toString());
            when(userService.findById(userId)).thenReturn(Optional.empty()); // User not found

            // Act & Assert
            InvalidCredentialsException exception = assertThrows(
                    InvalidCredentialsException.class,
                    () -> profileService.updateProfile(userId, request, validToken),
                    "Expected updateProfile to throw InvalidCredentialsException due to outer catch block");

            // Check the message from the outer catch block
            assertEquals("Invalid or expired token", exception.getMessage());

            // Verify interactions: parseToken and findById are called, save is not.
            verify(jwtService, times(1)).parseToken(validToken);
            verify(userService, times(1)).findById(userId);
            verify(userService, never()).save(any());
        }

        @Test
        void updateProfile_InvalidToken_ThrowsInvalidCredentialsException() {
            // Arrange
            UpdateProfileRequest request = createUpdateRequest();
            // Simulate JwtService throwing an exception during parsing
            when(jwtService.parseToken(validToken)).thenThrow(new RuntimeException("Simulated JWT parsing error"));

            // Act & Assert
            InvalidCredentialsException exception = assertThrows(
                    InvalidCredentialsException.class,
                    () -> profileService.updateProfile(userId, request, validToken));

            assertEquals("Invalid or expired token", exception.getMessage());
            verify(jwtService, times(1)).parseToken(validToken);
            verify(userService, never()).findById(any());
            verify(userService, never()).save(any());
        }

        @Test
        void updateProfile_UserWithNullRole_UpdatesAndReturnsProfileWithNullRole() {
            // Arrange
            testUser.setRole(null); // User has null role initially
            UpdateProfileRequest request = createUpdateRequest();

            when(jwtService.parseToken(validToken)).thenReturn(claims);
            when(claims.getSubject()).thenReturn(userId.toString());
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return saved
            // user

            // Act
            ProfileResponse response = profileService.updateProfile(userId, request, validToken);

            // Assert
            assertNotNull(response);
            assertNull(response.getRole()); // Role should remain null
            assertEquals(userId, response.getId());
            assertEquals("newinsta", response.getInstagram()); // Other fields updated

            verify(jwtService, times(1)).parseToken(validToken);
            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }

    @Test
    void extractInstagramUsername_WithComplexUrl_CorrectlyExtractsUsername() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod("extractInstagramUsername", String.class);
        method.setAccessible(true);
        
        // Test with complex URL that tests the regex matcher.find() logic
        String result1 = (String) method.invoke(profileService, "https://www.instagram.com/username?hl=en");
        assertEquals("username", result1);
        
        // Test with URL that has subdirectories after username
        String result2 = (String) method.invoke(profileService, "instagram.com/username/posts/");
        assertEquals("username", result2);
        
        // Test with URL that doesn't match the pattern (should return input as-is)
        String result3 = (String) method.invoke(profileService, "not-instagram-url");
        assertEquals("not-instagram-url", result3);
    }

        @Test
        void updateProfile_PartialUpdateRequest_UpdatesOnlyProvidedFields() {
            // Arrange
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setInstagram("only.new.insta"); // Only update Instagram
            // Other fields in request are null

            when(jwtService.parseToken(validToken)).thenReturn(claims);
            when(claims.getSubject()).thenReturn(userId.toString());
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenAnswer(invocation -> {
                User userToSave = invocation.getArgument(0);
                // Check that only specified fields were updated
                assertEquals("only.new.insta", userToSave.getInstagram()); // Updated
                assertEquals("testtwitter", userToSave.getTwitter()); // Should remain old value
                assertEquals("testline", userToSave.getLine()); // Should remain old value
                assertEquals("testtiktok", userToSave.getTiktok()); // Should remain old value
                assertEquals("testdiscord", userToSave.getDiscord()); // Should remain old value
                assertEquals("pic.jpg", userToSave.getProfilePicture()); // Should remain old value
                assertEquals("banner.jpg", userToSave.getProfileBanner()); // Should remain old value
                return userToSave;
            });

            // Act
            ProfileResponse response = profileService.updateProfile(userId, request, validToken);

            // Assert
            assertNotNull(response);
            assertEquals(userId, response.getId());
            assertEquals("only.new.insta", response.getInstagram());
            assertEquals("testtwitter", response.getTwitter()); // Check response reflects state
            assertEquals("testline", response.getLine());
            assertEquals("testtiktok", response.getTiktok());
            assertEquals("testdiscord", response.getDiscord());
            assertEquals("pic.jpg", response.getProfilePicture()); // Should be original value
            assertEquals("banner.jpg", response.getProfileBanner()); // Should be original value

            verify(jwtService, times(1)).parseToken(validToken);
            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getAllProfiles Tests")
    class GetAllProfilesTests {
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
        void getAllProfiles_NoUsersFound_ReturnsEmptyList() {
            // Arrange
            when(userService.findAllUsers()).thenReturn(Collections.emptyList());

            // Act
            List<UserPostResponse> result = profileService.getAllProfiles();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userService, times(1)).findAllUsers();
        }
    }

    @Nested
    @DisplayName("Extraction Logic Tests (Private Methods)")
    class ExtractionLogicTests {

        // Helper to invoke private methods
        private String invokeExtractionMethod(String methodName, String input) throws Exception {
            Method method = ProfileService.class.getDeclaredMethod(methodName, String.class);
            method.setAccessible(true);
            return (String) method.invoke(profileService, input);
        }

        @Test
        void extractInstagramUsername_VariousInputs() throws Exception {
            assertEquals("username", invokeExtractionMethod("extractInstagramUsername", "instagram.com/username"));
            assertEquals("username", invokeExtractionMethod("extractInstagramUsername", "instagram.com/@username"));
            assertEquals("username", invokeExtractionMethod("extractInstagramUsername", "username"));
            assertEquals("user_name.123",
                    invokeExtractionMethod("extractInstagramUsername", "instagram.com/user_name.123"));
            assertEquals("user_name.123", invokeExtractionMethod("extractInstagramUsername", "user_name.123"));
            assertEquals("user name", invokeExtractionMethod("extractInstagramUsername", " user name ")); // Trim test
            assertNull(invokeExtractionMethod("extractInstagramUsername", null));
            assertNull(invokeExtractionMethod("extractInstagramUsername", ""));
            assertNull(invokeExtractionMethod("extractInstagramUsername", "   ")); // Whitespace only
        }

        @Test
        void extractTwitterUsername_VariousInputs() throws Exception {
            assertEquals("username", invokeExtractionMethod("extractTwitterUsername", "twitter.com/username"));
            assertEquals("username", invokeExtractionMethod("extractTwitterUsername", "twitter.com/@username"));
            assertEquals("username", invokeExtractionMethod("extractTwitterUsername", "username"));
            assertEquals("user_name123", invokeExtractionMethod("extractTwitterUsername", "twitter.com/user_name123"));
            assertEquals("user_name123", invokeExtractionMethod("extractTwitterUsername", "user_name123"));
            assertEquals("user name", invokeExtractionMethod("extractTwitterUsername", " user name ")); // Trim test
            assertNull(invokeExtractionMethod("extractTwitterUsername", null));
            assertNull(invokeExtractionMethod("extractTwitterUsername", ""));
            assertNull(invokeExtractionMethod("extractTwitterUsername", "   ")); // Whitespace only
        }

        @Test
        void extractLineUsername_VariousInputs() throws Exception {
            assertEquals("line.user_name-123", invokeExtractionMethod("extractLineUsername", "line.user_name-123"));
            assertEquals("line user", invokeExtractionMethod("extractLineUsername", " line user ")); // Trim test
            assertNull(invokeExtractionMethod("extractLineUsername", null));
            assertNull(invokeExtractionMethod("extractLineUsername", ""));
            assertNull(invokeExtractionMethod("extractLineUsername", "   ")); // Whitespace only
        }

        @Test
        void extractTiktokUsername_VariousInputs() throws Exception {
            assertEquals("username", invokeExtractionMethod("extractTiktokUsername", "tiktok.com/username"));
            assertEquals("username", invokeExtractionMethod("extractTiktokUsername", "tiktok.com/@username"));
            assertEquals("username", invokeExtractionMethod("extractTiktokUsername", "username"));
            assertEquals("user.name_123", invokeExtractionMethod("extractTiktokUsername", "tiktok.com/@user.name_123"));
            assertEquals("user.name_123", invokeExtractionMethod("extractTiktokUsername", "user.name_123"));
            assertEquals("user name", invokeExtractionMethod("extractTiktokUsername", " user name ")); // Trim test
            assertNull(invokeExtractionMethod("extractTiktokUsername", null));
            assertNull(invokeExtractionMethod("extractTiktokUsername", ""));
            assertNull(invokeExtractionMethod("extractTiktokUsername", "   ")); // Whitespace only
        }

        @Test
        void extractDiscordId_VariousInputs() throws Exception {
            assertEquals("discorduser#1234", invokeExtractionMethod("extractDiscordId", "discorduser#1234"));
            assertEquals("discord user", invokeExtractionMethod("extractDiscordId", " discord user ")); // Trim test
            assertNull(invokeExtractionMethod("extractDiscordId", null));
            assertNull(invokeExtractionMethod("extractDiscordId", ""));
            assertNull(invokeExtractionMethod("extractDiscordId", "   ")); // Whitespace only
        }
    }

    @Nested
    @DisplayName("getUsersBatch Tests")
    class GetUsersBatchTests {

        @Test
        void getUsersBatch_ValidUserIds_ReturnsMapOfPostedByData() {
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

            // Verify content of the map using keys
            assertTrue(result.containsKey(userId1));
            assertEquals("User 1", result.get(userId1).getName());
            assertEquals("pic1.jpg", result.get(userId1).getProfilePicture());
            assertEquals(userId1, result.get(userId1).getUserId()); // Verify userId

            assertTrue(result.containsKey(userId2));
            assertEquals("User 2", result.get(userId2).getName());
            assertEquals("pic2.jpg", result.get(userId2).getProfilePicture());
            assertEquals(userId2, result.get(userId2).getUserId()); // Verify userId

            verify(userRepository, times(1)).findAllById(userIds);
        }

        @Test
        void getUsersBatch_EmptyUserIdsList_ReturnsEmptyMap() {
            // Arrange
            List<UUID> userIds = Collections.emptyList();

            // Act
            Map<UUID, PostedByData> result = profileService.getUsersBatch(userIds);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository, never()).findAllById(any()); // Should not call repo if list is empty
        }

        @Test
        void getUsersBatch_NullUserIdsList_ReturnsEmptyMap() {
            // Arrange
            List<UUID> userIds = null;

            // Act
            Map<UUID, PostedByData> result = profileService.getUsersBatch(userIds);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository, never()).findAllById(any()); // Should not call repo if list is null
        }

        @Test
        void getUsersBatch_SomeUserIdsNotFound_ReturnsPartialMap() {
            // Arrange
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID(); // This user won't be found
            UUID userId3 = UUID.randomUUID();

            User user1 = new User();
            user1.setId(userId1);
            user1.setName("User 1");
            user1.setProfilePicture("pic1.jpg");

            User user3 = new User();
            user3.setId(userId3);
            user3.setName("User 3");
            user3.setProfilePicture("pic3.jpg");

            List<UUID> requestedUserIds = Arrays.asList(userId1, userId2, userId3);
            // Simulate repository returning only found users
            List<User> foundUsers = Arrays.asList(user1, user3);
            when(userRepository.findAllById(requestedUserIds)).thenReturn(foundUsers); // Mock repo call

            // Act
            Map<UUID, PostedByData> result = profileService.getUsersBatch(requestedUserIds);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size()); // Should return 2 users found
            assertTrue(result.containsKey(userId1));
            assertEquals("User 1", result.get(userId1).getName());
            assertEquals("pic1.jpg", result.get(userId1).getProfilePicture());
            assertEquals(userId1, result.get(userId1).getUserId()); // Verify userId

            assertTrue(result.containsKey(userId3));
            assertEquals("User 3", result.get(userId3).getName());
            assertEquals("pic3.jpg", result.get(userId3).getProfilePicture());
            assertEquals(userId3, result.get(userId3).getUserId()); // Verify userId

            assertFalse(result.containsKey(userId2)); // User 2 should not be present

            verify(userRepository, times(1)).findAllById(requestedUserIds);
        }
    }

    @Test
    void getProfile_WithCurrentUserId_ChecksFollowingStatus() {
        // Arrange
        UUID currentUserId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(followService.getFollowersCount(userId)).thenReturn(10L);
        when(followService.getFollowingCount(userId)).thenReturn(20L);
        
        // Test when user is following
        when(followService.isFollowing(currentUserId, userId)).thenReturn(true);
        
        // Act
        ProfileResponse response = profileService.getProfile(userId, currentUserId);
        
        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertTrue(response.isFollowing());
        verify(followService, times(1)).isFollowing(currentUserId, userId);
        
        // Reset and test when user is not following
        reset(followService);
        when(followService.getFollowersCount(userId)).thenReturn(10L);
        when(followService.getFollowingCount(userId)).thenReturn(20L);
        when(followService.isFollowing(currentUserId, userId)).thenReturn(false);
        
        // Act again
        response = profileService.getProfile(userId, currentUserId);
        
        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertFalse(response.isFollowing());
        verify(followService, times(1)).isFollowing(currentUserId, userId);
    }
}
