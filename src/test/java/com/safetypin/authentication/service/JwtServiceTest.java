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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private UserService userService;

    private JwtService jwtService;

    private final String secretKey = "testSecretKeyWithAtLeast256BitsForHmacSha256Algorithm";
    private final UUID userId = UUID.randomUUID();
    private final User mockUser = mock(User.class);
    private final UserResponse mockUserResponse = mock(UserResponse.class);

    @BeforeEach
    void setUp() {
        // Create JwtService instance with the mocked UserService and test secret key
        jwtService = new JwtService(secretKey, userService);
    }

    @Test
    void constructor_shouldInitializeKeyAndUserService() {
        // Verify constructor properly initializes the service
        assertNotNull(jwtService);
    }

    @Test
    void generateToken_shouldCreateValidJwt() {
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
        // Set up mock responses
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.generateUserResponse()).thenReturn(mockUserResponse);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Get user from token
        UserResponse response = jwtService.getUserFromJwtToken(token);

        // Verify result
        assertSame(mockUserResponse, response);
        verify(userService).findById(userId);
        verify(mockUser).generateUserResponse();
    }

    @Test
    void getUserFromJwtToken_shouldThrowExceptionForExpiredToken() throws Exception {
        // Create a JwtService with a custom expiration time
        JwtService shortExpirationJwtService = new JwtService(secretKey, userService);

        // Create a new token with an expiration date in the past
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1 second in the past

        // Use reflection to mock the parseToken method to return expired claims
        JwtService spyService = spy(shortExpirationJwtService);
        Claims expiredClaims = mock(Claims.class);
        when(expiredClaims.getExpiration()).thenReturn(pastDate);
        when(expiredClaims.getSubject()).thenReturn(userId.toString());
        doReturn(expiredClaims).when(spyService).parseToken(anyString());

        // Verify exception is thrown
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> spyService.getUserFromJwtToken("expired-token")
        );
        assertEquals("Token expired", exception.getMessage());
    }

    @Test
    void getUserFromJwtToken_shouldThrowExceptionWhenUserNotFound() {
        // Set up mock to return empty optional
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Generate token
        String token = jwtService.generateToken(userId);

        // Verify exception is thrown
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> jwtService.getUserFromJwtToken(token)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
    }

    @Test
    void getUserFromJwtToken_shouldHandleInvalidToken() {
        // Invalid token
        String invalidToken = "invalid.token.string";

        // Verify exception is thrown
        assertThrows(JwtException.class, () -> jwtService.getUserFromJwtToken(invalidToken));
    }
}