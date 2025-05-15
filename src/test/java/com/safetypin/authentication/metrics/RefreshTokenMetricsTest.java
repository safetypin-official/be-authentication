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
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void init_shouldRegisterGauge() {
        // Mock static Gauge.builder()
        try (MockedStatic<Gauge> mockedGauge = mockStatic(Gauge.class)) {
            @SuppressWarnings("unchecked")
            Gauge.Builder<RefreshTokenMetrics> mockBuilder = mock(Gauge.Builder.class);
            when(mockBuilder.description(anyString())).thenReturn(mockBuilder);
            when(mockBuilder.register(any(MeterRegistry.class))).thenReturn(null); // Return value doesn't matter

            mockedGauge.when(() -> Gauge.builder(anyString(), any(RefreshTokenMetrics.class), any(ToDoubleFunction.class)))
                    .thenReturn(mockBuilder);

            refreshTokenMetrics.init();

            mockedGauge.verify(() -> Gauge.builder(
                    eq("refresh_tokens_not_expired_total"),
                    eq(refreshTokenMetrics),
                    any()
            ));
            verify(mockBuilder).description("Total number of non-expired refresh tokens in Database, aka the number of users logged in");
            verify(mockBuilder).register(meterRegistry);
        }
    }

    @Test
    void getTotalTokens_shouldReturnCountFromRepository() {
        long expectedCount = 10L;
        when(refreshTokenRepository.countByExpiryTimeBefore(any(Instant.class))).thenReturn(expectedCount);

        double actualCount = refreshTokenMetrics.getTotalTokens();

        assertEquals(expectedCount, actualCount);
        verify(refreshTokenRepository).countByExpiryTimeBefore(instantCaptor.capture());

        // Verify that Instant.now() was called (approximately)
        long nowMillis = Instant.now().toEpochMilli();
        long capturedMillis = instantCaptor.getValue().toEpochMilli();
        // Allow for a small difference in time due to execution
        assertEquals(nowMillis, capturedMillis, 1000);
    }
}