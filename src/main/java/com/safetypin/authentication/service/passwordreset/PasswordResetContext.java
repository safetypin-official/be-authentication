package com.safetypin.authentication.service.passwordreset;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.OTPService;
import com.safetypin.authentication.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Context class for managing password reset states and data
 */
public class PasswordResetContext {
    private final UserService userService;
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;
    // Setters for state and data
    @Setter
    private PasswordResetState state;
    // Getters for state and data
    // Data fields for password reset process
    @Getter
    @Setter
    private String email;
    @Setter
    @Getter
    private String otp;
    @Getter
    @Setter
    private String resetToken;
    @Getter
    @Setter
    private String newPassword;
    @Setter
    @Getter
    private User user;

    public PasswordResetContext(UserService userService, OTPService otpService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    public Object processState() {
        return state.handle(this);
    }

    // Convenience methods for states
    public Optional<User> findUserByEmail() {
        return userService.findByEmail(email);
    }

    public String generateOTP() {
        return otpService.generateOTP(email);
    }

    public boolean verifyOTP() {
        return otpService.verifyOTP(email, otp);
    }

    public String generateResetToken() {
        return otpService.generateResetToken(email);
    }

    public boolean verifyResetToken() {
        return otpService.verifyResetToken(resetToken, email);
    }

    public void saveUser(User user) {
        userService.save(user);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
