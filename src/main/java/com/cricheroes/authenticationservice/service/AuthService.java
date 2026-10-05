package com.cricheroes.authenticationservice.service;

import com.cricheroes.authenticationservice.dto.OtpRequest;
import com.cricheroes.authenticationservice.dto.OtpVerifyRequest;
import com.cricheroes.authenticationservice.dto.TokenResponse;
import com.cricheroes.authenticationservice.entity.AuthSession;
import com.cricheroes.authenticationservice.entity.User;
import com.cricheroes.authenticationservice.repositories.AuthSessionRepository;
import com.cricheroes.authenticationservice.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenHashService tokenHashService;
    private final AuthSessionRepository authSessionRepository;

    public AuthService(
            OtpService otpService,
            UserRepository userRepository,
            JWTService jwtService,
            RefreshTokenService refreshTokenService,
            TokenHashService tokenHashService,
            AuthSessionRepository authSessionRepository
    ) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenHashService = tokenHashService;
        this.authSessionRepository = authSessionRepository;
    }

    // =========================
    // REQUEST OTP
    // =========================

    public void requestOtp(OtpRequest request) {

        otpService.generateAndStoreOtp(
                request.countryCode(),
                request.phoneNumber()
        );
    }

    // =========================
    // VERIFY OTP
    // =========================

    public TokenResponse verifyOtp(OtpVerifyRequest request) {

        // 1. Verify OTP from Redis
        boolean verified = otpService.verifyOtp(
                request.countryCode(),
                request.phoneNumber(),
                request.otp()
        );

        if (!verified) {
            throw new RuntimeException("Invalid OTP");
        }

        // 2. Find user
        User user = userRepository
                .findByCountryCodeAndPhoneNumber(
                        request.countryCode(),
                        request.phoneNumber()
                )
                .orElseGet(() -> {

                    // First-time user
                    User newUser = new User();

                    newUser.setCountryCode(
                            request.countryCode()
                    );

                    newUser.setPhoneNumber(
                            request.phoneNumber()
                    );

                    newUser.setPhoneVerified(true);

                    newUser.setStatus("ACTIVE");

                    newUser.setCreatedAt(
                            LocalDateTime.now()
                    );

                    newUser.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return userRepository.save(newUser);
                });

        // 3. Update login information
        user.setPhoneVerified(true);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // 4. Create session
        return createSession(
                user,
                null,
                null
        );
    }

    // =========================
    // CREATE SESSION
    // =========================

    public TokenResponse createSession(
            User user,
            String deviceId,
            String ipAddress
    ) {

        // Access token
        String accessToken =
                jwtService.generateAccessToken(
                        user.getId()
                );

        // Refresh token
        String refreshToken =
                refreshTokenService.generateRefreshToken();

        // Hash refresh token before storing
        String refreshTokenHash =
                tokenHashService.hash(refreshToken);

        // Create database session
        AuthSession session = new AuthSession();

        session.setUser(user);
        session.setRefreshTokenHash(refreshTokenHash);
        session.setDeviceId(deviceId);
        session.setIpAddress(ipAddress);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(
                LocalDateTime.now().plusDays(30)
        );

        authSessionRepository.save(session);

        // Return plaintext refresh token ONLY to client
        return new TokenResponse(
                accessToken,
                refreshToken
        );
    }
}