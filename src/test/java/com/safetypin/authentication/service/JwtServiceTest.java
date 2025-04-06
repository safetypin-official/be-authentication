package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private final UUID userId = UUID.randomUUID();
    private final User mockUser = mock(User.class);
    private final UserResponse mockUserResponse = mock(UserResponse.class);
    @Mock
    private UserService userService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        // Create JwtService instance with the mocked UserService and test key pair
        String secretKey = "justanormalsecretkeyfortestingnothingsuspicioushere";
        jwtService = new JwtService(secretKey, userService);
    }

    @Test
    void constructor_shouldInitializeKeyAndUserService() {
        // Verify constructor properly initializes the service
        assertNotNull(jwtService);
    }

    @Test
    void generateToken_shouldCreateValidJwt() {
        // Setup mock user
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(com.safetypin.authentication.model.Role.REGISTERED_USER);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Verify token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify token can be parsed
        Claims claims = jwtService.parseToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertFalse(claims.getExpiration().before(new Date()));
    }

    @Test
    void parseToken_shouldDecodeValidToken() {
        // Setup mock user for token generation
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(com.safetypin.authentication.model.Role.REGISTERED_USER);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Parse the token
        Claims claims = jwtService.parseToken(token);

        // Verify claims
        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void parseToken_shouldThrowExceptionForInvalidToken() {
        // Invalid token
        String invalidToken = "invalid.token.string";

        // Verify exception is thrown
        assertThrows(JwtException.class, () -> jwtService.parseToken(invalidToken));
    }

    @Test
    void getUserFromJwtToken_shouldReturnUserForValidToken() throws InvalidCredentialsException {
        // Setup mock user and response
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(com.safetypin.authentication.model.Role.REGISTERED_USER);
        when(mockUser.generateUserResponse()).thenReturn(mockUserResponse);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Reset the mock to clear previous interactions
        reset(userService);

        // Setup mock again for the getUserFromJwtToken call
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.generateUserResponse()).thenReturn(mockUserResponse);
        // Get user from token
        UserResponse result = jwtService.getUserFromJwtToken(token);

        // Verify result
        assertSame(mockUserResponse, result);
        verify(userService).findById(userId); // Now verifying only one call
    }

    @Test
    void getUserFromJwtToken_shouldThrowExceptionWhenUserNotFound() {
        // Setup mock user for token generation
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(com.safetypin.authentication.model.Role.REGISTERED_USER);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Then setup user not found scenario
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Verify exception is thrown
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> jwtService.getUserFromJwtToken(token)
        );

        assertEquals("User not found", exception.getMessage());
    }
}