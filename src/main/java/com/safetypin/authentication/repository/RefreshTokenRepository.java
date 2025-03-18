package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);
    Optional<RefreshToken> findByUserId(UUID userId);

    // maybe don't use? or call this periodically
    void deleteAllByExpiryTimeBefore(Instant expiryTime);
}
