package com.traveltrek.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log =
            LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    private SecretKey getSigningKey() {

        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret key must be at least 32 characters long");
        }

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {

        try {
            return parseClaims(token).getSubject();
        } catch (Exception ex) {

            log.error(
                    "Failed to extract email from JWT: {}",
                    ex.getMessage());

            return null;
        }
    }

    public boolean isTokenValid(String token, String email) {

        try {

            String tokenEmail = extractEmail(token);

            return tokenEmail != null
                    && tokenEmail.equals(email)
                    && !isTokenExpired(token);

        } catch (Exception ex) {

            log.error(
                    "JWT validation failed: {}",
                    ex.getMessage());

            return false;
        }
    }

    private boolean isTokenExpired(String token) {

        return parseClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims parseClaims(String token) {

        try {

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException ex) {

            log.error(
                    "Invalid JWT token: {}",
                    ex.getMessage());

            throw ex;
        }
    }
}