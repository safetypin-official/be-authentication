package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final long EXPIRATION_TIME = 24 * 60 * 60L; // 1 day in seconds
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Optional<RefreshToken> refreshTokenWithUser = refreshTokenRepository.findByUserId(userId);
        if (refreshTokenWithUser.isPresent()) {
            throw new IllegalArgumentException("Refresh token already exists");
        }

        User user = userOptional.get();
        RefreshToken refreshToken = new RefreshToken();

        byte[] randomBytes = new byte[87]; // token length: 696 bits (116 chars), should be more than enough
        random.nextBytes(randomBytes);
        String encodedBytes = Base64.getEncoder().encodeToString(randomBytes);

        refreshToken.setToken(encodedBytes);
        refreshToken.setExpiryTime(Instant.now().plusSeconds(EXPIRATION_TIME));
        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken != null && refreshToken.getExpiryTime().isAfter(Instant.now());
    }

    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);
        }
    }

    // renew refresh token (verify, delete, create)
    public RefreshToken renewRefreshToken(String token) throws ApiException {
        if (!verifyRefreshToken(token)) {
            throw new ApiException("Invalid refresh token");
        }
        // delete existing token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        this.deleteRefreshToken(refreshToken.getToken());

        RefreshToken newRefreshToken = this.createRefreshToken(refreshToken.getUser().getId());
        return refreshTokenRepository.save(newRefreshToken);
    }
}
