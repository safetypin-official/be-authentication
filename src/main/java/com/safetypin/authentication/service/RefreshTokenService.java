package com.safetypin.authentication.service;

import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {
    private static final long EXPIRATION_TIME = 24 * 60 * 60L; // 1 day in seconds
    private static final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

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

    public Optional<RefreshToken> getAndVerifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        // token doesn't exist
        if (refreshToken == null) {
            log.warn("Refresh token not found: {}, found:", token);
            return Optional.empty();
        }
        // Check expiry of refresh token
        if (refreshToken.getExpiryTime().isAfter(Instant.now())) {
            log.info("Refresh token found: {}, found:", token);
            log.info(refreshToken.toString());
            return Optional.of(refreshToken);
        }
        // Expired token, delete from database
        log.warn("Refresh token expired: {}, found:", token);
        log.info(refreshToken.toString());
        refreshTokenRepository.delete(refreshToken);
        return Optional.empty();
    }

    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);
        }
    }
}
