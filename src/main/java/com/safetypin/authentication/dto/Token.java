package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


// DEPRECATED? Use AuthToken instead
@Getter
@AllArgsConstructor
public class Token {
    private String tokenValue;
}
