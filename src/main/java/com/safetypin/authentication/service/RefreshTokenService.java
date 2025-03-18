package com.safetypin.authentication.service;

import com.safetypin.authentication.model.RefreshToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshTokenService {


    public RefreshToken createRefreshToken(UUID userId) {
        // TODO: implementation
        return null;
    }

    public boolean verifyToken(String token) {
        // TODO: implementation
        return null;
    }

    public void deleteRefreshToken(String token) {
        // TODO: implementation
        return null;
    }
}
