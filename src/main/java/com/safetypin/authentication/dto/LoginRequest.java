package com.safetypin.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String email;
    private String password;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }
}
