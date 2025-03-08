package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        ErrorResponse response = new ErrorResponse();

        // Assert
        assertNull(response.getMessage());
        assertEquals(0, response.getStatus());
        assertNull(response.getTimestamp());
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        int status = 404;
        String message = "Not Found";

        // Act
        ErrorResponse response = new ErrorResponse(status, message);
        LocalDateTime beforeTest = LocalDateTime.now().minusSeconds(1);
        
        // Assert
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
        // Check that timestamp is recent
        assertTrue(response.getTimestamp().isAfter(beforeTest));
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        ErrorResponse response = new ErrorResponse();
        int status = 500;
        String message = "Internal Server Error";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        response.setStatus(status);
        response.setMessage(message);
        response.setTimestamp(timestamp);

        // Assert
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ErrorResponse response1 = new ErrorResponse(404, "Not Found");
        ErrorResponse response2 = new ErrorResponse(404, "Not Found");
        response2.setTimestamp(response1.getTimestamp()); // Ensure same timestamp for equality check
        ErrorResponse response3 = new ErrorResponse(500, "Error");

        // Assert
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ErrorResponse response = new ErrorResponse(404, "Not Found");
        
        // Act
        String toStringResult = response.toString();
        
        // Assert
        assertTrue(toStringResult.contains("404"));
        assertTrue(toStringResult.contains("Not Found"));
        assertTrue(toStringResult.contains("timestamp"));
    }
}
