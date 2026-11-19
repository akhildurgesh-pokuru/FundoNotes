
package com.fundoonotes.service;

import com.fundoonotes.dto.request.UpdateUserProfileRequestDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.UserNotFoundException;
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
class UserProfileServiceTest {

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
        user.setFirstName("Ravi");
        user.setLastName("Kumar");
        user.setEmail("ravi@example.com");
        user.setPassword("hashed-password");
    }

    @Test
    void getProfile_ShouldReturnUserDetails() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDto response = userService.getProfile(1L);

        assertEquals(1L, response.id());
        assertEquals("Ravi", response.firstName());
        assertEquals("Kumar", response.lastName());
        assertEquals("ravi@example.com", response.email());
    }

    @Test
    void getProfile_ShouldFailWhenUserNotFound() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getProfile(99L)
        );
    }

    @Test
    void updateProfile_ShouldSaveUpdatedUser() {

        UpdateUserProfileRequestDto request =
                new UpdateUserProfileRequestDto(
                        "Rahul",
                        "Sharma"
                );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserResponseDto response =
                userService.updateProfile(1L, request);

        assertEquals("Rahul", response.firstName());
        assertEquals("Sharma", response.lastName());

        // Email and password must not be changed.
        assertEquals("ravi@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPassword());

        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void updateProfile_ShouldFailWhenUserNotFound() {

        UpdateUserProfileRequestDto request =
                new UpdateUserProfileRequestDto(
                        "Rahul",
                        "Sharma"
                );

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateProfile(99L, request)
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));
    }
}
