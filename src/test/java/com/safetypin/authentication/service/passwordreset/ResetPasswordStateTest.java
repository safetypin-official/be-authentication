package com.safetypin.authentication.service.passwordreset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.OTPService;
import com.safetypin.authentication.service.UserService;

@ExtendWith(MockitoExtension.class)
class ResetPasswordStateTest {

    @Mock
    private UserService userService;

    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private PasswordResetContext context;
    private ResetPasswordState resetPasswordState;
    private User testUser;

    @BeforeEach
    void setUp() {
        context = new PasswordResetContext(userService, otpService, passwordEncoder);
        resetPasswordState = new ResetPasswordState();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setProvider("EMAIL");
        testUser.setPassword("oldPassword");
    }

    @Test
    void testHandleSuccessfulReset() {
        // Arrange
        final String email = "test@example.com";
        final String newPassword = "newPassword123";
        final String encodedPassword = "encodedNewPassword";
        final String resetToken = "validToken";

        context.setEmail(email);
        context.setNewPassword(newPassword);
        context.setResetToken(resetToken);

        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.verifyResetToken(resetToken, email)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // Act
        Object result = resetPasswordState.handle(context);

        // Assert
        assertNull(result);
        verify(userService).findByEmail(email);
        verify(otpService).verifyResetToken(resetToken, email);
        verify(passwordEncoder).encode(newPassword);
        verify(userService).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals(email, savedUser.getEmail());
    }

    @Test
    void testHandleWithInvalidToken() {
        // Arrange
        final String email = "test@example.com";
        final String newPassword = "newPassword123";
        final String resetToken = "invalidToken";

        context.setEmail(email);
        context.setNewPassword(newPassword);
        context.setResetToken(resetToken);

        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(otpService.verifyResetToken(resetToken, email)).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> resetPasswordState.handle(context));

        assertEquals("Invalid or expired reset token. Please request a new OTP.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService).verifyResetToken(resetToken, email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testHandleWithNullToken() {
        // Arrange
        final String email = "test@example.com";
        final String newPassword = "newPassword123";

        context.setEmail(email);
        context.setNewPassword(newPassword);
        context.setResetToken(null);

        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> resetPasswordState.handle(context));

        assertEquals("Invalid or expired reset token. Please request a new OTP.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).verifyResetToken(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testHandleUserNotFound() {
        // Arrange
        final String email = "nonexistent@example.com";
        final String newPassword = "newPassword123";
        final String resetToken = "validToken";

        context.setEmail(email);
        context.setNewPassword(newPassword);
        context.setResetToken(resetToken);

        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> resetPasswordState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).verifyResetToken(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testHandleNonEmailProvider() {
        // Arrange
        final String email = "social@example.com";
        final String newPassword = "newPassword123";
        final String resetToken = "validToken";

        User socialUser = new User();
        socialUser.setEmail(email);
        socialUser.setProvider("GOOGLE");

        context.setEmail(email);
        context.setNewPassword(newPassword);
        context.setResetToken(resetToken);

        when(userService.findByEmail(email)).thenReturn(Optional.of(socialUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> resetPasswordState.handle(context));

        assertEquals("Password reset is only available for email-registered users.", exception.getMessage());
        verify(userService).findByEmail(email);
        verify(otpService, never()).verifyResetToken(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }
}