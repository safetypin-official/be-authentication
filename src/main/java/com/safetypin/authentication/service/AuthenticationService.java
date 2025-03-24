package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.AuthToken;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.RefreshToken;
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
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthenticationService(
            UserService userService, PasswordEncoder passwordEncoder,
            OTPService otpService, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // Registration using email – includes birthdate and OTP generation
    public AuthToken registerUser(RegistrationRequest request) {
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

        String accessToken = jwtService.generateToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        logger.info("User registered at {}", java.time.LocalDateTime.now());
        return new AuthToken(accessToken, refreshToken.getToken());
    }

    // Email login with detailed error messages
    public AuthToken loginUser(String email, String rawPassword) {
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
        String accessToken = jwtService.generateToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        logger.info("User logged in at {}", java.time.LocalDateTime.now());
        return new AuthToken(accessToken, refreshToken.getToken());
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

    // Forgot password – only applicable for email-registered users
    public void forgotPassword(String email) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty() || !EMAIL_PROVIDER.equals(user.get().getProvider())) {
            throw new IllegalArgumentException("Password reset is only available for email-registered users.");
        }
        // In production, send a reset token via email.
        logger.info("Password reset requested at {}", java.time.LocalDateTime.now());
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

}
