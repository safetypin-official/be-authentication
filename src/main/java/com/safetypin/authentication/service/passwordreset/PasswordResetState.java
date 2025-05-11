package com.safetypin.authentication.service.passwordreset;

/**
 * Interface representing a state in the password reset process
 */
public interface PasswordResetState {
    /**
     * Process the current state
     *
     * @param context The context containing data for the password reset process
     * @return Object result of the processing (could be null, a token string, etc.)
     */
    Object handle(PasswordResetContext context);
}
