package com.safetypin.authentication.service;

import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;

    private User testUser;
    private UUID userId;
    private RefreshToken testRefreshToken, newTestRefreshToken;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository);

        // Setup test user
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);

        // Setup test refresh token
        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(UUID.randomUUID());
        testRefreshToken.setToken("test-token");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiryTime(Instant.now().plusSeconds(86400)); // 24 hours

        // Setup new test refresh token
        newTestRefreshToken = new RefreshToken();
        newTestRefreshToken.setId(UUID.randomUUID());
        newTestRefreshToken.setToken("new-test-token");
        newTestRefreshToken.setUser(testUser);
        newTestRefreshToken.setExpiryTime(Instant.now().plusSeconds(86400)); // 24 hours
    }

    @Test
    void createRefreshToken_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testRefreshToken, result);

        // Verify token is created with correct properties
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken capturedToken = tokenCaptor.getValue();
        assertNotNull(capturedToken.getToken());
        // Check that token length is greater than 40 characters
        assertTrue(capturedToken.getToken().length() > 40,
                "Token should be longer than 40 characters for security reasons");
        assertEquals(testUser, capturedToken.getUser());
        assertNotNull(capturedToken.getExpiryTime());
        assertTrue(capturedToken.getExpiryTime().isAfter(Instant.now()));
    }

    @Test
    void createRefreshToken_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.createRefreshToken(userId)

        );

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void createRefreshToken_TokenAlreadyExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newTestRefreshToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        // Assert
        assertNotNull(result);
        assertEquals(newTestRefreshToken, result);
        assertNotEquals(testRefreshToken, result);

        // Verify token is created with correct properties
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken capturedToken = tokenCaptor.getValue();
        assertNotNull(capturedToken.getToken());
        // Check that token length is greater than 40 characters
        assertTrue(capturedToken.getToken().length() > 40,
                "Token should be longer than 40 characters for security reasons");
        assertEquals(testUser, capturedToken.getUser());
        assertNotNull(capturedToken.getExpiryTime());
        assertTrue(capturedToken.getExpiryTime().isAfter(Instant.now()));
    }

    @Test
    void deleteRefreshToken_TokenExists() {
        // Arrange
        String token = "token-to-delete";
        RefreshToken tokenToDelete = new RefreshToken();
        tokenToDelete.setToken(token);

        when(refreshTokenRepository.findByToken(token)).thenReturn(tokenToDelete);

        // Act
        refreshTokenService.deleteRefreshToken(token);

        // Assert
        verify(refreshTokenRepository).delete(tokenToDelete);
    }

    @Test
    void deleteRefreshToken_TokenNotFound() {
        // Arrange
        String token = "non-existent-token";
        when(refreshTokenRepository.findByToken(token)).thenReturn(null);

        // Act
        refreshTokenService.deleteRefreshToken(token);

        // Assert
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void getAndVerifyRefreshToken_ValidToken() {
        // Arrange
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(testRefreshToken);

        // Act
        Optional<RefreshToken> result = refreshTokenService.getAndVerifyRefreshToken("test-token");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRefreshToken, result.get());
    }

    @Test
    void getAndVerifyRefreshToken_TokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(null);

        // Act
        Optional<RefreshToken> result = refreshTokenService.getAndVerifyRefreshToken("invalid-token");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAndVerifyRefreshToken_ExpiredToken() {
        // Arrange
        testRefreshToken.setExpiryTime(Instant.now().minusSeconds(3600)); // Expired 1 hour ago
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(testRefreshToken);

        // Act
        Optional<RefreshToken> result = refreshTokenService.getAndVerifyRefreshToken("test-token");

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository).delete(testRefreshToken);
    }
}