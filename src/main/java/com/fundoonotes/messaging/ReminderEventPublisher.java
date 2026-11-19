package com.fundoonotes.messaging;

import com.fundoonotes.config.RabbitMqConfig;
import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ReminderEventPublisher.class
            );

    private final NoteRepository noteRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReminderEventPublisher(
            NoteRepository noteRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.noteRepository = noteRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // UC12: Find due reminders and publish messages.
    @Scheduled(
            fixedDelayString =
                    "${app.reminder.check-delay-ms:60000}"
    )
    public void publishDueReminders() {

        List<Note> dueNotes =
                noteRepository.findDueUnsentReminders(
                        Instant.now()
                );

        for (Note note : dueNotes) {

            // Send only the note ID as a simple String.
            // Consumer will fetch current data from MySQL.
            String payload = note.getId().toString();

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.REMINDER_EXCHANGE,
                    RabbitMqConfig.REMINDER_ROUTING_KEY,
                    payload
            );

            log.info(
                    "UC12: Published due reminder for note {}",
                    note.getId()
            );
        }
    }
}
