package com.safetypin.authentication.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private Object data;
}