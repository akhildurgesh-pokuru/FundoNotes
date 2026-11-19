package com.fundoonotes.service;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
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

    // UC10: Runs automatically after each configured delay.
    // A single scheduled execution handles all eligible users.
    @Scheduled(
            fixedDelayString =
                    "${app.reminder.check-delay-ms:60000}"
    )
    @Transactional
    public void processDueReminders() {

        Instant now = Instant.now();

        var dueNotes =
                noteRepository.findDueUnsentReminders(now);

        if (dueNotes.isEmpty()) {
            return;
        }

        log.info(
                "UC10: Processing {} due reminders",
                dueNotes.size()
        );

        for (Note note : dueNotes) {

            String recipient = note.getUser().getEmail();

            // Do not try to send emails with no recipient.
            if (recipient == null || recipient.isBlank()) {
                log.warn(
                        "Skipping reminder for note {}: "
                                + "user email is missing",
                        note.getId()
                );
                continue;
            }

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject(
                    "Fundoo Notes Reminder: "
                            + note.getTitle()
            );

            String description =
                    note.getDescription() == null
                            ? ""
                            : note.getDescription();

            message.setText(
                    "Hello,\n\n"
                            + "This is your Fundoo Notes reminder.\n\n"
                            + "Note: " + note.getTitle() + "\n"
                            + "Description: " + description + "\n"
                            + "Reminder time (UTC): "
                            + note.getReminderAt() + "\n\n"
                            + "Regards,\n"
                            + "Fundoo Notes"
            );

            try {
                // Mailpit captures this email locally.
                mailSender.send(message);

                // Record success so it is not sent again.
                note.setReminderSentAt(
                        Instant.now()
                );

                noteRepository.saveAndFlush(note);

                log.info(
                        "Reminder email sent for note {}",
                        note.getId()
                );

            } catch (MailException ex) {

                // Keep reminderSentAt as NULL so it
                // can be retried at a later check.
                log.error(
                        "Failed to email reminder for note {}: {}",
                        note.getId(),
                        ex.getMessage()
                );
            }
        }
    }
}
