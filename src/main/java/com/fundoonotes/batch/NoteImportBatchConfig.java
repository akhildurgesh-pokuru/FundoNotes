package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NoteImportBatchConfig {

    // STEP A: Reader
    // Create one reader for each job execution.
    @Bean
    @StepScope
    public FlatFileItemReader<NoteImportRow> noteCsvReader(
            @Value("#{jobParameters['inputFile']}")
            String inputFile
    ) {

        DelimitedLineTokenizer tokenizer =
                new DelimitedLineTokenizer();

        tokenizer.setDelimiter(",");
        tokenizer.setNames("title", "description");
        tokenizer.setStrict(true);

        DefaultLineMapper<NoteImportRow> lineMapper =
                new DefaultLineMapper<>();

        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet ->
                new NoteImportRow(
                        fieldSet.readString("title"),
                        fieldSet.readString("description")
                )
        );

        return new FlatFileItemReaderBuilder<NoteImportRow>()
                .name("noteCsvReader")
                .resource(new FileSystemResource(inputFile))
                .linesToSkip(1) // Skip CSV header row.
                .lineMapper(lineMapper)
                .build();
    }

    // STEP B: Processor
    // The owner is determined by the authenticated user ID.
    @Bean
    @StepScope
    public ItemProcessor<NoteImportRow, Note> noteProcessor(
            @Value("#{jobParameters['userId']}")
            String userId,
            UserRepository userRepository
    ) {

        Long ownerId = Long.valueOf(userId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Import user not found"
                        )
                );

        return new NoteImportProcessor(owner);
    }

    // STEP C: Writer
    // Save imported notes inside Spring Batch chunk transactions.
    @Bean
    public ItemWriter<Note> noteWriter(
            NoteRepository noteRepository
    ) {
        return chunk -> {

            for (Note note : chunk.getItems()) {
                noteRepository.save(note);
            }
        };
    }

    // STEP D: Connect reader -> processor -> writer.
    @Bean
    public Step importNotesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<NoteImportRow> noteCsvReader,
            ItemProcessor<NoteImportRow, Note> noteProcessor,
            ItemWriter<Note> noteWriter
    ) {

        return new StepBuilder(
                "importNotesStep",
                jobRepository
        )
                .<NoteImportRow, Note>chunk(
                        20,
                        transactionManager
                )
                .reader(noteCsvReader)
                .processor(noteProcessor)
                .writer(noteWriter)
                .build();
    }

    // STEP E: Create the complete import job.

    @Bean
    public Job importNotesJob(
            JobRepository jobRepository,
            @Qualifier("importNotesStep")
            Step importNotesStep
    ) {

        return new JobBuilder(
                "importNotesJob",
                jobRepository
        )
                .start(importNotesStep)
                .build();
    }

}
