package com.cricheroes.authenticationservice.dto;

public record OtpVerifyRequest(
        String countryCode,
        String phoneNumber,
        String otp
) {
}