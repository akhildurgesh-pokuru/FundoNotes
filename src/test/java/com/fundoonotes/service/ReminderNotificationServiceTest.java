package com.fundoonotes.service;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderNotificationServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private JavaMailSender mailSender;

    private ReminderNotificationService service;

    private Note note;

    @BeforeEach
    void setUp() {

        service = new ReminderNotificationService(
                noteRepository,
                mailSender,
                "no-reply@fundoonotes.local"
        );

        User user = new User();
        user.setId(3L);
        user.setEmail("arun@example.com");

        note = new Note();
        note.setId(2L);
        note.setUser(user);
        note.setTitle("Spring Boot Practice");
        note.setDescription("Learning RabbitMQ");
        note.setReminderAt(
                Instant.now().minusSeconds(60)
        );
        note.setReminderSentAt(null);
    }

    @Test
    void dueReminder_ShouldSendEmail() {

        when(noteRepository.findReminderForProcessing(2L))
                .thenReturn(Optional.of(note));

        service.sendDueReminder(2L);

        verify(mailSender)
                .send(any(SimpleMailMessage.class));

        assertNotNull(note.getReminderSentAt());

        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void alreadySentReminder_ShouldNotSendAgain() {

        note.setReminderSentAt(Instant.now());

        when(noteRepository.findReminderForProcessing(2L))
                .thenReturn(Optional.of(note));

        service.sendDueReminder(2L);

        verifyNoInteractions(mailSender);
        verify(noteRepository, never())
                .saveAndFlush(any(Note.class));
    }

    @Test
    void cancelledReminder_ShouldNotSend() {

        note.setReminderAt(null);

        when(noteRepository.findReminderForProcessing(2L))
                .thenReturn(Optional.of(note));

        service.sendDueReminder(2L);

        verifyNoInteractions(mailSender);
    }

    @Test
    void smtpFailure_ShouldNotMarkSent() {

        when(noteRepository.findReminderForProcessing(2L))
                .thenReturn(Optional.of(note));

        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(
                MailSendException.class,
                () -> service.sendDueReminder(2L)
        );

        assertNull(note.getReminderSentAt());
    }
}
