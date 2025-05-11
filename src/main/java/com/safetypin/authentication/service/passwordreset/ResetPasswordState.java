package com.safetypin.authentication.service.passwordreset;

import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * State for performing the actual password reset
 */
public class ResetPasswordState implements PasswordResetState {
    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordState.class);
    private static final String EMAIL_PROVIDER = "EMAIL";
    private static final String PASSWORD_RESET_EMAIL_ERROR = "Password reset is only available for email-registered users.";

    @Override
    public Object handle(PasswordResetContext context) {
        String email = context.getEmail();
        String newPassword = context.getNewPassword();
        String resetToken = context.getResetToken();

        Optional<User> userOpt = context.findUserByEmail();
        if (userOpt.isEmpty() || !EMAIL_PROVIDER.equals(userOpt.get().getProvider())) {
            throw new IllegalArgumentException(PASSWORD_RESET_EMAIL_ERROR);
        }

        if (resetToken == null || !context.verifyResetToken()) {
            throw new InvalidCredentialsException("Invalid or expired reset token. Please request a new OTP.");
        }

        User user = userOpt.get();
        user.setPassword(context.encodePassword(newPassword));
        context.saveUser(user);

        logger.info("Password reset successfully for {} at {}", email, java.time.LocalDateTime.now());
        return null;
    }
}
