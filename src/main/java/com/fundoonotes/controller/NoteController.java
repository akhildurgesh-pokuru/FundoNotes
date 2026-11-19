package com.fundoonotes.controller;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // POST /api/notes
    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NoteRequestDto request
    ) {

        NoteResponseDto response =
                noteService.createNote(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET /api/notes
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(
                noteService.getAllNotes(userId)
        );
    }

    // GET /api/notes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> getNoteById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.getNoteById(userId, id)
        );
    }

    // PUT /api/notes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDto> updateNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequestDto request
    ) {

        return ResponseEntity.ok(
                noteService.updateNote(userId, id, request)
        );
    }

    // DELETE /api/notes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        noteService.deleteNote(userId, id);

        return ResponseEntity.noContent().build();
    }
}
