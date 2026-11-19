package com.fundoonotes.service;

import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.NoteServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteSearchServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note note;

    @BeforeEach
    void setUp() {

        User user = new User();
        user.setId(3L);

        note = new Note();
        note.setId(2L);
        note.setTitle("Spring Boot Practice");
        note.setDescription("Learning JPA and JWT");
        note.setUser(user);
    }

    @Test
    void searchNotes_ShouldFindMatchingNotes() {

        when(noteRepository.searchActiveNotes(3L, "spring"))
                .thenReturn(List.of(note));

        List<NoteResponseDto> results =
                noteService.searchNotes(3L, "spring");

        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).id());
        assertEquals(
                "Spring Boot Practice",
                results.get(0).title()
        );

        verify(noteRepository)
                .searchActiveNotes(3L, "spring");
    }

    @Test
    void searchNotes_ShouldTrimKeyword() {

        when(noteRepository.searchActiveNotes(3L, "Java"))
                .thenReturn(List.of(note));

        noteService.searchNotes(3L, "  Java  ");

        // The service removes leading/trailing whitespace.
        verify(noteRepository)
                .searchActiveNotes(3L, "Java");
    }

    @Test
    void searchNotes_ShouldReturnEmptyListWhenNoMatch() {

        when(noteRepository.searchActiveNotes(3L, "unknown"))
                .thenReturn(List.of());

        List<NoteResponseDto> results =
                noteService.searchNotes(3L, "unknown");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchNotes_ShouldRejectBlankKeyword() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteService.searchNotes(3L, "   ")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(noteRepository);
    }

    @Test
    void searchNotes_ShouldRejectNullKeyword() {

        assertThrows(
                ResponseStatusException.class,
                () -> noteService.searchNotes(3L, null)
        );

        verifyNoInteractions(noteRepository);
    }

    @Test
    void searchNotes_ShouldUseAuthenticatedUserId() {

        when(noteRepository.searchActiveNotes(3L, "JWT"))
                .thenReturn(List.of(note));

        noteService.searchNotes(3L, "JWT");

        // Only the authenticated user ID is supplied to the repository.
        verify(noteRepository)
                .searchActiveNotes(3L, "JWT");

        verify(noteRepository, never())
                .searchActiveNotes(eq(4L), anyString());
    }
}
