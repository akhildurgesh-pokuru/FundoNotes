package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequestDto(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name is too long")
        String lastName

) {
}
