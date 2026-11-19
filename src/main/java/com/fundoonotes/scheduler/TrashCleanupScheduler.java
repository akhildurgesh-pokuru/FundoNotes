package com.fundoonotes.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class TrashCleanupScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    TrashCleanupScheduler.class
            );

    private final JobLauncher jobLauncher;
    private final Job trashCleanupJob;
    private final long retentionDays;

    public TrashCleanupScheduler(
            JobLauncher jobLauncher,
            @Qualifier("trashCleanupJob")
            Job trashCleanupJob,
            @Value("${app.cleanup.retention-days:30}")
            long retentionDays
    ) {

        if (retentionDays < 1) {
            throw new IllegalArgumentException(
                    "Trash retention must be at least 1 day"
            );
        }

        this.jobLauncher = jobLauncher;
        this.trashCleanupJob = trashCleanupJob;
        this.retentionDays = retentionDays;
    }

    // UC15: Automatically run cleanup every day.
    @Scheduled(
            cron = "${app.cleanup.cron:0 0 2 * * *}",
            zone = "${app.cleanup.zone:Asia/Kolkata}"
    )
    public void scheduledCleanup() {

        try {

            Instant cutoff = Instant.now().minus(
                    Duration.ofDays(retentionDays)
            );

            // Unique execution ID allows new scheduled
            // runs even after a previous run completed.
            JobParameters parameters =
                    new JobParametersBuilder()
                            .addString(
                                    "cutoff",
                                    cutoff.toString()
                            )
                            .addString(
                                    "runId",
                                    UUID.randomUUID().toString()
                            )
                            .toJobParameters();

            log.info(
                    "UC15: Starting trash cleanup, cutoff={}",
                    cutoff
            );

            JobExecution execution =
                    jobLauncher.run(
                            trashCleanupJob,
                            parameters
                    );

            if (execution.getStatus()
                    == BatchStatus.COMPLETED) {

                log.info(
                        "UC15: Cleanup job completed. jobId={}",
                        execution.getId()
                );

            } else {

                log.error(
                        "UC15: Cleanup ended with status {}",
                        execution.getStatus()
                );
            }

        } catch (Exception ex) {

            log.error(
                    "UC15: Trash cleanup failed: {}",
                    ex.getClass().getSimpleName()
            );
        }
    }
}
