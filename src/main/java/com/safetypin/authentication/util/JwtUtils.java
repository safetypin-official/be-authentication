package com.safetypin.authentication.util;

import com.safetypin.authentication.constants.ApiConstants;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.service.JwtService;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    
    private final JwtService jwtService;
    
    public JwtUtils(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * Extracts and validates a UserResponse from the Authorization header
     * 
     * @param authHeader The Authorization header containing the JWT token
     * @return The UserResponse object extracted from the token
     * @throws InvalidCredentialsException If the token is invalid or expired
     */
    public UserResponse parseUserFromAuthHeader(String authHeader) throws InvalidCredentialsException {
        String token = authHeader.replace(ApiConstants.BEARER_PREFIX, "");
        try {
            return jwtService.getUserFromJwtToken(token);
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }
    
    /**
     * Tries to extract a UserResponse from the Authorization header, returns null on failure
     * 
     * @param authHeader The Authorization header containing the JWT token
     * @return The UserResponse object or null if extraction fails
     */
    public UserResponse parseUserFromAuthHeaderSafe(String authHeader) {
        try {
            return parseUserFromAuthHeader(authHeader);
        } catch (Exception e) {
            return null;
        }
    }
}