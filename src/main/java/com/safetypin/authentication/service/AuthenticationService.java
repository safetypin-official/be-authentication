package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class AuthenticationService {
    public static final String EMAIL_PROVIDER = "EMAIL";
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final JwtService jwtService;

    public AuthenticationService(UserService userService, PasswordEncoder passwordEncoder, OTPService otpService, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    // Registration using email – includes birthdate and OTP generation
    public String registerUser(RegistrationRequest request) {
        if (calculateAge(request.getBirthdate()) < 16) {
            throw new IllegalArgumentException("User must be at least 16 years old");
        }
        Optional<User> existingUser = userService.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Email address is already registered. If you previously used social login (Google/Apple), please use that method to sign in.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // save user to repository
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setVerified(false);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(request.getBirthdate());
        user.setProvider(EMAIL_PROVIDER);
        user = userService.save(user);

        otpService.generateOTP(request.getEmail());
        logger.info("OTP generated for user at {}", java.time.LocalDateTime.now());

        return jwtService.generateToken(user.getId());
    }

    // Email login with detailed error messages
    public String loginUser(String email, String rawPassword) {
        Optional<User> findUser = userService.findByEmail(email);
        if (findUser.isEmpty()) {
            // email not exists
            logger.warn("Login failed: Email not found");
            throw new InvalidCredentialsException("Invalid email");
        }
        User user = findUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            // incorrect password
            logger.warn("Login failed: Incorrect password attempt");
            throw new InvalidCredentialsException("Invalid password");
        }
        logger.info("User logged in at {}", java.time.LocalDateTime.now());
        return jwtService.generateToken(user.getId());
    }

    // OTP verification – marks user as verified upon success
    public boolean verifyOTP(String email, String otp) {
        boolean result = otpService.verifyOTP(email, otp);
        if (result) {
            Optional<User> findUser = userService.findByEmail(email);
            if (findUser.isPresent()) {
                User user = findUser.get();
                user.setVerified(true);
                userService.save(user);
                logger.info("OTP successfully verified at {}", java.time.LocalDateTime.now());
            }
        } else {
            logger.warn("OTP verification failed at {}", java.time.LocalDateTime.now());
        }
        return result;
    }

    // Forgot password – initiates password reset flow
    public void forgotPassword(String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException("Password reset is only available for email-registered users.");
        }

        // Generate OTP for password reset
        String otp = otpService.generateOTP(email);

        // In production, send the OTP via email
        logger.info("Password reset OTP generated for email {} at {}: {}", email, java.time.LocalDateTime.now(), otp);
    }

    // Verify OTP for password reset and generate a reset token
    public String verifyPasswordResetOTP(String email, String otp) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException("Password reset is only available for email-registered users.");
        }

        boolean isValid = otpService.verifyOTP(email, otp);
        if (isValid) {
            // Generate a reset token valid for 3 minutes
            String resetToken = otpService.generateResetToken(email);
            logger.info("Password reset OTP verified for {}. Reset token generated at {}", email, java.time.LocalDateTime.now());
            return resetToken;
        } else {
            logger.warn("Invalid password reset OTP attempt for {} at {}", email, java.time.LocalDateTime.now());
            return null;
        }
    }

    // Reset password using the reset token
    public void resetPassword(String email, String newPassword, String resetToken) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException("Password reset is only available for email-registered users.");
        }

        if (resetToken == null || !otpService.verifyResetToken(resetToken, email)) {
            throw new InvalidCredentialsException("Invalid or expired reset token. Please request a new OTP.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        logger.info("Password reset successfully for {} at {}", email, java.time.LocalDateTime.now());
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

}
