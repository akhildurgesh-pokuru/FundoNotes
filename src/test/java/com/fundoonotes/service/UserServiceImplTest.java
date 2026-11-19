
package com.fundoonotes.service;

import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.UserAlreadyExistsException;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void register_ShouldCreateUser_WhenEmailIsNew() {

        UserRegisterRequestDto request =
                new UserRegisterRequestDto(
                        "Ravi",
                        "Kumar",
                        "ravi@example.com",
                        "Password@123"
                );

        when(userRepository.existsByEmail("ravi@example.com"))
                .thenReturn(false);

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {

                    User user = invocation.getArgument(0);

                    // Simulate a generated database ID.
                    user.setId(1L);

                    return user;
                });

        UserResponseDto response = userService.register(request);

        assertEquals(1L, response.id());
        assertEquals("Ravi", response.firstName());
        assertEquals("ravi@example.com", response.email());

        // Verify the saved password is hashed.
        verify(userRepository).saveAndFlush(argThat(user ->
                passwordEncoder.matches(
                        "Password@123",
                        user.getPassword()
                )
        ));
    }

    @Test
    void register_ShouldRejectDuplicateEmail() {

        UserRegisterRequestDto request =
                new UserRegisterRequestDto(
                        "Ravi",
                        "Kumar",
                        "ravi@example.com",
                        "Password@123"
                );

        when(userRepository.existsByEmail("ravi@example.com"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        // No user should be saved after duplicate detection.
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }
}
