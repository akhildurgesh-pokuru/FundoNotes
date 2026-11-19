package com.fundoonotes.service;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderNotificationServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private JavaMailSender mailSender;

    private ReminderNotificationService notificationService;

    private Note note;

    @BeforeEach
    void setUp() {

        notificationService =
                new ReminderNotificationService(
                        noteRepository,
                        mailSender,
                        "no-reply@fundoonotes.local"
                );

        User user = new User();
        user.setId(3L);
        user.setEmail("arun@example.com");

        note = new Note();
        note.setId(2L);
        note.setTitle("Spring Boot Practice");
        note.setDescription("Learning Spring Security");
        note.setUser(user);
        note.setReminderAt(
                Instant.parse("2026-11-19T08:00:00Z")
        );
        note.setReminderSentAt(null);
    }

    @Test
    void dueReminder_ShouldSendEmailAndMarkAsSent() {

        when(noteRepository.findDueUnsentReminders(
                any(Instant.class)
        )).thenReturn(List.of(note));

        notificationService.processDueReminders();

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class
                );

        verify(mailSender).send(captor.capture());

        SimpleMailMessage email = captor.getValue();

        assertArrayEquals(
                new String[]{"arun@example.com"},
                email.getTo()
        );

        assertTrue(
                email.getSubject().contains(
                        "Spring Boot Practice"
                )
        );

        assertNotNull(note.getReminderSentAt());

        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void noDueReminders_ShouldNotSendEmail() {

        when(noteRepository.findDueUnsentReminders(
                any(Instant.class)
        )).thenReturn(List.of());

        notificationService.processDueReminders();

        verifyNoInteractions(mailSender);
        verify(noteRepository, never())
                .saveAndFlush(any(Note.class));
    }

    @Test
    void emailFailure_ShouldNotMarkReminderAsSent() {

        when(noteRepository.findDueUnsentReminders(
                any(Instant.class)
        )).thenReturn(List.of(note));

        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        notificationService.processDueReminders();

        assertNull(note.getReminderSentAt());

        verify(noteRepository, never())
                .saveAndFlush(any(Note.class));
    }

    @Test
    void missingRecipient_ShouldSkipReminder() {

        note.getUser().setEmail("");

        when(noteRepository.findDueUnsentReminders(
                any(Instant.class)
        )).thenReturn(List.of(note));

        notificationService.processDueReminders();

        verifyNoInteractions(mailSender);
        assertNull(note.getReminderSentAt());
    }
}
