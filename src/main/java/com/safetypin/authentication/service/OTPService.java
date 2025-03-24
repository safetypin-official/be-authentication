package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.OTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class OTPService {
    private static final long OTP_EXPIRATION_SECONDS = 120; // 2 minutes expiration
    private static final Logger log = LoggerFactory.getLogger(OTPService.class);
    private static final long RESET_TOKEN_EXPIRATION_SECONDS = 180; // 3 minutes
    private final EmailService emailService;
    private final ConcurrentHashMap<String, OTPDetails> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    // Store reset tokens with their expiration time
    private final ConcurrentHashMap<String, ResetTokenDetails> resetTokenStorage = new ConcurrentHashMap<>();

    @Autowired
    public OTPService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        OTPDetails details = new OTPDetails(otp, LocalDateTime.now());
        otpStorage.put(email, details);

        try {
            boolean status = emailService.sendOTPMail(email, otp).get();
            if (!status) {
                throw new OTPException("Failed to send OTP");
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new OTPException("Failed to send OTP: " + e.getMessage());
        }

        log.info("Sending OTP {} to {}", otp, email);
        return otp;
    }

    public boolean verifyOTP(String email, String otp) {
        if (otp == null) {
            throw new NullPointerException("OTP cannot be null");
        }

        OTPDetails details = otpStorage.get(email);
        if (details == null) {
            return false;
        }
        // Check if OTP has expired
        if (details.generatedAt().plusSeconds(OTP_EXPIRATION_SECONDS).isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            return false;
        }
        if (details.otp().equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }

    /**
     * Generate a reset token after OTP verification
     *
     * @param email the email address
     * @return the reset token
     */
    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        resetTokenStorage.put(token, new ResetTokenDetails(email, LocalDateTime.now()));
        log.info("Generated reset token for {}", email);
        return token;
    }

    /**
     * Verify if a reset token is valid
     *
     * @param token the reset token
     * @param email the email associated with the token
     * @return true if valid, false otherwise
     */
    public boolean verifyResetToken(String token, String email) {
        ResetTokenDetails details = resetTokenStorage.get(token);

        if (details == null) {
            log.warn("Reset token not found: {}", token);
            return false;
        }

        // Check if token has expired
        if (details.generatedAt.plusSeconds(RESET_TOKEN_EXPIRATION_SECONDS).isBefore(LocalDateTime.now())) {
            log.warn("Reset token expired: {}", token);
            resetTokenStorage.remove(token);
            return false;
        }

        // Check if token matches the email
        if (!details.email.equals(email)) {
            log.warn("Email mismatch for token. Expected: {}, Actual: {}", details.email, email);
            return false;
        }

        // Token is valid, remove it to prevent reuse
        resetTokenStorage.remove(token);
        return true;
    }

    private record OTPDetails(String otp, LocalDateTime generatedAt) {
    }

    private record ResetTokenDetails(String email, LocalDateTime generatedAt) {
    }
}
