package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequestDto(

        @NotBlank(message = "Note title is required")
        @Size(
                max = 200,
                message = "Title must be 200 characters or less"
        )
        String title,

        @Size(
                max = 10000,
                message = "Description must be 10000 characters or less"
        )
        String description

) {
}
