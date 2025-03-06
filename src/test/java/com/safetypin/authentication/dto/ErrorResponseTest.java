package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void testErrorResponseConstructor() {
        ErrorResponse errorResponse = new ErrorResponse(404, "Resource not found");

        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getMessage()).isEqualTo("Resource not found");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
