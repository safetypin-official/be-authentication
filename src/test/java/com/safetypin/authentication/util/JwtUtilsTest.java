package com.safetypin.authentication.util;

import com.safetypin.authentication.constants.ApiConstants;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String testToken = "test.jwt.token";
    private final String testAuthHeader = ApiConstants.BEARER_PREFIX + testToken;
    private final UserResponse testUser = UserResponse.builder()
            .id(java.util.UUID.fromString("550e8400-e29b-41d4-a716-446655440000")) // UUID format
            .email("test@example.com")
            .build();

    @BeforeEach
    void setUp() {
        // Setup test user data
    }

    @Test
    void parseUserFromAuthHeader_Success() throws InvalidCredentialsException {
        // Arrange
        when(jwtService.getUserFromJwtToken(testToken)).thenReturn(testUser);

        // Act
        UserResponse result = jwtUtils.parseUserFromAuthHeader(testAuthHeader);

        // Assert
        assertEquals(testUser, result);
        verify(jwtService).getUserFromJwtToken(testToken);
    }

    @Test
    void parseUserFromAuthHeader_InvalidCredentialsException() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Token expired");
        when(jwtService.getUserFromJwtToken(testToken)).thenThrow(exception);

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(
            InvalidCredentialsException.class,
            () -> jwtUtils.parseUserFromAuthHeader(testAuthHeader)
        );
        
        // Should propagate the original exception
        assertSame(exception, thrown);
        verify(jwtService).getUserFromJwtToken(testToken);
    }

    @Test
    void parseUserFromAuthHeader_OtherException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Some other error");
        when(jwtService.getUserFromJwtToken(testToken)).thenThrow(exception);

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(
            InvalidCredentialsException.class,
            () -> jwtUtils.parseUserFromAuthHeader(testAuthHeader)
        );
        
        // Should wrap with InvalidCredentialsException
        assertEquals("Invalid token", thrown.getMessage());
        verify(jwtService).getUserFromJwtToken(testToken);
    }

    @Test
    void parseUserFromAuthHeaderSafe_Success() {
        // Arrange
        when(jwtService.getUserFromJwtToken(testToken)).thenReturn(testUser);

        // Act
        UserResponse result = jwtUtils.parseUserFromAuthHeaderSafe(testAuthHeader);

        // Assert
        assertEquals(testUser, result);
        verify(jwtService).getUserFromJwtToken(testToken);
    }

    @Test
    void parseUserFromAuthHeaderSafe_ReturnsNullOnException() {
        // Arrange
        when(jwtService.getUserFromJwtToken(testToken)).thenThrow(new RuntimeException("Some error"));

        // Act
        UserResponse result = jwtUtils.parseUserFromAuthHeaderSafe(testAuthHeader);

        // Assert
        assertNull(result);
        verify(jwtService).getUserFromJwtToken(testToken);
    }
}