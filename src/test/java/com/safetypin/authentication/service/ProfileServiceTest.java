package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.ProfileViewDTO;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.repository.ProfileViewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    private UUID userId, premiumUserId;
    private User testUser, testPremiumUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setRole(Role.REGISTERED_USER);
        testUser.setVerified(true);
        testUser.setInstagram("testinsta");
        testUser.setTwitter("testtwitter");
        testUser.setLine("testline");
        testUser.setTiktok("testtiktok");
        testUser.setDiscord("testdiscord");

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
    }

    @Test
    void getProfile_UserExists_ReturnsProfileResponse() {
        // Arrange
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ProfileResponse response = profileService.getProfile(userId, null);

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
                () -> profileService.getProfile(userId, null)
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
                () -> profileService.updateProfile(userId, request)
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
        ProfileResponse response = profileService.updateProfile(userId, request);

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
        ProfileResponse response = profileService.getProfile(userId, null);

        // Assert
        assertNotNull(response);
        assertNull(response.getRole());
        assertTrue(response.isVerified());
        
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
                () -> profileService.getProfile(userId, viewerId)
        );

        assertTrue(exception.getMessage().contains("Viewer not found with id"));
        assertTrue(exception.getMessage().contains(viewerId.toString()));
        verify(profileViewRepository, never()).save(any(ProfileView.class));
    }

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
                () -> profileService.getProfileViews(userId)
        );

        assertEquals("You need to be a premium user to view profile views.", exception.getMessage());
        verify(userService, times(1)).findById(userId);
        verify(profileViewRepository, never()).findByUser_Id(any());
    }

    @Test
    void getProfileViews_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.getProfileViews(userId)
        );

        assertEquals("User not found with id " + userId, exception.getMessage());
        verify(userService, times(1)).findById(userId);
        verify(profileViewRepository, never()).findByUser_Id(any());
    }
}
