
package com.fundoonotes.service;

import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.entity.AccountToken;
import com.fundoonotes.entity.AccountTokenType;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.AccountTokenRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.AccountServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountTokenRepository tokenRepository;

    @Mock
    private AccountTokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountServiceImpl accountService;

    private User user;

    @BeforeEach
    void setUp() {

        accountService = new AccountServiceImpl(
                userRepository,
                tokenRepository,
                tokenService,
                emailService,
                passwordEncoder,
                1440L,
                15L
        );

        user = new User();
        user.setId(1L);
        user.setEmail("ravi@example.com");
        user.setEmailVerified(false);
    }

    @Test
    void verifyEmail_ShouldMarkUserAsVerified() {

        AccountToken token = new AccountToken();
        token.setUser(user);
        token.setType(AccountTokenType.EMAIL_VERIFICATION);

        when(tokenService.validateToken(
                "verification-token",
                AccountTokenType.EMAIL_VERIFICATION
        )).thenReturn(token);

        accountService.verifyEmail("verification-token");

        assertTrue(user.isEmailVerified());
        assertTrue(token.isUsed());

        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_ShouldEncodePasswordAndConsumeToken() {

        AccountToken token = new AccountToken();
        token.setUser(user);
        token.setType(AccountTokenType.PASSWORD_RESET);

        when(tokenService.validateToken(
                "reset-token",
                AccountTokenType.PASSWORD_RESET
        )).thenReturn(token);

        when(passwordEncoder.encode("NewPassword@123"))
                .thenReturn("hashed-new-password");

        accountService.resetPassword(
                new ResetPasswordRequestDto(
                        "reset-token",
                        "NewPassword@123"
                )
        );

        assertEquals("hashed-new-password", user.getPassword());
        assertTrue(token.isUsed());

        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }
}
