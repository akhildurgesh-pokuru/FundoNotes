
package com.fundoonotes.service;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UpdateUserProfileRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;

public interface UserService {

    // UC1: Register a new user.
    UserResponseDto register(UserRegisterRequestDto request);

    // UC2: Login and generate a JWT.
    LoginResponseDto login(LoginRequestDto request);

    // UC3: Retrieve the logged-in user's profile.
    UserResponseDto getProfile(Long userId);

    // UC3: Update the logged-in user's profile.
    UserResponseDto updateProfile(
            Long userId,
            UpdateUserProfileRequestDto request
    );
}
