package com.example.tradetrackeruser.dto;

public record UserInfoDto(
        Long id,
        String email,
        String username,
        String password,
        boolean enabled
) {
}
