package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;

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
