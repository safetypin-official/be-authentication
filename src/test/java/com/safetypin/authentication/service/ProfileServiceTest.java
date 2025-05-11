package com.safetypin.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.ProfileViewDTO;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.ProfileViewRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProfileViewRepository profileViewRepository;

    @Mock
    private FollowService followService;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId, premiumUserId;
    private User testUser, testPremiumUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
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

        premiumUserId = UUID.randomUUID();
        testPremiumUser = new User();
        testPremiumUser.setId(premiumUserId);
        testPremiumUser.setRole(Role.PREMIUM_USER);
        testPremiumUser.setVerified(true);
        testPremiumUser.setInstagram("test_insta2");
        testPremiumUser.setTwitter("test_twitter2");
        testPremiumUser.setLine("test_line2");
        testPremiumUser.setTiktok("test_tiktok2");
        testPremiumUser.setDiscord("test_discord2");
        testPremiumUser.setProfilePicture("pic.jpg");
        testPremiumUser.setProfileBanner("banner.jpg");
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
            ProfileResponse response = profileService.getProfile(userId, null);

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
                    () -> profileService.getProfile(userId, null));

            assertEquals("User not found with id " + userId, exception.getMessage());
            verify(userService, times(1)).findById(userId);
        }

        @Test
        void getProfile_UserWithNullRole_ReturnsProfileResponseWithNullRole() {
            // Arrange
            testUser.setRole(null); // Set role to null
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            ProfileResponse response = profileService.getProfile(userId, null);

            // Assert
            assertNotNull(response);
            assertNull(response.getRole()); // Assert role is null in response
            assertTrue(response.isVerified());
            assertEquals(userId, response.getId());

            verify(userService, times(1)).findById(userId);
        }

        @Test
        void getProfile_ViewerDifferentFromUser_SavesProfileView() {
            // Arrange
            UUID viewerId = UUID.randomUUID();

            User viewer = new User();
            viewer.setId(viewerId);

            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.findById(viewerId)).thenReturn(Optional.of(viewer));
            when(profileViewRepository.findByUser_IdAndViewer_Id(userId, viewerId)).thenReturn(Optional.empty());

            // Act
            profileService.getProfile(userId, viewerId);

            // Assert
            verify(profileViewRepository, times(1)).save(any(ProfileView.class));
        }

        @Test
        void getProfile_ViewerSameAsUser_DoesNotSaveProfileView() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            profileService.getProfile(userId, userId);

            // Assert
            verify(profileViewRepository, never()).save(any(ProfileView.class));
        }

        @Test
        void getProfile_ViewerDifferentFromUser_AlreadySaved_UpdatesProfileView() {
            // Arrange
            UUID viewerId = UUID.randomUUID();

            User viewer = new User();
            viewer.setId(viewerId);

            LocalDateTime prev = LocalDateTime.of(2025, 2, 2, 2, 2);
            ProfileView view1 = new ProfileView();
            view1.setUser(testUser);
            view1.setViewer(viewer);
            view1.setViewedAt(prev);

            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.findById(viewerId)).thenReturn(Optional.of(viewer));
            when(profileViewRepository.findByUser_IdAndViewer_Id(userId, viewerId)).thenReturn(Optional.of(view1));

            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.findById(viewerId)).thenReturn(Optional.of(viewer));
            when(profileViewRepository.findByUser_IdAndViewer_Id(userId, viewerId)).thenReturn(Optional.of(view1));

            // Act
            profileService.getProfile(userId, viewerId);

            // Assert
            ArgumentCaptor<ProfileView> captor = ArgumentCaptor.forClass(ProfileView.class);
            verify(profileViewRepository, times(1)).save(
                    captor.capture());
            ProfileView updatedView = captor.getValue();
            assertNotEquals(prev, updatedView.getViewedAt());
            assertEquals(testUser, updatedView.getUser());
            assertEquals(viewer, updatedView.getViewer());
        }

        @Test
        void getProfile_ViewerNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            UUID viewerId = UUID.randomUUID();
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.findById(viewerId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> profileService.getProfile(userId, viewerId));

            assertTrue(exception.getMessage().contains("Viewer not found with id"));
            assertTrue(exception.getMessage().contains(viewerId.toString()));
            verify(profileViewRepository, never()).save(any(ProfileView.class));
        }

        @Test
        void getProfile_WithCurrentUserId_ChecksFollowingStatus() {
            // Arrange
            UUID currentUserId = UUID.randomUUID();
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.findById(currentUserId)).thenReturn(Optional.of(testPremiumUser));
            when(profileViewRepository.findByUser_IdAndViewer_Id(userId, currentUserId)).thenReturn(Optional.empty());
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

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {
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
            ProfileResponse response = profileService.updateProfile(userId, request);

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
                    () -> profileService.updateProfile(userId, request));

            assertTrue(exception.getMessage().contains("User not found with id"));
            assertTrue(exception.getMessage().contains(userId.toString()));
            verify(userService, times(1)).findById(userId);
            verify(userService, never()).save(any());
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
            ProfileResponse response = profileService.updateProfile(userId, request);

            // Assert
            assertNotNull(response);
            assertNull(response.getRole());

            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        void updateProfile_PartialUpdateRequest_UpdatesOnlyProvidedFields() {
            // Arrange
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setInstagram("only.new.insta"); // Only update Instagram
            // Other fields in request are null

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
            ProfileResponse response = profileService.updateProfile(userId, request);

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

            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        void updateProfile_WithNameField_UpdatesUserName() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setName("New Test Name"); // Only updating the name

            // Act
            ProfileResponse response = profileService.updateProfile(userId, request);

            // Assert
            assertNotNull(response);
            assertEquals("New Test Name", response.getName());

            // Verify user was saved with the new name
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userService).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals("New Test Name", savedUser.getName());

            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        void updateProfile_WithNameAndOtherFields_UpdatesAllProvidedFields() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));
            when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setName("New Test Name");
            request.setInstagram("instagram.com/newinsta");
            request.setProfilePicture("new-profile-pic.jpg");

            // Act
            ProfileResponse response = profileService.updateProfile(userId, request);

            // Assert
            assertNotNull(response);
            assertEquals("New Test Name", response.getName());
            assertEquals("newinsta", response.getInstagram());
            assertEquals("new-profile-pic.jpg", response.getProfilePicture());

            // Original values should be maintained for fields not in request
            assertEquals("testtwitter", response.getTwitter());
            assertEquals("testline", response.getLine());
            assertEquals("testtiktok", response.getTiktok());
            assertEquals("testdiscord", response.getDiscord());
            assertEquals("banner.jpg", response.getProfileBanner());

            verify(userService, times(1)).findById(userId);
            verify(userService, times(1)).save(any(User.class));
        }

        @Test
        void updateProfile_NameTooLong_ThrowsIllegalArgumentException() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));

            // Create a name that's 101 characters long
            StringBuilder longName = new StringBuilder();
            for (int i = 0; i < 101; i++) {
                longName.append("a");
            }

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setName(longName.toString());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> profileService.updateProfile(userId, request));

            assertEquals("Name must not exceed 100 characters", exception.getMessage());
            verify(userService, times(1)).findById(userId);
            verify(userService, never()).save(any(User.class));
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

            assertEquals(user1.getId(), result.get(0).getUserId());
            assertEquals(user1.getName(), result.get(0).getName());
            assertEquals(user1.getProfilePicture(), result.get(0).getProfilePicture());
            assertEquals(user1.getProfileBanner(), result.get(0).getProfileBanner());

            assertEquals(user2.getId(), result.get(1).getUserId());
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
        static Stream<Arguments> provideUsernameCases() {
            return Stream.of(
                    // Instagram cases
                    // 1. Username extraction from URL
                    Arguments.of("extractInstagramUsername", "username", "instagram.com/username"),
                    Arguments.of("extractInstagramUsername", "username", "instagram.com/@username"),
                    Arguments.of("extractInstagramUsername", "user_name.123", "instagram.com/user_name.123"),
                    // 2. Username extraction from plain username
                    Arguments.of("extractInstagramUsername", "username", "username"),
                    Arguments.of("extractInstagramUsername", "user_name.123", "user_name.123"),
                    // 3. Username with whitespace
                    Arguments.of("extractInstagramUsername", "user name", " user name "), // Trim test
                    // 4. Null or empty cases
                    Arguments.of("extractInstagramUsername", null, null),
                    Arguments.of("extractInstagramUsername", null, ""),
                    Arguments.of("extractInstagramUsername", null, "  "),

                    // Twitter cases
                    // 1. Username extraction from URL
                    Arguments.of("extractTwitterUsername", "username", "twitter.com/username"),
                    Arguments.of("extractTwitterUsername", "username", "twitter.com/@username"),
                    Arguments.of("extractTwitterUsername", "user_name123", "twitter.com/user_name123"),
                    // 2. Username extraction from plain username
                    Arguments.of("extractTwitterUsername", "username", "username"),
                    Arguments.of("extractTwitterUsername", "user_name123", "user_name123"),
                    // 3. Username with whitespace
                    Arguments.of("extractTwitterUsername", "user name", " user name "), // Trim test
                    // 4. Null or empty cases
                    Arguments.of("extractTwitterUsername", null, null),
                    Arguments.of("extractTwitterUsername", null, ""),
                    Arguments.of("extractTwitterUsername", null, " "),

                    // TikTok cases
                    // 1. Username extraction from URL
                    Arguments.of("extractTiktokUsername", "username", "tiktok.com/username"),
                    Arguments.of("extractTiktokUsername", "username", "tiktok.com/@username"),
                    Arguments.of("extractTiktokUsername", "user.name_123", "tiktok.com/@user.name_123"),
                    // 2. Username extraction from plain username
                    Arguments.of("extractTiktokUsername", "username", "username"),
                    Arguments.of("extractTiktokUsername", "user.name_123", "user.name_123"),
                    // 3. Username with whitespace
                    Arguments.of("extractTiktokUsername", "user name", " user name "), // Trim test
                    // 4. Null or empty cases
                    Arguments.of("extractTiktokUsername", null, null),
                    Arguments.of("extractTiktokUsername", null, ""),
                    Arguments.of("extractTiktokUsername", null, "  "),

                    // Line cases
                    // 1. Username extraction from plain username
                    Arguments.of("extractLineUsername", "lineusername", "lineusername"),
                    Arguments.of("extractLineUsername", "line.user_name-123", "line.user_name-123"),
                    // 2. Username with whitespace
                    Arguments.of("extractLineUsername", "lineusername", " lineusername "),
                    Arguments.of("extractLineUsername", "line user", " line user "), // Trim test
                    // 3. Null or empty cases
                    Arguments.of("extractLineUsername", null, null),
                    Arguments.of("extractLineUsername", null, ""),
                    Arguments.of("extractLineUsername", null, " "),

                    // Discord cases
                    // 1. Username extraction from plain username
                    Arguments.of("extractDiscordId", "discorduser#1234", "discorduser#1234"),
                    // 2. Username with whitespace
                    Arguments.of("extractDiscordId", "discorduser#1234", " discorduser#1234 "),
                    Arguments.of("extractDiscordId", "discord user", " discord user "), // Trim test
                    // 3. Null or empty cases
                    Arguments.of("extractDiscordId", null, null),
                    Arguments.of("extractDiscordId", null, ""),
                    Arguments.of("extractDiscordId", null, " "));
        }

        @ParameterizedTest
        @MethodSource("provideUsernameCases")
        void extractUsername_FromInput_ReturnsCleanedUsername(String methodName, String expected, String input)
                throws Exception {
            // Use reflection to access private method
            java.lang.reflect.Method method = ProfileService.class.getDeclaredMethod(methodName, String.class);
            method.setAccessible(true);

            // Test the extraction
            String result = (String) method.invoke(profileService, input);
            assertEquals(expected, result);
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

            when(userService.findAllById(userIds)).thenReturn(users);

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

            verify(userService, times(1)).findAllById(userIds);
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
            verify(userService, never()).findAllById(any()); // Should not call repo if list is empty
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
            verify(userService, never()).findAllById(any()); // Should not call repo if list is null
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
            when(userService.findAllById(requestedUserIds)).thenReturn(foundUsers); // Mock repo call

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

            verify(userService, times(1)).findAllById(requestedUserIds);
        }
    }

    @Nested
    @DisplayName("getProfileViews Tests")
    class GetProfileViewsTessts {
        @Test
        void getProfileViews_PremiumUser_ReturnsProfileViewDTOList() {
            // Arrange
            User viewer1 = new User();
            viewer1.setId(UUID.randomUUID());
            viewer1.setName("Viewer 1");
            viewer1.setProfilePicture("viewer1.jpg");

            User viewer2 = new User();
            viewer2.setId(UUID.randomUUID());
            viewer2.setName("Viewer 2");
            viewer2.setProfilePicture("viewer2.jpg");

            ProfileView view1 = new ProfileView();
            view1.setUser(testPremiumUser);
            view1.setViewer(viewer1);
            view1.setViewedAt(LocalDateTime.now());

            ProfileView view2 = new ProfileView();
            view2.setUser(testPremiumUser);
            view2.setViewer(viewer2);
            view2.setViewedAt(LocalDateTime.now());

            when(userService.findById(premiumUserId)).thenReturn(Optional.of(testPremiumUser));
            when(profileViewRepository.findByUser_Id(premiumUserId)).thenReturn(List.of(view1, view2));

            // Act
            List<ProfileViewDTO> result = profileService.getProfileViews(premiumUserId);

            // Assert
            assertEquals(2, result.size());
            assertEquals(viewer1.getId(), result.get(0).getViewerUserId());
            assertEquals(viewer1.getName(), result.get(0).getName());
            assertEquals(viewer1.getProfilePicture(), result.get(0).getProfilePicture());
            assertEquals(viewer2.getId(), result.get(1).getViewerUserId());
            assertEquals(viewer2.getName(), result.get(1).getName());
            assertEquals(viewer2.getProfilePicture(), result.get(1).getProfilePicture());

            verify(userService, times(1)).findById(premiumUserId);
            verify(profileViewRepository, times(1)).findByUser_Id(premiumUserId);
        }

        @Test
        void getProfileViews_NonPremiumUser_ThrowsInvalidCredentialsException() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.of(testUser));

            // Act & Assert
            InvalidCredentialsException exception = assertThrows(
                    InvalidCredentialsException.class,
                    () -> profileService.getProfileViews(userId));

            assertEquals("You need to be a premium user to view profile views.", exception.getMessage());
            verify(userService, times(1)).findById(userId);
            verify(profileViewRepository, never()).findByUser_Id(any());
        }

        @Test
        void getProfileViews_UserNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            when(userService.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> profileService.getProfileViews(userId));

            assertEquals("User not found with id " + userId, exception.getMessage());
            verify(userService, times(1)).findById(userId);
            verify(profileViewRepository, never()).findByUser_Id(any());
        }
    }
}
