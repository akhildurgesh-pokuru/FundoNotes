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

    // UC5: CREATE
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

    // UC5 + UC6: LIST ACTIVE NOTES
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(
                noteService.getAllNotes(userId)
        );
    }

    // UC6: LIST ARCHIVED NOTES
    @GetMapping("/archived")
    public ResponseEntity<List<NoteResponseDto>> getArchivedNotes(
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(
                noteService.getArchivedNotes(userId)
        );
    }

    // UC6: LIST TRASHED NOTES
    @GetMapping("/trash")
    public ResponseEntity<List<NoteResponseDto>> getTrashedNotes(
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(
                noteService.getTrashedNotes(userId)
        );
    }

    // UC5: GET ONE NOTE
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> getNoteById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.getNoteById(userId, id)
        );
    }

    // UC5: UPDATE
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

    // UC6: PIN
    @PatchMapping("/{id}/pin")
    public ResponseEntity<NoteResponseDto> pinNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.pinNote(userId, id)
        );
    }

    // UC6: UNPIN
    @PatchMapping("/{id}/unpin")
    public ResponseEntity<NoteResponseDto> unpinNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.unpinNote(userId, id)
        );
    }

    // UC6: ARCHIVE
    @PatchMapping("/{id}/archive")
    public ResponseEntity<NoteResponseDto> archiveNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.archiveNote(userId, id)
        );
    }

    // UC6: UNARCHIVE
    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<NoteResponseDto> unarchiveNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.unarchiveNote(userId, id)
        );
    }

    // UC6: MOVE TO TRASH
    @PatchMapping("/{id}/trash")
    public ResponseEntity<NoteResponseDto> moveToTrash(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.moveToTrash(userId, id)
        );
    }

    // UC6: RESTORE FROM TRASH
    @PatchMapping("/{id}/restore")
    public ResponseEntity<NoteResponseDto> restoreNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                noteService.restoreNote(userId, id)
        );
    }

    // UC5 endpoint, now performing UC6 soft delete.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        noteService.deleteNote(userId, id);

        return ResponseEntity.noContent().build();
    }

    // UC6: PERMANENT DELETE
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {

        noteService.permanentlyDeleteNote(userId, id);

        return ResponseEntity.noContent().build();
    }
}
