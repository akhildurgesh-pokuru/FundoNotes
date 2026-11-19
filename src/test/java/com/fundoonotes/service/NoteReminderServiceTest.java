package com.fundoonotes.service;

import com.fundoonotes.dto.response.ReminderResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.service.impl.NoteServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteReminderServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note note;

    @BeforeEach
    void setUp() {
        note = new Note();
        note.setId(2L);
        note.setTitle("Spring Boot Practice");
        note.setDescription("Learning JWT");
        note.setArchived(false);
        note.setTrashed(false);
    }

    @Test
    void setReminder_ShouldSaveFutureReminder() {

        Instant future = Instant.parse(
                "2030-11-21T04:30:00Z"
        );

        when(noteRepository.findByIdAndUserId(2L, 3L))
                .thenReturn(Optional.of(note));

        when(noteRepository.saveAndFlush(note))
                .thenReturn(note);

        ReminderResponseDto result = noteService.setReminder(
                3L, 2L, future
        );

        assertEquals(future, result.reminderAt());
        assertEquals(future, note.getReminderAt());

        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void setReminder_ShouldRejectPastTime() {

        Instant past = Instant.now().minusSeconds(60);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteService.setReminder(3L, 2L, past)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(noteRepository);
    }

    @Test
    void getUpcomingReminders_ShouldReturnNotes() {

        note.setReminderAt(
                Instant.parse("2030-11-21T04:30:00Z")
        );

        when(noteRepository.findUpcomingReminders(
                eq(3L), any(Instant.class)
        )).thenReturn(List.of(note));

        List<ReminderResponseDto> results =
                noteService.getUpcomingReminders(3L);

        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).noteId());
    }

    @Test
    void removeReminder_ShouldClearReminder() {

        note.setReminderAt(
                Instant.parse("2030-11-21T04:30:00Z")
        );

        when(noteRepository.findByIdAndUserId(2L, 3L))
                .thenReturn(Optional.of(note));

        noteService.removeReminder(3L, 2L);

        assertNull(note.getReminderAt());

        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void setReminder_ShouldRejectOtherUsersNote() {

        Instant future = Instant.parse(
                "2030-11-21T04:30:00Z"
        );

        when(noteRepository.findByIdAndUserId(2L, 4L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteService.setReminder(
                        4L, 2L, future
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }
}
