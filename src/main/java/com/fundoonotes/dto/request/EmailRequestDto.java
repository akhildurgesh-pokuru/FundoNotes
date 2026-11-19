package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequestDto(

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        String email

) {
}
