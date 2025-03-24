package com.safetypin.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class GoogleAuthDTO {

    @NotBlank
    private String idToken;

    @NotBlank
    private String serverAuthCode;
}