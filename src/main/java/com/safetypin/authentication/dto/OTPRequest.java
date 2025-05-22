package com.safetypin.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OTPRequest {
    String email;
    String otp;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }

    public void setOtp(String otp) {
        this.otp = (otp != null) ? otp.trim() : null;
    }
}
