package com.example.tradetrackeruser.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
