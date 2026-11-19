package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteExportBatchConfigTest {

    @Test
    void exportNotes_ShouldCreateCsvForAuthenticatedUser()
            throws Exception {

        NoteRepository repository =
                mock(NoteRepository.class);

        Note note = new Note();
        note.setId(5L);
        note.setTitle("Learn Spring Batch");
        note.setDescription("Import and export notes");
        note.setPinned(false);

        // Only notes owned by user 3 will be returned.
        when(repository
                .findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
                        3L
                ))
                .thenReturn(List.of(note));

        Path csvFile = Files.createTempFile(
                "uc16-export-test-",
                ".csv"
        );

        try {

            NoteExportBatchConfig config =
                    new NoteExportBatchConfig();

            Tasklet tasklet = config.exportNotesTasklet(
                    "3",
                    csvFile.toString(),
                    repository
            );

            StepContribution contribution =
                    mock(StepContribution.class);

            StepExecution stepExecution =
                    mock(StepExecution.class);

            when(contribution.getStepExecution())
                    .thenReturn(stepExecution);

            when(stepExecution.getExecutionContext())
                    .thenReturn(new ExecutionContext());

            tasklet.execute(
                    contribution,
                    mock(ChunkContext.class)
            );

            String csv = Files.readString(csvFile);

            assertTrue(csv.contains(
                    "id,title,description,pinned"
            ));

            assertTrue(csv.contains(
                    "Learn Spring Batch"
            ));

            assertTrue(csv.contains(
                    "Import and export notes"
            ));

            verify(repository)
                    .findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
                            3L
                    );

        } finally {
            Files.deleteIfExists(csvFile);
        }
    }

    @Test
    void exportNotes_ShouldEscapeCommasAndQuotes()
            throws Exception {

        NoteRepository repository =
                mock(NoteRepository.class);

        Note note = new Note();
        note.setId(8L);
        note.setTitle("Study Java, Spring");
        note.setDescription("Practice \"Batch\" processing");

        when(repository
                .findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
                        3L
                ))
                .thenReturn(List.of(note));

        Path csvFile = Files.createTempFile(
                "uc16-escape-test-",
                ".csv"
        );

        try {

            Tasklet tasklet =
                    new NoteExportBatchConfig()
                            .exportNotesTasklet(
                                    "3",
                                    csvFile.toString(),
                                    repository
                            );

            StepContribution contribution =
                    mock(StepContribution.class);

            StepExecution execution =
                    mock(StepExecution.class);

            when(contribution.getStepExecution())
                    .thenReturn(execution);

            when(execution.getExecutionContext())
                    .thenReturn(new ExecutionContext());

            tasklet.execute(
                    contribution,
                    mock(ChunkContext.class)
            );

            String csv = Files.readString(csvFile);

            assertTrue(
                    csv.contains("\"Study Java, Spring\"")
            );

            assertTrue(
                    csv.contains(
                            "\"Practice \"\"Batch\"\" processing\""
                    )
            );

        } finally {
            Files.deleteIfExists(csvFile);
        }
    }
}
