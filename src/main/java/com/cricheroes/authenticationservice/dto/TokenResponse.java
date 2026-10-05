package com.cricheroes.authenticationservice.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}