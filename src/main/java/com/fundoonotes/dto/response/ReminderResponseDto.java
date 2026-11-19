package com.fundoonotes.dto.response;

import java.time.Instant;

public record ReminderResponseDto(
        Long noteId,
        String title,
        String description,
        Instant reminderAt
) {
}
