package com.fundoonotes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public TokenUtil(
            @Value("${jwt.secret.key}") String secret,
            @Value("${jwt.expiration.ms}") long expirationMs
    ) {

        // Decode the Base64 key supplied through configuration.
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId) {

        Date issuedAt = new Date();

        Date expiresAt = new Date(
                issuedAt.getTime() + expirationMs
        );

        // Store the user ID as the token subject.
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {

        // Verify the signature and expiration before using claims.
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
