package com.fawry.routing.config.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET = "your-very-long-random-secret-string"; // from properties ideally
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes()); // fixed key
    private static final long EXPIRATION_MS = 3600000; // 1 hour
    private Claims claims;

    public static String generateToken(String email, int id, String role) {
        return Jwts.builder()
                // the payload is email + userId + the single role the user holds
                .claim("email", email)
                .claim("id", id)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    // validate token - throws exceptions if invalid
    public void validateToken(String token) {
        claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token) // throws unchecked exceptions if invalid
                .getPayload();

    }

    public Integer getId(String token) {
        return this.claims.get("id", Integer.class);
    }

    public String getEmail(String token) {
        return this.claims.get("email", String.class);
    }

    public String getRole(String token) {
        return this.claims.get("role", String.class);
    }
}
