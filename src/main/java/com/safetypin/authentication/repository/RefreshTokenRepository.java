package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);

    // maybe don't use? or call this periodically
    void deleteAllByExpiryTimeBefore(Instant expiryTime);
}
