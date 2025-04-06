package com.safetypin.authentication.service.passwordreset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.OTPService;
import com.safetypin.authentication.service.UserService;

@ExtendWith(MockitoExtension.class)
class InitiateResetStateTest {

    @Mock
    private UserService userService;

    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetContext context;
    private InitiateResetState initiateResetState;
    private User testUser;

    @BeforeEach
    void setUp() {
        context = new PasswordResetContext(userService, otpService, passwordEncoder);
        initiateResetState = new InitiateResetState();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setProvider("EMAIL");
    }

    @Test
    void testHandleSuccess() {
        // Arrange
        final String email = "test@example.com";
        final String otp = "123456";

        context.setEmail(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.generateOTP(email)).thenReturn(otp);

        // Act
        Object result = initiateResetState.handle(context);

        // Assert
        assertNull(result);
        verify(userService).findByEmail(email);
        verify(otpService).generateOTP(email);
    }

    @Test
    void testHandleUserNotFound() {
        // Arrange
        final String email = "nonexistent@example.com";
        context.setEmail(email);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> initiateResetState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).generateOTP(anyString());
    }

    @Test
    void testHandleNonEmailProvider() {
        // Arrange
        final String email = "social@example.com";
        User socialUser = new User();
        socialUser.setEmail(email);
        socialUser.setProvider("GOOGLE");

        context.setEmail(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(socialUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> initiateResetState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).generateOTP(anyString());
    }
}