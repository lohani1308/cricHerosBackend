package com.cricheroes.authenticationservice.controllers;

import com.cricheroes.authenticationservice.dto.OtpRequest;
import com.cricheroes.authenticationservice.dto.OtpVerifyRequest;
import com.cricheroes.authenticationservice.dto.TokenResponse;
import com.cricheroes.authenticationservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<String> requestOtp(
            @RequestBody OtpRequest request) {

        authService.requestOtp(request);

        return ResponseEntity.ok(
                "OTP generated successfully"
        );
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<TokenResponse> verifyOtp(
            @RequestBody OtpVerifyRequest request) {

        TokenResponse response =
                authService.verifyOtp(request);

        return ResponseEntity.ok(response);
    }
}