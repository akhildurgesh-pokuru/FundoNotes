package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReminderRequestDto(

        @NotNull(message = "Reminder time is required")
        @Future(message = "Reminder time must be in the future")
        Instant reminderAt

) {
}
