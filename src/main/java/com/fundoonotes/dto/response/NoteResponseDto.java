package com.fundoonotes.dto.response;

import java.time.Instant;

public record NoteResponseDto(

        Long id,
        String title,
        String description,

        boolean pinned,
        boolean archived,
        boolean trashed,

        Instant createdAt,
        Instant updatedAt

) {
}
