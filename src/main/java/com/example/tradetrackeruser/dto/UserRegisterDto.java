package com.example.tradetrackeruser.dto;

import javax.validation.constraints.NotEmpty;

public record UserRegisterDto(
        @NotEmpty(message = "Enter an email")
        String email,

        @NotEmpty(message = "Enter a password")
        String password
) {
}
