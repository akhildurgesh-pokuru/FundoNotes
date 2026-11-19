
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UpdateUserProfileRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.service.UserService;
import com.fundoonotes.dto.request.EmailRequestDto;
import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.service.AccountService;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    public UserController(
            UserService userService,
            AccountService accountService
    ) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegisterRequestDto request
    ) {

        UserResponseDto response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {

        LoginResponseDto response = userService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth-check")
    public ResponseEntity<Map<String, Object>> checkAuthentication(
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(
                Map.of(
                        "message", "Token is valid",
                        "userId", userId
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile(
            @AuthenticationPrincipal Long userId
    ) {

        // Spring Security supplies the authenticated user's ID.
        UserResponseDto response = userService.getProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateUserProfileRequestDto request
    ) {

        // Only the user identified by the JWT can be updated.
        UserResponseDto response =
                userService.updateProfile(userId, request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String token
    ) {

        accountService.verifyEmail(token);

        return ResponseEntity.ok(
                Map.of("message", "Email verified successfully")
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody EmailRequestDto request
    ) {

        accountService.resendVerification(request.email());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If verification is required, an email will be sent"
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody EmailRequestDto request
    ) {

        accountService.forgotPassword(request.email());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If the account exists, a reset email will be sent"
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request
    ) {

        accountService.resetPassword(request);

        return ResponseEntity.ok(
                Map.of("message", "Password reset successfully")
        );
    }

}
