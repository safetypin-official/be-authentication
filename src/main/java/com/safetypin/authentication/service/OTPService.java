package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.OTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class OTPService {
    private static final long OTP_EXPIRATION_SECONDS = 120; // 2 minutes expiration
    private static final Logger log = LoggerFactory.getLogger(OTPService.class);
    private final EmailService emailService;
    private final ConcurrentHashMap<String, OTPDetails> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

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

    private record OTPDetails(String otp, LocalDateTime generatedAt) {
    }
}
