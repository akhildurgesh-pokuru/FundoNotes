package com.fundoonotes.service;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;

import com.fundoonotes.dto.response.ReminderResponseDto;
import java.time.Instant;

import java.util.List;

public interface NoteService {

    // UC5: Create, Read, Update, Delete

    NoteResponseDto createNote(
            Long userId,
            NoteRequestDto request
    );

    List<NoteResponseDto> getAllNotes(Long userId);

    NoteResponseDto getNoteById(
            Long userId,
            Long noteId
    );

    NoteResponseDto updateNote(
            Long userId,
            Long noteId,
            NoteRequestDto request
    );

    void deleteNote(Long userId, Long noteId);

    // UC6: Pin, Archive, Trash, Restore

    NoteResponseDto pinNote(Long userId, Long noteId);

    NoteResponseDto unpinNote(Long userId, Long noteId);

    NoteResponseDto archiveNote(Long userId, Long noteId);

    NoteResponseDto unarchiveNote(Long userId, Long noteId);

    NoteResponseDto moveToTrash(Long userId, Long noteId);

    NoteResponseDto restoreNote(Long userId, Long noteId);

    List<NoteResponseDto> getArchivedNotes(Long userId);

    List<NoteResponseDto> getTrashedNotes(Long userId);

    void permanentlyDeleteNote(Long userId, Long noteId);

    // UC7: Search active notes owned by the current user.

    List<NoteResponseDto> searchNotes(
            Long userId,
            String keyword
    );


    // UC8: Attach a label to an owned note.
    void assignLabel(
            Long userId,
            Long noteId,
            Long labelId
    );

    // UC8: Remove a label from an owned note.
    void removeLabel(
            Long userId,
            Long noteId,
            Long labelId
    );

    // UC8: List active notes by label.
    List<NoteResponseDto> getNotesByLabel(
            Long userId,
            Long labelId
    );


    // UC9: Set or update a reminder on a note.
    ReminderResponseDto setReminder(
            Long userId,
            Long noteId,
            Instant reminderAt
    );

    // UC9: Get upcoming reminders for the current user.
    List<ReminderResponseDto> getUpcomingReminders(
            Long userId
    );

    // UC9: Clear the reminder from a note.
    void removeReminder(
            Long userId,
            Long noteId
    );


}
