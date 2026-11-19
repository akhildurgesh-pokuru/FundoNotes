package com.fundoonotes.dto.response;

public record LoginResponseDto(
        String token,
        String tokenType,
        long expiresIn,
        String message
) {
}
