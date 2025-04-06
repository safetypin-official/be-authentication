package com.safetypin.authentication.service.passwordreset;

import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * State for verifying OTP during password reset
 */
public class VerifyOTPState implements PasswordResetState {
    private static final Logger logger = LoggerFactory.getLogger(VerifyOTPState.class);
    private static final String EMAIL_PROVIDER = "EMAIL";
    private static final String PASSWORD_RESET_EMAIL_ERROR = "Password reset is only available for email-registered users.";

    @Override
    public Object handle(PasswordResetContext context) {
        String email = context.getEmail();


        Optional<User> userOpt = context.findUserByEmail();
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException(PASSWORD_RESET_EMAIL_ERROR);
        }

        boolean isValid = context.verifyOTP();
        if (isValid) {
            // Generate a reset token valid for 3 minutes
            String resetToken = context.generateResetToken();
            logger.info("Password reset OTP verified for {}. Reset token generated at {}",
                    email, java.time.LocalDateTime.now());
            return resetToken;
        } else {
            logger.warn("Invalid password reset OTP attempt for {} at {}",
                    email, java.time.LocalDateTime.now());
            return null;
        }
    }
}
