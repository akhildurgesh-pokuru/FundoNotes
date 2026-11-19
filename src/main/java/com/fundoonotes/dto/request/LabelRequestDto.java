package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LabelRequestDto(

        @NotBlank(message = "Label name is required")
        @Size(
                max = 100,
                message = "Label name must be at most 100 characters"
        )
        String name

) {
}
