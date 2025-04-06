package com.safetypin.authentication.service.passwordreset;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.OTPService;
import com.safetypin.authentication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyOTPStateTest {

    @Mock
    private UserService userService;

    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetContext context;
    private VerifyOTPState verifyOTPState;
    private User testUser;

    @BeforeEach
    void setUp() {
        context = new PasswordResetContext(userService, otpService, passwordEncoder);
        verifyOTPState = new VerifyOTPState();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setProvider("EMAIL");
    }

    @Test
    void testHandleSuccessfulVerification() {
        // Arrange
        final String email = "test@example.com";
        final String otp = "123456";
        final String resetToken = "token123";

        context.setEmail(email);
        context.setOtp(otp);
        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(email, otp)).thenReturn(true);
        when(otpService.generateResetToken(email)).thenReturn(resetToken);

        // Act
        Object result = verifyOTPState.handle(context);

        // Assert
        assertEquals(resetToken, result);
        verify(userService).findByEmail(email);
        verify(otpService).verifyOTP(email, otp);
        verify(otpService).generateResetToken(email);
    }

    @Test
    void testHandleFailedVerification() {
        // Arrange
        final String email = "test@example.com";
        final String otp = "123456";

        context.setEmail(email);
        context.setOtp(otp);
        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(email, otp)).thenReturn(false);

        // Act
        Object result = verifyOTPState.handle(context);

        // Assert
        assertNull(result);
        verify(userService).findByEmail(email);
        verify(otpService).verifyOTP(email, otp);
        verify(otpService, never()).generateResetToken(anyString());
    }

    @Test
    void testHandleUserNotFound() {
        // Arrange
        final String email = "nonexistent@example.com";
        context.setEmail(email);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> verifyOTPState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).verifyOTP(anyString(), anyString());
        verify(otpService, never()).generateResetToken(anyString());
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
                () -> verifyOTPState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).verifyOTP(anyString(), anyString());
        verify(otpService, never()).generateResetToken(anyString());
    }
}