package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    private final long accessTokenValidityMs = 15 * 60 * 1000L;
    private final long refreshTokenValidityMs = 7L * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret
    ) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(String username, String role) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenValidityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username, String deviceId) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenValidityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("deviceId", deviceId)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {

        try {

            Claims claims = parseClaims(token);

            return "ACCESS".equals(
                    claims.get("tokenType", String.class)
            );

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    public boolean validateRefreshToken(String token) {

        try {

            Claims claims = parseClaims(token);

            return "REFRESH".equals(
                    claims.get("tokenType", String.class)
            );

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    public String getUsernameFromToken(String token) {

        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {

        return parseClaims(token)
                .get("role", String.class);
    }

    public long getAccessTokenValidityMs() {
        return accessTokenValidityMs;
    }

    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }

    private Claims parseClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}