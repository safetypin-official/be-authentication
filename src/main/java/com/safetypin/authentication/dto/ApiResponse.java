package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private boolean success;
    private T data;
    private String message;
    
    // Custom builder to support chaining
    public static class ApiResponseBuilder<T> {
        private String status;
        private boolean success;
        private T data;
        private String message;
        
        public ApiResponseBuilder<T> status(String status) {
            this.status = status;
            return this;
        }
        
        public ApiResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }
        
        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }
        
        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }
        
        public ApiResponse<T> build() {
            return new ApiResponse<>(status, success, data, message);
        }
    }
    
    // Static factory methods for easier creation
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }
    
    // Convenience methods for creating success/error responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", true, data, message);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", false, null, message);
    }
}