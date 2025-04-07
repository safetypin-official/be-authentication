package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    RefreshToken findByToken(String token);

    // maybe don't use? or call this periodically
    void deleteAllByExpiryTimeBefore(Instant expiryTime);
}
