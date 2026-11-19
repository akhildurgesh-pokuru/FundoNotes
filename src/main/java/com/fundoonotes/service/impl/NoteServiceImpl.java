package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidNoteStateException;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.fundoonotes.entity.Label;
import com.fundoonotes.repository.LabelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger log =
            LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    public NoteServiceImpl(
            NoteRepository noteRepository,
            UserRepository userRepository,
            LabelRepository labelRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
    }

    // ---------------- UC5: CREATE ----------------

    @Override
    @Transactional
    public NoteResponseDto createNote(
            Long userId,
            NoteRequestDto request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        Note note = new Note();

        note.setTitle(request.title().trim());
        note.setDescription(request.description());

        // The authenticated user becomes the note owner.
        note.setUser(user);

        Note savedNote = noteRepository.saveAndFlush(note);

        log.info(
                "Created note {} for user {}",
                savedNote.getId(),
                userId
        );

        return mapToResponse(savedNote);
    }

    // ---------------- UC5 + UC6: READ ----------------

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getAllNotes(Long userId) {

        // Main notes list excludes archived and trashed notes.
        return noteRepository
                .findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponseDto getNoteById(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        // Trashed notes are accessed through the Trash APIs.
        ensureNotTrashed(note);

        return mapToResponse(note);
    }

    // ---------------- UC5: UPDATE ----------------

    @Override
    @Transactional
    public NoteResponseDto updateNote(
            Long userId,
            Long noteId,
            NoteRequestDto request
    ) {

        Note note = findOwnedNote(userId, noteId);

        // Editing notes in Trash is not allowed.
        ensureNotTrashed(note);

        note.setTitle(request.title().trim());
        note.setDescription(request.description());

        Note updatedNote = noteRepository.saveAndFlush(note);

        log.info(
                "Updated note {} for user {}",
                noteId,
                userId
        );

        return mapToResponse(updatedNote);
    }

    // ---------------- UC6: SOFT DELETE ----------------

    @Override
    @Transactional
    public void deleteNote(Long userId, Long noteId) {

        // The old DELETE endpoint now moves notes to Trash.
        moveToTrash(userId, noteId);
    }

    // ---------------- UC6: PIN ----------------

    @Override
    @Transactional
    public NoteResponseDto pinNote(Long userId, Long noteId) {

        Note note = findOwnedNote(userId, noteId);

        ensureActive(note);

        note.setPinned(true);

        return saveAndMap(note);
    }

    @Override
    @Transactional
    public NoteResponseDto unpinNote(Long userId, Long noteId) {

        Note note = findOwnedNote(userId, noteId);

        ensureActive(note);

        note.setPinned(false);

        return saveAndMap(note);
    }

    // ---------------- UC6: ARCHIVE ----------------

    @Override
    @Transactional
    public NoteResponseDto archiveNote(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        ensureNotTrashed(note);

        note.setArchived(true);

        // Archived notes should not remain pinned.
        note.setPinned(false);

        return saveAndMap(note);
    }

    @Override
    @Transactional
    public NoteResponseDto unarchiveNote(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        ensureNotTrashed(note);

        note.setArchived(false);

        return saveAndMap(note);
    }

    // ---------------- UC6: TRASH ----------------

    @Override
    @Transactional
    public NoteResponseDto moveToTrash(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        // A note already in Trash stays in Trash.
        note.setTrashed(true);

        // Clear other states to avoid conflicting categories.
        note.setPinned(false);
        note.setArchived(false);

        NoteResponseDto response = saveAndMap(note);

        log.info("Moved note {} to Trash", noteId);

        return response;
    }

    @Override
    @Transactional
    public NoteResponseDto restoreNote(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        if (!note.isTrashed()) {
            throw new InvalidNoteStateException(
                    "Only trashed notes can be restored"
            );
        }

        // Restore the note to the active notes list.
        note.setTrashed(false);
        note.setArchived(false);
        note.setPinned(false);

        NoteResponseDto response = saveAndMap(note);

        log.info("Restored note {} from Trash", noteId);

        return response;
    }

    // ---------------- UC6: LIST ARCHIVED ----------------

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getArchivedNotes(Long userId) {

        return noteRepository
                .findByUserIdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ---------------- UC6: LIST TRASH ----------------

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getTrashedNotes(Long userId) {

        return noteRepository
                .findByUserIdAndTrashedTrueOrderByUpdatedAtDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ---------------- UC6: PERMANENT DELETE ----------------

    @Override
    @Transactional
    public void permanentlyDeleteNote(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        // Permanent deletion is permitted only from Trash.
        if (!note.isTrashed()) {
            throw new InvalidNoteStateException(
                    "Move the note to Trash before permanent deletion"
            );
        }

        noteRepository.delete(note);

        log.info("Permanently deleted note {}", noteId);
    }

    // ---------------- SHARED HELPER METHODS ----------------

    private Note findOwnedNote(Long userId, Long noteId) {

        // Prevent users from accessing other users' notes.
        return noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() ->
                        new NoteNotFoundException("Note not found")
                );
    }

    private void ensureNotTrashed(Note note) {

        if (note.isTrashed()) {
            throw new InvalidNoteStateException(
                    "This operation is not allowed for trashed notes"
            );
        }
    }

    private void ensureActive(Note note) {

        ensureNotTrashed(note);

        if (note.isArchived()) {
            throw new InvalidNoteStateException(
                    "Unarchive the note before pinning or unpinning"
            );
        }
    }

    private NoteResponseDto saveAndMap(Note note) {

        Note savedNote = noteRepository.saveAndFlush(note);

        return mapToResponse(savedNote);
    }

    private NoteResponseDto mapToResponse(Note note) {

        return new NoteResponseDto(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.isPinned(),
                note.isArchived(),
                note.isTrashed(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> searchNotes(
            Long userId,
            String keyword
    ) {

        // Reject an empty search instead of returning every note.
        if (keyword == null || keyword.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search keyword cannot be empty"
            );
        }

        // Remove unnecessary spaces around the search term.
        String normalizedKeyword = keyword.trim();

        // The query searches only the authenticated user's active notes.
        List<Note> matchingNotes =
                noteRepository.searchActiveNotes(
                        userId,
                        normalizedKeyword
                );

        log.info(
                "Found {} matching notes for user {}",
                matchingNotes.size(),
                userId
        );

        // Reuse the response mapping from UC5 and UC6.
        return matchingNotes.stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    @Transactional
    public void assignLabel(
            Long userId,
            Long noteId,
            Long labelId
    ) {
        Note note = noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Note not found"
                        )
                );

        // Prevent modifying a note that is in Trash.
        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restore the note before adding labels"
            );
        }

        // Both the label and note must belong to this user.
        Label label = labelRepository
                .findByIdAndUserId(labelId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Label not found"
                        )
                );

        // Set prevents duplicate associations.
        note.getLabels().add(label);

        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void removeLabel(
            Long userId,
            Long noteId,
            Long labelId
    ) {
        Note note = noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Note not found"
                        )
                );

        Label label = labelRepository
                .findByIdAndUserId(labelId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Label not found"
                        )
                );

        // Removing an absent association is harmless.
        note.getLabels().remove(label);

        noteRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getNotesByLabel(
            Long userId,
            Long labelId
    ) {
        // Check label ownership even when it contains no notes.
        labelRepository
                .findByIdAndUserId(labelId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Label not found"
                        )
                );

        List<Note> notes =
                noteRepository.findActiveNotesByLabel(
                        userId, labelId
                );

        // Reuse your existing UC5 note response mapper.
        return notes.stream()
                .map(this::mapToResponse)
                .toList();
    }


}
