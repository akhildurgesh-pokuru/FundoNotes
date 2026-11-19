package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

import org.springframework.batch.item.ItemProcessor;

public class NoteImportProcessor
        implements ItemProcessor<NoteImportRow, Note> {

    private final User owner;

    public NoteImportProcessor(User owner) {
        this.owner = owner;
    }

    @Override
    public Note process(NoteImportRow row) {

        // Spring Batch filters an item when the
        // processor returns null.
        if (row.title() == null
                || row.title().isBlank()) {
            return null;
        }

        String title = row.title().trim();

        // Keep titles reasonably short.
        if (title.length() > 150) {
            return null;
        }

        String description = row.description() == null
                ? ""
                : row.description().trim();

        // Avoid importing overly long descriptions.
        if (description.length() > 1000) {
            return null;
        }

        Note note = new Note();

        note.setTitle(title);
        note.setDescription(description);

        // Security: Every imported note belongs
        // to the authenticated user.
        note.setUser(owner);

        // Match the defaults used by normal note creation.
        note.setPinned(false);
        note.setArchived(false);
        note.setTrashed(false);

        return note;
    }
}
