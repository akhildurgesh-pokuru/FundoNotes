
package com.fundoonotes.service;

import com.fundoonotes.entity.AccountToken;
import com.fundoonotes.entity.AccountTokenType;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidAccountTokenException;
import com.fundoonotes.repository.AccountTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AccountTokenService {

    private final AccountTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountTokenService(
            AccountTokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String createToken(
            User user,
            AccountTokenType type,
            long expirationMinutes
    ) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        // URL-safe token, suitable for email links.
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        AccountToken token = new AccountToken();

        // The database stores only a hash, not the usable token.
        token.setTokenHash(hashToken(rawToken));
        token.setType(type);
        token.setUser(user);
        token.setExpiresAt(
                Instant.now().plus(
                        Duration.ofMinutes(expirationMinutes)
                )
        );

        tokenRepository.save(token);

        return rawToken;
    }

    @Transactional
    public AccountToken validateToken(
            String rawToken,
            AccountTokenType type
    ) {

        AccountToken token = tokenRepository
                .findByTokenHashAndType(hashToken(rawToken), type)
                .orElseThrow(() ->
                        new InvalidAccountTokenException(
                                "Invalid or expired token"
                        )
                );

        if (token.isUsed()
                || !token.getExpiresAt().isAfter(Instant.now())) {

            throw new InvalidAccountTokenException(
                    "Invalid or expired token"
            );
        }

        return token;
    }

    public String hashToken(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashed);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", exception
            );
        }
    }
}
