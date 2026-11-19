package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Configuration
public class NoteExportBatchConfig {

    // Create a separate Tasklet for each export execution.
    @Bean
    @StepScope
    public Tasklet exportNotesTasklet(
            @Value("#{jobParameters['userId']}")
            String userIdText,
            @Value("#{jobParameters['outputFile']}")
            String outputFile,
            NoteRepository noteRepository
    ) {

        return (contribution, chunkContext) -> {

            Long userId = Long.valueOf(userIdText);

            // Reuse the owner-safe UC6 repository method.
            // Only active notes of this user are exported.
            List<Note> notes = noteRepository
                    .findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
                            userId
                    );

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 Path.of(outputFile),
                                 StandardCharsets.UTF_8
                         )) {

                writer.write(
                        "id,title,description,pinned,"
                                + "created_at,updated_at"
                );
                writer.newLine();

                for (Note note : notes) {

                    String line =
                            note.getId() + ","
                                    + csvCell(note.getTitle()) + ","
                                    + csvCell(note.getDescription()) + ","
                                    + note.isPinned() + ","
                                    + csvCell(timeText(note.getCreatedAt())) + ","
                                    + csvCell(timeText(note.getUpdatedAt()));

                    writer.write(line);
                    writer.newLine();
                }
            }

            // Store the number of exported notes in the
            // Batch execution context for debugging.
            contribution.getStepExecution()
                    .getExecutionContext()
                    .putInt("exportedCount", notes.size());

            return RepeatStatus.FINISHED;
        };
    }

    // Handle commas, quotes, and newlines safely.
    // Also protect spreadsheet users against CSV
    // formula injection in note text.
    private static String csvCell(String value) {

        if (value == null) {
            value = "";
        }

        // A spreadsheet may interpret user-controlled
        // text beginning with these characters as a formula.
        String check = value.stripLeading();

        if (check.startsWith("=")
                || check.startsWith("+")
                || check.startsWith("-")
                || check.startsWith("@")
                || check.startsWith("\t")
                || check.startsWith("\r")) {
            value = "'" + value;
        }

        // Escape quotes and quote every text field.
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String timeText(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    @Bean
    public Step exportNotesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("exportNotesTasklet") Tasklet tasklet
    ) {

        return new StepBuilder(
                "exportNotesStep",
                jobRepository
        )
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job exportNotesJob(
            JobRepository jobRepository,
            @Qualifier("exportNotesStep") Step step
    ) {

        return new JobBuilder(
                "exportNotesJob",
                jobRepository
        )
                .start(step)
                .build();
    }
}
