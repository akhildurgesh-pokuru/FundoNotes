
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UpdateUserProfileRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidCredentialsException;
import com.fundoonotes.exception.UserAlreadyExistsException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.TokenUtil;
import com.fundoonotes.service.UserService;
import com.fundoonotes.service.AccountService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    private final AccountService accountService;


    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenUtil tokenUtil,
            AccountService accountService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
        this.accountService = accountService;
    }



    @Override
    @Transactional
    public UserResponseDto register(UserRegisterRequestDto request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);

        // New users must verify their email.
        user.setEmailVerified(false);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.saveAndFlush(user);

        // Create and send the verification token.
        accountService.sendRegistrationVerification(savedUser);

        log.info("Registered user with ID {}", savedUser.getId());

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                "Registration successful. Verify your email before login."
        );
    }


    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        // Verify the entered password against its BCrypt hash.
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }


        // Only verified accounts are allowed to receive JWTs.
        if (!user.isEmailVerified()) {
            throw new InvalidCredentialsException(
                    "Please verify your email before logging in"
            );
        }


        String token = tokenUtil.generateToken(user.getId());

        log.info("User logged in with ID {}", user.getId());

        return new LoginResponseDto(
                token,
                "Bearer",
                tokenUtil.getExpirationSeconds(),
                "Login successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getProfile(Long userId) {

        // The user ID comes from the authenticated JWT.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User profile not found"
                        )
                );

        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                "User profile retrieved successfully"
        );
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(
            Long userId,
            UpdateUserProfileRequestDto request
    ) {

        // Retrieve only the authenticated user's account.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User profile not found"
                        )
                );

        // Update only fields permitted by this use case.
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());

        User savedUser = userRepository.saveAndFlush(user);

        log.info("Updated profile for user ID {}", savedUser.getId());

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                "User profile updated successfully"
        );
    }
}
