package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceRS256Test {

    private final UUID userId = UUID.randomUUID();
    @Mock
    private UserService userService;
    private JwtService jwtService;
    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {

        // Create JwtService instance with the test key pair
        String secretKey = "justanormalsecretkeyfortestingnothingsuspicioushere";
        jwtService = new JwtService(secretKey, userService);

        // Create a proper mock for User
        mockUser = mock(User.class);

        // Setup mock user response
        mockUserResponse = mock(UserResponse.class);
    }

    @Test
    void generateToken_shouldCreateValidJwtWithUserClaims() {
        // Setup only the necessary mocks for this test
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(Role.REGISTERED_USER);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Verify token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Extract claims and verify they contain the expected user information
        Claims claims = jwtService.parseToken(token);

        assertEquals(userId.toString(), claims.get("userId"));
        assertEquals("Test User", claims.get("name"));
        assertEquals(true, claims.get("isVerified"));
        assertEquals("REGISTERED_USER", claims.get("role"));

        // Verify mocks were used as expected
        verify(userService, times(1)).findById(userId);
        verify(mockUser, times(1)).getName();
        verify(mockUser, times(1)).isVerified();
        verify(mockUser, times(1)).getRole();
    }

    @Test
    void parseToken_shouldDecodeValidToken() {
        // Setup only the necessary mocks for this test
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(Role.REGISTERED_USER);

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Parse the token
        Claims claims = jwtService.parseToken(token);

        // Verify claims
        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        // Verify mocks were used as expected
        verify(userService, times(1)).findById(userId);
    }

    @Test
    void getUserFromJwtToken_shouldReturnUserForValidToken() throws InvalidCredentialsException {
        // Setup specific mocks for this test
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.isVerified()).thenReturn(true);
        when(mockUser.getRole()).thenReturn(Role.REGISTERED_USER);
        when(mockUser.generateUserResponse()).thenReturn(mockUserResponse);
        when(mockUserResponse.getId()).thenReturn(userId);
        when(mockUserResponse.getName()).thenReturn("Test User");

        // Generate a token
        String token = jwtService.generateToken(userId);

        // Clear invocations instead of resetting to maintain stubbing
        clearInvocations(userService);

        // Get user from token
        UserResponse userResponse = jwtService.getUserFromJwtToken(token);

        // Verify user response
        assertNotNull(userResponse);
        assertEquals(userId, userResponse.getId());
        assertEquals("Test User", userResponse.getName());

        // Verify userService was called exactly once after clearing invocations
        verify(userService, times(1)).findById(userId);
    }
}
