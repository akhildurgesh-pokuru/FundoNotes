
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UpdateUserProfileRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
