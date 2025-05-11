package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthToken {
    private UUID userId;
    private String accessToken;
    private String refreshToken;
}
