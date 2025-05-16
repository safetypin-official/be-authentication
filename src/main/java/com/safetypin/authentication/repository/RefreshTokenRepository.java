package com.safetypin.authentication.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.safetypin.authentication.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    RefreshToken findByToken(String token);

    // maybe don't use? or call this periodically
    void deleteAllByExpiryTimeBefore(Instant expiryTime);

    // For monitoring purposes (active users)
    long countByExpiryTimeAfter(Instant expiryTime);

    void deleteByUserId(UUID userId);
}
