package com.safetypin.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OTPRequest {
    String email;
    String otp;
}
