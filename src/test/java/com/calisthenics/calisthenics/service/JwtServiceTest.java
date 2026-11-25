package com.calisthenics.calisthenics.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-not-real-0123456789-abcdefghijkl";
    private static final String OTHER_SECRET = "other-secret-not-real-0123456789-abcdefghij";
    private static final long ONE_HOUR = 3_600_000L;

    @Test
    void generateToken_andExtractClaims() {
        JwtService service = new JwtService(SECRET, ONE_HOUR);

        String token = service.generateToken(42L, "ADMIN");

        assertTrue(service.isTokenValid(token));
        assertEquals("42", service.extractUserId(token));
        assertEquals("ADMIN", service.extractRole(token));
    }

    @Test
    void constructor_shortSecret_throws() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corta", ONE_HOUR));
    }

    @Test
    void constructor_nullSecret_throws() {
        assertThrows(IllegalStateException.class, () -> new JwtService(null, ONE_HOUR));
    }

    @Test
    void constructor_blankSecret_throws() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", ONE_HOUR));
        assertThrows(IllegalStateException.class, () -> new JwtService("   ", ONE_HOUR));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService service = new JwtService(SECRET, -1000L);

        String token = service.generateToken(1L, "USER");

        assertFalse(service.isTokenValid(token));
    }

    @Test
    void isTokenValid_tokenSignedWithOtherKey_returnsFalse() {
        JwtService other = new JwtService(OTHER_SECRET, ONE_HOUR);
        JwtService service = new JwtService(SECRET, ONE_HOUR);

        String token = other.generateToken(1L, "USER");

        assertFalse(service.isTokenValid(token));
    }

    @Test
    void isTokenValid_garbageToken_returnsFalse() {
        JwtService service = new JwtService(SECRET, ONE_HOUR);

        assertFalse(service.isTokenValid("esto-no-es-un-jwt"));
        assertFalse(service.isTokenValid(""));
        assertFalse(service.isTokenValid(null));
    }
}
