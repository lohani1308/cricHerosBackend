package com.cricheroes.authenticationservice.service;

import com.cricheroes.authenticationservice.dto.OtpRequest;
import com.cricheroes.authenticationservice.dto.OtpVerifyRequest;
import org.springframework.stereotype.Service;
import com.cricheroes.authenticationservice.service.OtpService;

@Service
public class AuthService {

    private final OtpService otpService;

    public AuthService(OtpService otpService) {
        this.otpService = otpService;
    }

    public void requestOtp(OtpRequest request) {

        otpService.generateAndStoreOtp(
                request.countryCode(),
                request.phoneNumber()
        );
    }

    public boolean verifyOtp(OtpVerifyRequest request) {

        return otpService.verifyOtp(
                request.countryCode(),
                request.phoneNumber(),
                request.otp()
        );
    }
}