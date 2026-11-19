package com.fundoonotes.batch;

// Represents one row read from the CSV file.
public record NoteImportRow(
        String title,
        String description
) {
}
