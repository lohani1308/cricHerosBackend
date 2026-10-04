package com.cricheroes.authenticationservice.dto;

public record OtpRequest(
        String countryCode,
        String phoneNumber
) {
}