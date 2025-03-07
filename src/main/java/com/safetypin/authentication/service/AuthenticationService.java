package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

@Service
public class AuthenticationService {
    public static final String EMAIL_PROVIDER = "EMAIL";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, OTPService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    // Registration using email – includes birthdate and OTP generation
    public User registerUser(RegistrationRequest request) {
        if (calculateAge(request.getBirthdate()) < 16) {
            throw new IllegalArgumentException("User must be at least 16 years old");
        }
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("Email address is already registered. If you previously used social login (Google/Apple), please use that method to sign in.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setVerified(false);
        user.setRole("USER");
        user.setBirthdate(request.getBirthdate());
        user.setProvider(EMAIL_PROVIDER);
        user.setSocialId(null);
        user = userRepository.save(user);
        otpService.generateOTP(request.getEmail());
        logger.info("OTP generated for user at {}", java.time.LocalDateTime.now());
        return user;
    }

    // Social registration/login – simulating data fetched from Google/Apple
    public User socialLogin(SocialLoginRequest request) {
        if (calculateAge(request.getBirthdate()) < 16) {
            throw new IllegalArgumentException("User must be at least 16 years old");
        }
        User existing = userRepository.findByEmail(request.getEmail());
        if (existing != null) {
            if (EMAIL_PROVIDER.equals(existing.getProvider())) {
                throw new UserAlreadyExistsException("An account with this email exists. Please sign in using your email and password.");
            }
            return existing;
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(null);
        user.setName(request.getName());
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(request.getBirthdate());
        user.setProvider(request.getProvider().toUpperCase());
        user.setSocialId(request.getSocialId());

        user = userRepository.save(user);
        logger.info("User registered via social login at {}", java.time.LocalDateTime.now());
        return user;
    }

    // Email login with detailed error messages
    public User loginUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // email not exists
            logger.warn("Login failed: Email not found");
            throw new InvalidCredentialsException("Invalid email");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            // incorrect password
            logger.warn("Login failed: Incorrect password attempt");
            throw new InvalidCredentialsException("Invalid password");
        }
        logger.info("User logged in at {}", java.time.LocalDateTime.now());
        return user;
    }

    // Social login verification (assumed to be pre-verified externally)
    public User loginSocial(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidCredentialsException("Social login failed: Email not found");
        }
        logger.info("User logged in via social authentication at {}", java.time.LocalDateTime.now());
        return user;
    }

    // OTP verification – marks user as verified upon success
    public boolean verifyOTP(String email, String otp) {
        boolean result = otpService.verifyOTP(email, otp);
        if (result) {
            User user = userRepository.findByEmail(email);
            if (user != null) {
                user.setVerified(true);
                userRepository.save(user);
                logger.info("OTP successfully verified at {}", java.time.LocalDateTime.now());
            }
        } else {
            logger.warn("OTP verification failed at {}", java.time.LocalDateTime.now());
        }
        return result;
    }

    // Forgot password – only applicable for email-registered users
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || !EMAIL_PROVIDER.equals(user.getProvider())) {
            throw new IllegalArgumentException("Password reset is only available for email-registered users.");
        }
        // In production, send a reset token via email.
        logger.info("Password reset requested at {}", java.time.LocalDateTime.now());
    }

    // Example method representing posting content that requires a verified account
    public String postContent(String email, String content) { // NOSONAR
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found. Please register.";
        }
        if (!user.isVerified()) {
            return "Your account is not verified. Please complete OTP verification. You may request a new OTP after 2 minutes.";
        }
        logger.info("Content posted successfully by user");
        // For demo purposes, we assume the post is successful.
        return "Content posted successfully";
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
