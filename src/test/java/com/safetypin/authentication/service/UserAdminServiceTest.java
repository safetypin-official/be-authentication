package com.safetypin.authentication.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.exception.UnauthorizedAccessException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import com.safetypin.authentication.repository.ProfileViewRepository;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    private static final String POST_SERVICE_URL = "http://post-service:8080";
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private ProfileViewRepository profileViewRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserAdminService userAdminService;
    private UUID moderatorId;
    private UUID targetUserId;
    private User moderator;
    private User targetUser;

    @BeforeEach
    void setUp() {
        moderatorId = UUID.randomUUID();
        targetUserId = UUID.fromString("e4384827-54f9-447b-8197-e6887170b291");

        moderator = new User();
        moderator.setId(moderatorId);
        moderator.setName("Moderator User");
        moderator.setEmail("moderator@example.com");
        moderator.setRole(Role.MODERATOR);
        moderator.setVerified(true);

        targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setName("Target User");
        targetUser.setEmail("target@example.com");
        targetUser.setRole(Role.REGISTERED_USER);
        targetUser.setVerified(true);

        // Set the post service URL for testing
        ReflectionTestUtils.setField(userAdminService, "postServiceUrl", POST_SERVICE_URL);

        // Mock JWT token generation with lenient stubbing
        lenient().when(jwtService.generateToken(any(UUID.class))).thenReturn("mocked-jwt-token");
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        // Act
        userAdminService.deleteUser(moderatorId, targetUserId);

        // Assert
        verify(refreshTokenRepository).deleteByUserId(targetUserId);
        verify(profileViewRepository).deleteByViewerId(targetUserId);
        verify(profileViewRepository).deleteByUserId(targetUserId);
        verify(followRepository).deleteByFollowerId(targetUserId);
        verify(followRepository).deleteByFollowingId(targetUserId);
        verify(userRepository).delete(targetUser);
        verify(restTemplate).exchange(
                contains("/posts/delete/" + targetUserId),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void deleteUser_CorrectlyNotifiesPostService() {
        // Arrange
        when(userRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        // Capture the URL and HttpEntity arguments
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // Act
        userAdminService.deleteUser(moderatorId, targetUserId);

        // Assert
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.DELETE),
                entityCaptor.capture(),
                eq(Void.class));

        // Verify the exact URL that was called
        String expectedUrl = POST_SERVICE_URL + "/posts/delete/" + targetUserId;
        String actualUrl = urlCaptor.getValue();
        assert (actualUrl.equals(expectedUrl));

        // Verify the headers in the HttpEntity
        HttpEntity<?> entity = entityCaptor.getValue();
        HttpHeaders headers = entity.getHeaders();
        assert (headers.getContentType().equals(MediaType.APPLICATION_JSON));
        assert (headers.getAccept().contains(MediaType.APPLICATION_JSON));
        assert (headers.get("Authorization").get(0).equals("Bearer mocked-jwt-token"));
    }

    @Test
    void deleteUser_ModeratorNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(moderatorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.deleteUser(moderatorId, targetUserId));

        // Verify no other actions were taken
        verify(userRepository, never()).delete(any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
        verify(restTemplate, never()).exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void deleteUser_NotAModerator_ThrowsUnauthorizedAccessException() {
        // Arrange
        moderator.setRole(Role.REGISTERED_USER); // Not a moderator
        when(userRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> userAdminService.deleteUser(moderatorId, targetUserId));

        // Verify no other actions were taken
        verify(userRepository, never()).delete(any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
        verify(restTemplate, never()).exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void deleteUser_TargetUserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.deleteUser(moderatorId, targetUserId));

        // Verify no other actions were taken
        verify(userRepository, never()).delete(any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
        verify(restTemplate, never()).exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void deleteUser_PostServiceException_CompletesSuccessfully() {
        // Arrange
        when(userRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        // Simulate exception when calling post service
        doThrow(new RuntimeException("Connection refused")).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class));

        // Act - should not throw exception even if post service call fails
        userAdminService.deleteUser(moderatorId, targetUserId);

        // Assert database operations still happen
        verify(refreshTokenRepository).deleteByUserId(targetUserId);
        verify(profileViewRepository).deleteByViewerId(targetUserId);
        verify(profileViewRepository).deleteByUserId(targetUserId);
        verify(followRepository).deleteByFollowerId(targetUserId);
        verify(followRepository).deleteByFollowingId(targetUserId);
        verify(userRepository).delete(targetUser);
    }
}
