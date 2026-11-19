
package com.fundoonotes.service;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidCredentialsException;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.TokenUtil;
import com.fundoonotes.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenUtil tokenUtil;

    // Added in UC4 because UserServiceImpl depends on AccountService.
    @Mock
    private AccountService accountService;

    // Mockito injects all four mocked dependencies.
    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {

        // Create a test user before every test.
        user = new User();

        user.setId(1L);
        user.setEmail("ravi@example.com");
        user.setPassword("stored-password-hash");

        // UC4 requires email verification before login.
        user.setEmailVerified(true);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {

        LoginRequestDto request =
                new LoginRequestDto(
                        "ravi@example.com",
                        "Password@123"
                );

        // Simulate finding the registered user.
        when(userRepository.findByEmail("ravi@example.com"))
                .thenReturn(Optional.of(user));

        // Simulate a successful BCrypt password match.
        when(passwordEncoder.matches(
                "Password@123",
                "stored-password-hash"
        )).thenReturn(true);

        // Simulate JWT generation.
        when(tokenUtil.generateToken(1L))
                .thenReturn("sample.jwt.token");

        when(tokenUtil.getExpirationSeconds())
                .thenReturn(86400L);

        LoginResponseDto response = userService.login(request);

        assertEquals("sample.jwt.token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(86400L, response.expiresIn());
        assertEquals("Login successful", response.message());

        verify(tokenUtil).generateToken(1L);
    }

    @Test
    void login_ShouldFail_WhenPasswordIsIncorrect() {

        LoginRequestDto request =
                new LoginRequestDto(
                        "ravi@example.com",
                        "WrongPassword"
                );

        when(userRepository.findByEmail("ravi@example.com"))
                .thenReturn(Optional.of(user));

        // Simulate an incorrect password.
        when(passwordEncoder.matches(
                "WrongPassword",
                "stored-password-hash"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        // JWT must not be generated for incorrect credentials.
        verify(tokenUtil, never()).generateToken(anyLong());
    }

    @Test
    void login_ShouldFail_WhenEmailDoesNotExist() {

        LoginRequestDto request =
                new LoginRequestDto(
                        "unknown@example.com",
                        "Password@123"
                );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        verify(tokenUtil, never()).generateToken(anyLong());
    }

    @Test
    void login_ShouldFail_WhenEmailIsNotVerified() {

        // Simulate a user who has registered but not verified email.
        user.setEmailVerified(false);

        LoginRequestDto request =
                new LoginRequestDto(
                        "ravi@example.com",
                        "Password@123"
                );

        when(userRepository.findByEmail("ravi@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "stored-password-hash"
        )).thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        // Unverified users must not receive JWTs.
        verify(tokenUtil, never()).generateToken(anyLong());
    }
}
