package com.fundoonotes.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrashCleanupSchedulerTest {

    private JobLauncher jobLauncher;
    private Job cleanupJob;
    private TrashCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        jobLauncher = mock(JobLauncher.class);
        cleanupJob = mock(Job.class);

        // Default retention is 30 days.
        scheduler = new TrashCleanupScheduler(
                jobLauncher,
                cleanupJob,
                30
        );
    }

    @Test
    void scheduledCleanup_ShouldLaunchBatchJob()
            throws Exception {

        JobExecution execution =
                mock(JobExecution.class);

        when(execution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(jobLauncher.run(
                eq(cleanupJob),
                any(JobParameters.class)
        )).thenReturn(execution);

        Instant before = Instant.now();

        scheduler.scheduledCleanup();

        Instant after = Instant.now();

        ArgumentCaptor<JobParameters> captor =
                ArgumentCaptor.forClass(
                        JobParameters.class
                );

        verify(jobLauncher).run(
                eq(cleanupJob),
                captor.capture()
        );

        JobParameters parameters = captor.getValue();

        String cutoffText =
                parameters.getString("cutoff");

        assertNotNull(cutoffText);
        assertNotNull(parameters.getString("runId"));

        Instant actualCutoff =
                Instant.parse(cutoffText);

        Instant earliest =
                before.minus(Duration.ofDays(30));

        Instant latest =
                after.minus(Duration.ofDays(30));

        // Allow the small execution-time interval.
        assertFalse(actualCutoff.isBefore(earliest));
        assertFalse(actualCutoff.isAfter(latest));
    }

    @Test
    void zeroRetention_ShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new TrashCleanupScheduler(
                        jobLauncher,
                        cleanupJob,
                        0
                )
        );
    }

    @Test
    void negativeRetention_ShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new TrashCleanupScheduler(
                        jobLauncher,
                        cleanupJob,
                        -5
                )
        );
    }
}
