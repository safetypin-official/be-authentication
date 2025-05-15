package com.safetypin.authentication.metrics;

import com.safetypin.authentication.repository.RefreshTokenRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenMetricsTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private MeterRegistry meterRegistry;

    private RefreshTokenMetrics refreshTokenMetrics;

    @Captor
    private ArgumentCaptor<Instant> instantCaptor;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry(); // Use a real simple registry for easier testing of registration
        refreshTokenMetrics = new RefreshTokenMetrics(refreshTokenRepository, meterRegistry);
    }

    @Test
    void init_shouldRegisterGaugeAndReportCorrectValue() {
        // Arrange
        long expectedTokenCount = 5L;
        when(refreshTokenRepository.countByExpiryTimeAfter(any(Instant.class))).thenReturn(expectedTokenCount);

        // Act
        refreshTokenMetrics.init(); // This will register the gauge

        // Assert
        Gauge registeredGauge = meterRegistry.find("refresh_tokens_not_expired_total").gauge();
        assertNotNull(registeredGauge, "Gauge 'refresh_tokens_not_expired_total' should be registered.");
        assertEquals(expectedTokenCount, registeredGauge.value(), "Gauge should report the count from the repository.");

        // Verify that the repository method was called when the gauge value was fetched (implicitly by init and then by value())
        // The gauge's value function (getTotalTokens) will be called by Micrometer when the gauge is registered
        // and again when we call registeredGauge.value().
        // We expect at least one call to countByExpiryTimeAfter due to the gauge's nature.
        verify(refreshTokenRepository, atLeastOnce()).countByExpiryTimeAfter(any(Instant.class));
    }

    @Test
    void getTotalTokens_shouldReturnCountFromRepository() {
        long expectedCount = 10L;
        when(refreshTokenRepository.countByExpiryTimeAfter(any(Instant.class))).thenReturn(expectedCount);

        double actualCount = refreshTokenMetrics.getTotalTokens();

        assertEquals(expectedCount, actualCount);
        verify(refreshTokenRepository).countByExpiryTimeAfter(instantCaptor.capture());

        // Verify that Instant.now() was called (approximately)
        long nowMillis = Instant.now().toEpochMilli();
        long capturedMillis = instantCaptor.getValue().toEpochMilli();
        // Allow for a small difference in time due to execution
        assertEquals(nowMillis, capturedMillis, 1000);
    }
}