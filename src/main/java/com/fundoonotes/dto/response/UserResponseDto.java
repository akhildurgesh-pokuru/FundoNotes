
package com.fundoonotes.dto.response;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String message
) {
}
