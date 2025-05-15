package com.safetypin.authentication.metrics;

import com.safetypin.authentication.repository.RefreshTokenRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenMetrics {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MeterRegistry meterRegistry;


    public RefreshTokenMetrics(RefreshTokenRepository refreshTokenRepository, MeterRegistry meterRegistry) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        Gauge.builder("refresh_tokens_not_expired_total", this, RefreshTokenMetrics::getTotalTokens)
                .description("Total number of non-expired refresh tokens in Database, aka the number of users logged in")
                .register(meterRegistry);
    }

    // Micrometer calls this method to get the latest value
    public double getTotalTokens() {
        return refreshTokenRepository.countByExpiryTimeBefore(Instant.now());
    }
}
