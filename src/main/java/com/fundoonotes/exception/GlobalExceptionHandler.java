
package com.fundoonotes.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUser(
            UserAlreadyExistsException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        // Collect each invalid field and its validation message.
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("statusCode", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataConflict(
            DataIntegrityViolationException exception
    ) {

        // Return a safe message instead of exposing database details.
        return buildError(
                HttpStatus.CONFLICT,
                "Registration conflicts with existing data"
        );
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status,
            String message
    ) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("statusCode", status.value());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }


    @ExceptionHandler(InvalidAccountTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAccountToken(
            InvalidAccountTokenException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }


    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoteNotFound(
            NoteNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }


    @ExceptionHandler(InvalidNoteStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidNoteState(
            InvalidNoteStateException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }





}
