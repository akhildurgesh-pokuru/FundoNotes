package com.fundoonotes.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteExportController {

    private final JobLauncher jobLauncher;
    private final Job exportNotesJob;

    public NoteExportController(
            JobLauncher jobLauncher,
            @Qualifier("exportNotesJob") Job exportNotesJob
    ) {
        this.jobLauncher = jobLauncher;
        this.exportNotesJob = exportNotesJob;
    }

    // UC16: Export only the authenticated user's notes.
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportNotes(
            @AuthenticationPrincipal Long userId
    ) {

        Path outputFile = null;

        try {

            // Do not accept file paths from HTTP requests.
            outputFile = Files.createTempFile(
                    "fundoo-export-",
                    ".csv"
            );

            // Each export has a unique job instance.
            JobParameters parameters =
                    new JobParametersBuilder()
                            .addString(
                                    "userId",
                                    userId.toString()
                            )
                            .addString(
                                    "outputFile",
                                    outputFile.toAbsolutePath().toString()
                            )
                            .addString(
                                    "requestId",
                                    UUID.randomUUID().toString()
                            )
                            .toJobParameters();

            JobExecution execution =
                    jobLauncher.run(
                            exportNotesJob,
                            parameters
                    );

            // The default launcher is synchronous
            // in this local learning configuration.
            if (execution.getStatus()
                    != BatchStatus.COMPLETED) {

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Notes export job failed"
                );
            }

            // Keep HTTP memory usage bounded.
            long fileSize = Files.size(outputFile);

            if (fileSize > 10L * 1024 * 1024) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "Export exceeds 10 MB"
                );
            }

            byte[] csvBytes =
                    Files.readAllBytes(outputFile);

            ContentDisposition disposition =
                    ContentDisposition
                            .attachment()
                            .filename("fundoo-notes.csv")
                            .build();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition.toString()
                    )
                    .contentType(
                            MediaType.parseMediaType(
                                    "text/csv; charset=UTF-8"
                            )
                    )
                    .body(csvBytes);

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to export notes",
                    ex
            );

        } finally {

            // Always remove the temporary CSV file,
            // including after a failed export.
            if (outputFile != null) {

                try {
                    Files.deleteIfExists(outputFile);
                } catch (Exception ignored) {
                    // Add cleanup logging for production.
                }
            }
        }
    }
}
