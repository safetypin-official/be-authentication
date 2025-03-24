package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OTPService otpService;

    @Mock
    private JwtService jwtService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userService, passwordEncoder, otpService, jwtService);
    }

    // registerUser tests

    @Test
    void testRegisterUser_UnderAge() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        // set birthdate to 15 years old
        request.setBirthdate(LocalDate.now().minusYears(15));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.registerUser(request)
        );
        assertEquals("User must be at least 16 years old", exception.getMessage());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        User existingUser = new User();
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
                authenticationService.registerUser(request)
        );
        assertTrue(exception.getMessage().contains("Email address is already registered"));
    }

    @Test
    void testRegisterUser_Success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setName("Test User");
        savedUser.setVerified(false);
        savedUser.setRole(Role.REGISTERED_USER);
        savedUser.setBirthdate(request.getBirthdate());
        savedUser.setProvider("EMAIL");

        UUID id = UUID.randomUUID();
        savedUser.setId(id);

        when(userService.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(id)).thenReturn("jwtToken");

        String token = authenticationService.registerUser(request);

        assertNotNull(token);
        assertEquals("jwtToken", token);
        verify(otpService, times(1)).generateOTP("test@example.com");
        verify(userService, times(1)).save(any(User.class));
    }

    // loginUser tests

    @Test
    void testLoginUser_EmailNotFound() {
        when(userService.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("notfound@example.com", "password")
        );

        assertEquals("Invalid email", exception.getMessage());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("test@example.com", "wrongPassword")
        );

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");

        UUID id = UUID.randomUUID();
        user.setId(id);

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(id)).thenReturn("jwtToken");

        String token = authenticationService.loginUser("test@example.com", "password");

        assertNotNull(token);
        assertEquals("jwtToken", token);
    }

    // verifyOTP tests

    @Test
    void testVerifyOTP_Success() {
        // OTPService returns true and user is found
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(true);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(false);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenReturn(user);

        boolean result = authenticationService.verifyOTP("test@example.com", "123456");

        assertTrue(result);
        assertTrue(user.isVerified());
        verify(userService, times(1)).save(user);
    }

    @Test
    void testVerifyOTP_Success_UserNotFound() {
        // OTPService returns true but user is not found
        when(otpService.verifyOTP("nonexistent@example.com", "123456")).thenReturn(true);
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = authenticationService.verifyOTP("nonexistent@example.com", "123456");

        assertTrue(result);
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testVerifyOTP_Failure() {
        when(otpService.verifyOTP("test@example.com", "000000")).thenReturn(false);

        boolean result = authenticationService.verifyOTP("test@example.com", "000000");

        assertFalse(result);
        verify(userService, never()).save(any(User.class));
    }

    // forgotPassword tests

    @Test
    void testForgotPassword_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateOTP("test@example.com")).thenReturn("123456");

        assertDoesNotThrow(() -> authenticationService.forgotPassword("test@example.com"));
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userService.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.forgotPassword("notfound@example.com")
        );

        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
    }

    @Test
    void testForgotPassword_NonEmailProvider() {
        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(25));
        user.setProvider("GOOGLE");

        when(userService.findByEmail("social@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.forgotPassword("social@example.com")
        );

        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
    }

    @Test
    void testVerifyPasswordResetOTP_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(true);
        when(otpService.generateResetToken("test@example.com")).thenReturn("reset-token-123");

        String resetToken = authenticationService.verifyPasswordResetOTP("test@example.com", "123456");
        assertNotNull(resetToken);
        assertEquals("reset-token-123", resetToken);
    }

    @Test
    void testVerifyPasswordResetOTP_Failure() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(false);

        String resetToken = authenticationService.verifyPasswordResetOTP("test@example.com", "123456");
        assertNull(resetToken);
    }

    @Test
    void testVerifyPasswordResetOTP_UserNotFound() {
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.verifyPasswordResetOTP("nonexistent@example.com", "123456")
        );

        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
        verify(otpService, never()).verifyOTP(anyString(), anyString());
    }

    @Test
    void testVerifyPasswordResetOTP_NonEmailProvider() {
        User user = new User();
        user.setEmail("social@example.com");
        user.setProvider("GOOGLE");

        when(userService.findByEmail("social@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.verifyPasswordResetOTP("social@example.com", "123456")
        );

        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
        verify(otpService, never()).verifyOTP(anyString(), anyString());
    }

    @Test
    void testResetPassword_NonEmailProvider_WithoutOTP() {
        User user = new User();
        user.setEmail("social@example.com");
        user.setProvider("GOOGLE");

        when(userService.findByEmail("social@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.resetPassword("social@example.com", "newPassword", "reset-token")
        );

        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldEncodedPassword");
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyResetToken("valid-token", "test@example.com")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        authenticationService.resetPassword("test@example.com", "newPassword", "valid-token");

        verify(userService).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    void testResetPassword_InvalidToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyResetToken("invalid-token", "test@example.com")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.resetPassword("test@example.com", "newPassword", "invalid-token")
        );

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_NullToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setProvider("EMAIL");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.resetPassword("test@example.com", "newPassword", null)
        );

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_UserNotFound() {
        // Simulate user not found scenario
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.resetPassword("nonexistent@example.com", "newPassword", "reset-token")
        );
        
        assertTrue(exception.getMessage().contains("Password reset is only available for email-registered users"));
        verify(otpService, never()).verifyResetToken(anyString(), anyString());
        verify(userService, never()).save(any(User.class));
    }
}