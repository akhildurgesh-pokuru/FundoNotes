
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.entity.AccountToken;
import com.fundoonotes.entity.AccountTokenType;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.AccountTokenRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.AccountService;
import com.fundoonotes.service.AccountTokenService;
import com.fundoonotes.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountTokenRepository tokenRepository;
    private final AccountTokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final long verificationMinutes;
    private final long resetMinutes;

    public AccountServiceImpl(
            UserRepository userRepository,
            AccountTokenRepository tokenRepository,
            AccountTokenService tokenService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            @Value("${app.verification.expiration-minutes}")
            long verificationMinutes,
            @Value("${app.password-reset.expiration-minutes}")
            long resetMinutes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.verificationMinutes = verificationMinutes;
        this.resetMinutes = resetMinutes;
    }

    @Override
    @Transactional
    public void sendRegistrationVerification(User user) {

        String token = tokenService.createToken(
                user,
                AccountTokenType.EMAIL_VERIFICATION,
                verificationMinutes
        );

        emailService.sendVerificationEmail(
                user.getEmail(),
                token
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {

        AccountToken token = tokenService.validateToken(
                rawToken,
                AccountTokenType.EMAIL_VERIFICATION
        );

        User user = token.getUser();

        user.setEmailVerified(true);

        // Prevent using the verification token again.
        token.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void resendVerification(String email) {

        userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendRegistrationVerification);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {

        userRepository.findByEmail(normalizeEmail(email))
                .ifPresent(user -> {

                    String token = tokenService.createToken(
                            user,
                            AccountTokenType.PASSWORD_RESET,
                            resetMinutes
                    );

                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            token
                    );
                });
    }

    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordRequestDto request
    ) {

        AccountToken token = tokenService.validateToken(
                request.token(),
                AccountTokenType.PASSWORD_RESET
        );

        User user = token.getUser();

        // BCrypt-encode the new password before saving.
        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        // A consumed token cannot reset the password again.
        token.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
