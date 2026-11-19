package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;

@Configuration
public class TrashCleanupBatchConfig {

    // READER: Read only IDs of expired trashed notes.
    //
    // We use a JDBC cursor instead of offset-based
    // pagination because rows are being deleted
    // while the job runs.
    @Bean
    @StepScope
    public JdbcCursorItemReader<Long> trashCleanupReader(
            DataSource dataSource,
            @Value("#{jobParameters['cutoff']}")
            String cutoffText
    ) {

        Instant cutoff = Instant.parse(cutoffText);

        return new JdbcCursorItemReaderBuilder<Long>()
                .name("trashCleanupReader")
                .dataSource(dataSource)
                .sql("""
                    SELECT id
                    FROM notes
                    WHERE trashed = true
                      AND trashed_at IS NOT NULL
                      AND trashed_at <= ?
                    ORDER BY id
                    """)
                .preparedStatementSetter(ps ->
                        ps.setTimestamp(
                                1,
                                Timestamp.from(cutoff)
                        )
                )
                .rowMapper((rs, rowNum) ->
                        rs.getLong("id")
                )
                .build();
    }

    // WRITER: Recheck each note before permanent deletion.
    //
    // Spring Batch executes this in a chunk transaction.
    @Bean
    @StepScope
    public ItemWriter<Long> trashCleanupWriter(
            NoteRepository noteRepository,
            @Value("#{jobParameters['cutoff']}")
            String cutoffText
    ) {

        Instant cutoff = Instant.parse(cutoffText);

        return chunk -> {

            for (Long noteId : chunk.getItems()) {

                Note note = noteRepository
                        .findById(noteId)
                        .orElse(null);

                if (note == null) {
                    continue;
                }

                // Never delete an active/restored note.
                if (!note.isTrashed()) {
                    continue;
                }

                // Never delete a note without a known
                // trash-entry time.
                if (note.getTrashedAt() == null) {
                    continue;
                }

                // Recheck the retention rule.
                if (note.getTrashedAt().isAfter(cutoff)) {
                    continue;
                }

                // JPA removes the note and handles its
                // owned many-to-many join-table links.
                noteRepository.delete(note);
            }
        };
    }

    // STEP: Delete in chunks of 20 records.
    @Bean
    public Step trashCleanupStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("trashCleanupReader")
            JdbcCursorItemReader<Long> trashCleanupReader,
            @Qualifier("trashCleanupWriter")
            ItemWriter<Long> trashCleanupWriter
    ) {

        return new StepBuilder(
                "trashCleanupStep",
                jobRepository
        )
                .<Long, Long>chunk(
                        20,
                        transactionManager
                )
                .reader(trashCleanupReader)
                .writer(trashCleanupWriter)
                .build();
    }

    // JOB: Complete scheduled cleanup operation.
    @Bean
    public Job trashCleanupJob(
            JobRepository jobRepository,
            @Qualifier("trashCleanupStep")
            Step trashCleanupStep
    ) {

        return new JobBuilder(
                "trashCleanupJob",
                jobRepository
        )
                .start(trashCleanupStep)
                .build();
    }
}
