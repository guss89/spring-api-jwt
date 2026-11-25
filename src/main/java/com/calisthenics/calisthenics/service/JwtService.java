package com.calisthenics.calisthenics.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La propiedad jwt.secret no está configurada (defina la variable de entorno JWT_SECRET)");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String role){
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractUserId(String token){
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token){
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
