package com.fundoonotes.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;

import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteImportController {

    private final JobLauncher jobLauncher;
    private final Job importNotesJob;

    public NoteImportController(
            JobLauncher jobLauncher,
            Job importNotesJob
    ) {
        this.jobLauncher = jobLauncher;
        this.importNotesJob = importNotesJob;
    }

    // UC14: Upload a CSV file containing notes.
    @PostMapping(
            value = "/import",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Map<String, Object>> importNotes(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV file cannot be empty"
            );
        }

        if (file.getSize() > 2L * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV file must be 2 MB or smaller"
            );
        }

        Path tempFile = null;

        try {

            // Use a server-generated path, not a filename
            // supplied by the API caller.
            tempFile = Files.createTempFile(
                    "fundoo-note-import-",
                    ".csv"
            );

            Files.copy(
                    file.getInputStream(),
                    tempFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // UC14 expects a specific CSV header.
            try (BufferedReader reader =
                         Files.newBufferedReader(
                                 tempFile,
                                 StandardCharsets.UTF_8
                         )) {

                String header = reader.readLine();

                if (header == null
                        || !header.replace("\uFEFF", "")
                        .trim()
                        .equals("title,description")) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "CSV header must be title,description"
                    );
                }
            }

            // A unique parameter identifies this import job.
            JobParameters parameters =
                    new JobParametersBuilder()
                            .addString(
                                    "inputFile",
                                    tempFile.toAbsolutePath().toString()
                            )
                            .addString(
                                    "userId",
                                    userId.toString()
                            )
                            .addString(
                                    "requestId",
                                    UUID.randomUUID().toString()
                            )
                            .toJobParameters();

            // This beginner endpoint runs the job synchronously.
            JobExecution execution =
                    jobLauncher.run(importNotesJob, parameters);

            long readCount = 0;
            long writeCount = 0;
            long filterCount = 0;

            for (StepExecution step :
                    execution.getStepExecutions()) {

                readCount += step.getReadCount();
                writeCount += step.getWriteCount();
                filterCount += step.getFilterCount();
            }

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("jobId", execution.getId());
            result.put(
                    "status",
                    execution.getStatus().name()
            );
            result.put("readCount", readCount);
            result.put("importedCount", writeCount);
            result.put("filteredCount", filterCount);

            HttpStatus responseStatus =
                    execution.getStatus() == BatchStatus.COMPLETED
                            ? HttpStatus.OK
                            : HttpStatus.INTERNAL_SERVER_ERROR;

            return ResponseEntity
                    .status(responseStatus)
                    .body(result);

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (Exception ex) {

            // Don't expose internal file paths or
            // database errors in the API response.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Note import could not be completed",
                    ex
            );

        } finally {

            // Our job is synchronous, so this temporary
            // upload can be removed after execution.
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Log cleanup failures in production.
                }
            }
        }
    }
}
