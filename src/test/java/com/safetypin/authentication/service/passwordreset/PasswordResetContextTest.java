package com.safetypin.authentication.service.passwordreset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class PasswordResetContextTest {

    @Mock
    private UserService userService;

    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetState mockState;

    private PasswordResetContext context;
    private User testUser;

    @BeforeEach
    void setUp() {
        context = new PasswordResetContext(userService, otpService, passwordEncoder);
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setProvider("EMAIL");
    }

    @Test
    void testProcessState() {
        // Arrange
        context.setState(mockState);
        when(mockState.handle(context)).thenReturn("result");

        // Act
        Object result = context.processState();

        // Assert
        assertEquals("result", result);
        verify(mockState).handle(context);
    }

    @Test
    void testFindUserByEmail() {
        // Arrange
        context.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = context.findUserByEmail();

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userService).findByEmail("test@example.com");
    }

    @Test
    void testGenerateOTP() {
        // Arrange
        context.setEmail("test@example.com");
        when(otpService.generateOTP("test@example.com")).thenReturn("123456");

        // Act
        String otp = context.generateOTP();

        // Assert
        assertEquals("123456", otp);
        verify(otpService).generateOTP("test@example.com");
    }

    @Test
    void testVerifyOTP() {
        // Arrange
        context.setEmail("test@example.com");
        context.setOtp("123456");
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(true);

        // Act
        boolean result = context.verifyOTP();

        // Assert
        assertTrue(result);
        verify(otpService).verifyOTP("test@example.com", "123456");
    }

    @Test
    void testGenerateResetToken() {
        // Arrange
        context.setEmail("test@example.com");
        when(otpService.generateResetToken("test@example.com")).thenReturn("token123");

        // Act
        String token = context.generateResetToken();

        // Assert
        assertEquals("token123", token);
        verify(otpService).generateResetToken("test@example.com");
    }

    @Test
    void testVerifyResetToken() {
        // Arrange
        context.setEmail("test@example.com");
        context.setResetToken("token123");
        when(otpService.verifyResetToken("token123", "test@example.com")).thenReturn(true);

        // Act
        boolean result = context.verifyResetToken();

        // Assert
        assertTrue(result);
        verify(otpService).verifyResetToken("token123", "test@example.com");
    }

    @Test
    void testSaveUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        // Act
        context.saveUser(user);

        // Assert
        verify(userService).save(user);
    }

    @Test
    void testEncodePassword() {
        // Arrange
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String result = context.encodePassword("password");

        // Assert
        assertEquals("encodedPassword", result);
        verify(passwordEncoder).encode("password");
    }

    @Test
    void testGettersAndSetters() {
        // Arrange & Act
        context.setEmail("email@test.com");
        context.setOtp("654321");
        context.setResetToken("resetToken123");
        context.setNewPassword("newPassword");
        context.setUser(testUser);

        // Assert
        assertEquals("email@test.com", context.getEmail());
        assertEquals("654321", context.getOtp());
        assertEquals("resetToken123", context.getResetToken());
        assertEquals("newPassword", context.getNewPassword());
        assertEquals(testUser, context.getUser());
    }
}