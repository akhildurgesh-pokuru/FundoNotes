
package com.fundoonotes.service;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReminderNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ReminderNotificationService.class
            );

    private final NoteRepository noteRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ReminderNotificationService(
            NoteRepository noteRepository,
            JavaMailSender mailSender,
            @Value("${app.reminder.mail.from:no-reply@fundoonotes.local}")
            String fromAddress
    ) {
        this.noteRepository = noteRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    // UC12: Called by the RabbitMQ consumer, not by a scheduler.
    @Transactional
    public void sendDueReminder(Long noteId) {

        Note note = noteRepository
                .findReminderForProcessing(noteId)
                .orElse(null);

        if (note == null) {
            log.info("Reminder note {} no longer exists", noteId);
            return;
        }

        Instant now = Instant.now();

        // An old message must not email a cancelled,
        // rescheduled, archived, or trashed note.
        if (note.getReminderAt() == null
                || note.getReminderAt().isAfter(now)
                || note.getReminderSentAt() != null
                || note.isArchived()
                || note.isTrashed()) {

            log.info(
                    "Skipping ineligible reminder for note {}",
                    noteId
            );
            return;
        }

        String recipient = note.getUser().getEmail();

        if (recipient == null || recipient.isBlank()) {
            log.warn(
                    "Cannot email note {}: missing recipient",
                    noteId
            );
            return;
        }

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(fromAddress);
        email.setTo(recipient);

        email.setSubject(
                "Fundoo Notes Reminder: " + note.getTitle()
        );

        String description = note.getDescription() == null
                ? ""
                : note.getDescription();

        email.setText(
                "Hello,\n\n"
                        + "Your Fundoo Notes reminder is due.\n\n"
                        + "Note: " + note.getTitle() + "\n"
                        + "Description: " + description + "\n"
                        + "Reminder time (UTC): "
                        + note.getReminderAt() + "\n\n"
                        + "Regards,\nFundoo Notes"
        );

        // If SMTP fails, the exception propagates to the
        // RabbitMQ listener. The transaction rolls back.
        mailSender.send(email);

        // Only mark sent after SMTP accepted the email.
        note.setReminderSentAt(Instant.now());

        noteRepository.saveAndFlush(note);

        log.info(
                "UC12: Reminder email sent for note {}",
                noteId
        );
    }
}
