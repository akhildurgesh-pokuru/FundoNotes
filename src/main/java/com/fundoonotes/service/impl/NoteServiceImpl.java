package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteService;

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

    public NoteServiceImpl(
            NoteRepository noteRepository,
            UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // CREATE NOTE
    @Override
    @Transactional
    public NoteResponseDto createNote(
            Long userId,
            NoteRequestDto request
    ) {

        // Get the authenticated user from the database.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        Note note = new Note();

        note.setTitle(request.title().trim());
        note.setDescription(request.description());

        // Assign ownership on the server, not from client input.
        note.setUser(user);

        // Flush so JPA assigns the ID and timestamps before mapping.
        Note savedNote = noteRepository.saveAndFlush(note);

        log.info(
                "Created note {} for user {}",
                savedNote.getId(),
                userId
        );

        return mapToResponse(savedNote);
    }

    // READ ALL NOTES FOR CURRENT USER
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getAllNotes(Long userId) {

        List<Note> notes =
                noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);

        return notes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ ONE NOTE
    @Override
    @Transactional(readOnly = true)
    public NoteResponseDto getNoteById(
            Long userId,
            Long noteId
    ) {

        Note note = findOwnedNote(userId, noteId);

        return mapToResponse(note);
    }

    // UPDATE NOTE
    @Override
    @Transactional
    public NoteResponseDto updateNote(
            Long userId,
            Long noteId,
            NoteRequestDto request
    ) {

        // Refuse to load notes that belong to another user.
        Note note = findOwnedNote(userId, noteId);

        note.setTitle(request.title().trim());
        note.setDescription(request.description());

        // JPA persists changes; saveAndFlush ensures timestamps update.
        Note updatedNote = noteRepository.saveAndFlush(note);

        log.info(
                "Updated note {} for user {}",
                noteId,
                userId
        );

        return mapToResponse(updatedNote);
    }

    // DELETE NOTE
    @Override
    @Transactional
    public void deleteNote(Long userId, Long noteId) {

        Note note = findOwnedNote(userId, noteId);

        noteRepository.delete(note);

        log.info(
                "Deleted note {} for user {}",
                noteId,
                userId
        );
    }

    // Shared helper: find a note only if the user owns it.
    private Note findOwnedNote(Long userId, Long noteId) {

        return noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found"
                        )
                );
    }

    // Convert the database entity into a safe API response.
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
}
