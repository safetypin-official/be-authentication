package com.safetypin.authentication.service.passwordreset;

import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * State for initiating the password reset process
 */
public class InitiateResetState implements PasswordResetState {
    private static final Logger logger = LoggerFactory.getLogger(InitiateResetState.class);
    private static final String EMAIL_PROVIDER = "EMAIL";
    private static final String PASSWORD_RESET_EMAIL_ERROR = "Password reset is only available for email-registered users.";

    @Override
    public Object handle(PasswordResetContext context) {
        String email = context.getEmail();
        Optional<User> userOpt = context.findUserByEmail();
        
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException(PASSWORD_RESET_EMAIL_ERROR);
        }
        
        // Generate OTP for password reset
        String otp = context.generateOTP();
        
        // Log the action
        logger.info("Password reset OTP generated for email {} at {}: {}", email, 
                java.time.LocalDateTime.now(), otp);
                
        return null;
    }
}
