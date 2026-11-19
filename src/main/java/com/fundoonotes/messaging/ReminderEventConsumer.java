package com.fundoonotes.messaging;

import com.fundoonotes.config.RabbitMqConfig;
import com.fundoonotes.service.ReminderNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReminderEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ReminderEventConsumer.class
            );

    private final ReminderNotificationService
            notificationService;

    public ReminderEventConsumer(
            ReminderNotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    // UC12: Automatically receive RabbitMQ messages.
    @RabbitListener(
            queues = RabbitMqConfig.REMINDER_QUEUE
    )
    public void handleReminder(String noteIdText) {

        Long noteId;

        try {
            noteId = Long.parseLong(noteIdText);
        } catch (NumberFormatException ex) {
            log.warn(
                    "Invalid reminder message: {}",
                    noteIdText
            );
            return;
        }

        log.info(
                "UC12: Consuming reminder event for note {}",
                noteId
        );

        // Call a separate Spring service so its
        // @Transactional behavior is applied correctly.
        notificationService.sendDueReminder(noteId);
    }
}
