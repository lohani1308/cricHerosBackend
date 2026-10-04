package com.cricheroes.authenticationservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    private final SecretKey secretKey;

    private static final long ACCESS_TOKEN_EXPIRY =
            15 * 60 * 1000; // 15 minutes

    public JWTService(
            @Value("${jwt.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(Long userId) {

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + ACCESS_TOKEN_EXPIRY
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}