package com.cricheroes.authenticationservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_REQUESTS = 3;

    private static final Duration OTP_EXPIRY =
            Duration.ofMinutes(5);

    private static final Duration REQUEST_WINDOW =
            Duration.ofSeconds(10);

    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =========================
    // REQUEST OTP
    // =========================

    public void generateAndStoreOtp(
            String countryCode,
            String phoneNumber) {

        String phone = countryCode + phoneNumber;

        /* *

        Phone: +919876543210

        otp:requests:+919876543210
                ↓
        How many OTPs did they REQUEST?

        otp:attempts:+919876543210
                ↓
        How many WRONG OTPs did they ENTER?

        otp:resend:+919876543210
                ↓
        Can they REQUEST another OTP right now?

        * */

        String otpKey = "otp:" + phone;
        String attemptsKey = "otp:attempts:" + phone;
        String requestsKey = "otp:requests:" + phone;
        String resendKey = "otp:resend:" + phone;

        // -------------------------
        // 1. Check resend cooldown
        // -------------------------

        Boolean resendExists =
                redisTemplate.hasKey(resendKey);

        if (Boolean.TRUE.equals(resendExists)) {
            throw new RuntimeException(
                    "Please wait before requesting another OTP"
            );
        }

        // -------------------------
        // 2. Rate limit OTP requests
        // -------------------------

        Long requestCount =
                redisTemplate.opsForValue()
                        .increment(requestsKey);

        if (requestCount == 1) {

            redisTemplate.expire(
                    requestsKey,
                    REQUEST_WINDOW
            );
        }

        if (requestCount > MAX_REQUESTS) {

            throw new RuntimeException(
                    "Too many OTP requests. Try again later."
            );
        }

        // -------------------------
        // 3. Generate OTP
        // -------------------------

        String otp = generateOtp();

        System.out.println("Generated OTP: " + otp);

        // -------------------------
        // 4. Store OTP
        // -------------------------

        redisTemplate.opsForValue().set(
                otpKey,
                otp,
                OTP_EXPIRY
        );

        // -------------------------
        // 5. Reset attempts
        // -------------------------

        redisTemplate.opsForValue().set(
                attemptsKey,
                "0",
                OTP_EXPIRY
        );

        // -------------------------
        // 6. Set resend cooldown
        // -------------------------

        redisTemplate.opsForValue().set(
                resendKey,
                "1",
                RESEND_COOLDOWN
        );

        // TODO:
        // Send OTP through SMS provider
    }


    // =========================
    // VERIFY OTP
    // =========================

    public boolean verifyOtp(
            String countryCode,
            String phoneNumber,
            String enteredOtp) {

        String phone = countryCode + phoneNumber;

        String otpKey = "otp:" + phone;
        String attemptsKey = "otp:attempts:" + phone;

        // -------------------------
        // 1. Get stored OTP
        // -------------------------

        String storedOtp =
                redisTemplate.opsForValue()
                        .get(otpKey);

        if (storedOtp == null) {

            throw new RuntimeException(
                    "OTP expired or does not exist"
            );
        }

        // -------------------------
        // 2. Get attempts
        // -------------------------

        String attemptsValue =
                redisTemplate.opsForValue()
                        .get(attemptsKey);

        int attempts = attemptsValue == null
                ? 0
                : Integer.parseInt(attemptsValue);

        // -------------------------
        // 3. Check maximum attempts
        // -------------------------

        if (attempts >= MAX_ATTEMPTS) {

            throw new RuntimeException(
                    "Maximum OTP attempts exceeded"
            );
        }

        // -------------------------
        // 4. Compare OTP
        // -------------------------

        if (!storedOtp.equals(enteredOtp)) {

            Long newAttempts =
                    redisTemplate.opsForValue()
                            .increment(attemptsKey);

            if (newAttempts >= MAX_ATTEMPTS) {

                throw new RuntimeException(
                        "Maximum OTP attempts exceeded"
                );
            }

            throw new RuntimeException(
                    "Invalid OTP. Attempts remaining: "
                            + (MAX_ATTEMPTS - newAttempts)
            );
        }

        // -------------------------
        // 5. OTP correct
        // -------------------------

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);

        return true;
    }


    private String generateOtp() {

        int otp =
                10000 + secureRandom.nextInt(90000);

        return String.valueOf(otp);
    }
}