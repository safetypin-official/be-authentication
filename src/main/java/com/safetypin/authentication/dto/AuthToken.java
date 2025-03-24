package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthToken {
    private String accessToken;
    private String refreshToken;
}
