
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

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setId(1L);
        user.setEmail("ravi@example.com");
        user.setPassword("stored-password-hash");
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {

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

        when(tokenUtil.generateToken(1L))
                .thenReturn("sample.jwt.token");

        when(tokenUtil.getExpirationSeconds())
                .thenReturn(86400L);

        LoginResponseDto response = userService.login(request);

        assertEquals("sample.jwt.token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(86400L, response.expiresIn());

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

        when(passwordEncoder.matches(
                "WrongPassword",
                "stored-password-hash"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

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
    }
}
