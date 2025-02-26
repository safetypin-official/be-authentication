package com.safetypin.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private static final long OTP_EXPIRATION_SECONDS = 120; // 2 minutes expiration
    private static final Logger log = LoggerFactory.getLogger(OTPService.class);
    private final ConcurrentHashMap<String, OTPDetails> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        OTPDetails details = new OTPDetails(otp, LocalDateTime.now());
        otpStorage.put(email, details);
        // Simulate sending OTP via email (in production, integrate with an email service)
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
