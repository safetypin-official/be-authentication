package com.safetypin.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetTokenResponse {
    private String resetToken;

    public ResetTokenResponse(String resetToken) {
        this.resetToken = resetToken;
    }

}
