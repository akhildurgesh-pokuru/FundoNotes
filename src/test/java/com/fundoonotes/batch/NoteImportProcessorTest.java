package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteImportProcessorTest {

    private NoteImportProcessor processor;
    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(3L);

        processor = new NoteImportProcessor(user);
    }

    @Test
    void validRow_ShouldCreateOwnedNote() throws Exception {

        NoteImportRow row = new NoteImportRow(
                "Learn Spring Batch",
                "Practice importing notes"
        );

        Note note = processor.process(row);

        assertNotNull(note);
        assertEquals("Learn Spring Batch", note.getTitle());
        assertEquals(
                "Practice importing notes",
                note.getDescription()
        );
        assertSame(user, note.getUser());

        assertFalse(note.isArchived());
        assertFalse(note.isTrashed());
        assertFalse(note.isPinned());
    }

    @Test
    void blankTitle_ShouldBeFiltered() throws Exception {

        NoteImportRow row = new NoteImportRow(
                "   ",
                "No valid title"
        );

        Note note = processor.process(row);

        assertNull(note);
    }

    @Test
    void validRow_ShouldTrimWhitespace() throws Exception {

        NoteImportRow row = new NoteImportRow(
                "  Study Redis  ",
                "  Learn caching  "
        );

        Note note = processor.process(row);

        assertNotNull(note);
        assertEquals("Study Redis", note.getTitle());
        assertEquals("Learn caching", note.getDescription());
    }

    @Test
    void tooLongTitle_ShouldBeFiltered() throws Exception {

        NoteImportRow row = new NoteImportRow(
                "A".repeat(151),
                "Too long"
        );

        assertNull(processor.process(row));
    }

    @Test
    void missingDescription_ShouldUseEmptyString()
            throws Exception {

        NoteImportRow row = new NoteImportRow(
                "Practice Batch",
                null
        );

        Note note = processor.process(row);

        assertNotNull(note);
        assertEquals("", note.getDescription());
    }
}
