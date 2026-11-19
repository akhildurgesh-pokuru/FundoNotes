package com.fundoonotes.service;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.NoteServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User user;
    private Note note;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail("ravi@example.com");

        note = new Note();
        note.setId(10L);
        note.setTitle("Spring Boot");
        note.setDescription("Learn CRUD");
        note.setUser(user);
    }

    @Test
    void createNote_ShouldSaveNoteForAuthenticatedUser() {

        NoteRequestDto request =
                new NoteRequestDto(
                        "New Note",
                        "Learning JPA"
                );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(noteRepository.saveAndFlush(any(Note.class)))
                .thenAnswer(invocation -> {
                    Note saved = invocation.getArgument(0);
                    saved.setId(11L);
                    return saved;
                });

        NoteResponseDto response =
                noteService.createNote(1L, request);

        assertEquals(11L, response.id());
        assertEquals("New Note", response.title());

        verify(noteRepository).saveAndFlush(
                argThat(saved ->
                        saved.getUser() == user
                )
        );
    }

    @Test
    void getAllNotes_ShouldReturnOnlyUserNotes() {

        when(noteRepository.findByUserIdOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(note));

        List<NoteResponseDto> result =
                noteService.getAllNotes(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());

        verify(noteRepository)
                .findByUserIdOrderByUpdatedAtDesc(1L);
    }

    @Test
    void getNoteById_ShouldReturnOwnedNote() {

        when(noteRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(note));

        NoteResponseDto response =
                noteService.getNoteById(1L, 10L);

        assertEquals(10L, response.id());
        assertEquals("Spring Boot", response.title());
    }

    @Test
    void updateNote_ShouldChangeTitleAndDescription() {

        NoteRequestDto request =
                new NoteRequestDto(
                        "Updated Title",
                        "Updated Description"
                );

        when(noteRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(note));

        when(noteRepository.saveAndFlush(note))
                .thenReturn(note);

        NoteResponseDto response =
                noteService.updateNote(1L, 10L, request);

        assertEquals("Updated Title", response.title());
        assertEquals(
                "Updated Description",
                response.description()
        );

        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void deleteNote_ShouldDeleteOwnedNote() {

        when(noteRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(note));

        noteService.deleteNote(1L, 10L);

        verify(noteRepository).delete(note);
    }

    @Test
    void getNoteById_ShouldRejectOtherUsersNote() {

        // User 2 must not be able to access user 1's note.
        when(noteRepository.findByIdAndUserId(10L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(2L, 10L)
        );

        verify(noteRepository, never())
                .saveAndFlush(any(Note.class));
    }
}
